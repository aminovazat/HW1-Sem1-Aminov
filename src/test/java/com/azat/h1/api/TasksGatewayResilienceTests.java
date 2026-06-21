package com.azat.h1.api;

import com.azat.h1.dto.ErrorResponse;
import com.azat.h1.dto.security.LoginRequest;
import com.azat.h1.dto.security.LoginResponse;
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
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"server.port=18081",
		"external-api.base-url=http://localhost:18081/external/v1",
		"resilience4j.ratelimiter.instances.externalApi.limit-for-period=1",
		"resilience4j.ratelimiter.instances.externalApi.limit-refresh-period=60s",
		"resilience4j.ratelimiter.instances.externalApi.timeout-duration=0"
})
class TasksGatewayResilienceTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void rateLimiterReturnsMeaningfulTooManyRequests() {
		String token = token("user");

		ResponseEntity<String> firstResponse = restTemplate.exchange(
				"/api/v1/tasks",
				HttpMethod.GET,
				bearerEntity(token),
				String.class
		);
		ResponseEntity<ErrorResponse> secondResponse = restTemplate.exchange(
				"/api/v1/tasks",
				HttpMethod.GET,
				bearerEntity(token),
				ErrorResponse.class
		);

		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(secondResponse.getBody()).isNotNull();
		assertThat(secondResponse.getBody().getMessage()).contains("rate limit");
	}

	private String token(String username) {
		LoginRequest request = new LoginRequest();
		request.setUsername(username);
		request.setPassword("password");
		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
				"/api/v1/auth/login",
				request,
				LoginResponse.class
		);
		assertThat(response.getBody()).isNotNull();
		return response.getBody().getAccessToken();
	}

	private HttpEntity<Void> bearerEntity(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(headers);
	}
}
