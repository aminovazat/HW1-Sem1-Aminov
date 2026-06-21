package com.azat.h1.security;

import com.azat.h1.dto.ErrorResponse;
import com.azat.h1.dto.security.LoginRequest;
import com.azat.h1.dto.security.LoginResponse;
import com.azat.h1.dto.security.MessageResponse;
import com.azat.h1.dto.security.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityApiTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void loginWithValidUserReturnsToken() {
		ResponseEntity<LoginResponse> response = login("user", "password");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getAccessToken()).isNotBlank();
		assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
	}

	@Test
	void loginWithInvalidPasswordReturnsUnauthorized() {
		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
				"/api/v1/auth/login",
				loginRequest("user", "wrong"),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(401);
	}

	@Test
	void profileWithoutTokenReturnsUnauthorizedJson() {
		ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/v1/profile", ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Authentication required");
	}

	@Test
	void profileWithUserTokenReturnsProfile() {
		ResponseEntity<ProfileResponse> response = restTemplate.exchange(
				"/api/v1/profile",
				HttpMethod.GET,
				bearerEntity(token("user")),
				ProfileResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getUsername()).isEqualTo("user");
		assertThat(response.getBody().getAuthorities()).contains("ROLE_USER");
	}

	@Test
	void docsWithUserTokenReturnsForbidden() {
		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/v1/docs",
				HttpMethod.GET,
				bearerEntity(token("user")),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(403);
	}

	@Test
	void docsWithReaderTokenReturnsOk() {
		ResponseEntity<MessageResponse> response = restTemplate.exchange(
				"/api/v1/docs",
				HttpMethod.GET,
				bearerEntity(token("reader")),
				MessageResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Protected");
	}

	@Test
	void invalidTokenReturnsUnauthorized() {
		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/v1/profile",
				HttpMethod.GET,
				bearerEntity("bad-token"),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Invalid");
	}

	@Test
	void traceIdHeaderIsReturned() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Trace-Id", "test-trace-id");

		ResponseEntity<String> response = restTemplate.exchange(
				"/actuator/health",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo("test-trace-id");
	}

	@Test
	void generatedTraceIdHeaderIsReturned() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst("X-Trace-Id")).isNotBlank();
	}

	@Test
	void actuatorMetricsIsPublic() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/metrics", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	private ResponseEntity<LoginResponse> login(String username, String password) {
		return restTemplate.postForEntity("/api/v1/auth/login", loginRequest(username, password), LoginResponse.class);
	}

	private LoginRequest loginRequest(String username, String password) {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword(password);
		return request;
	}

	private String token(String username) {
		ResponseEntity<LoginResponse> response = login(username, "password");
		assertThat(response.getBody()).isNotNull();
		return response.getBody().getAccessToken();
	}

	private HttpEntity<Void> bearerEntity(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(headers);
	}
}
