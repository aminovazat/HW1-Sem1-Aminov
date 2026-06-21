package com.azat.h1.api;

import com.azat.h1.dto.ErrorResponse;
import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
		"server.port=18080",
		"external-api.base-url=http://localhost:18080/external/v1"
})
class TasksGatewayControllerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void gatewayCreatesGetsListsAndDeletesExternalTask() {
		ExternalTaskRequest request = externalTaskRequest("Gateway task", false);

		ResponseEntity<ExternalTaskResponse> createResponse = restTemplate.exchange(
				"/api/v1/tasks",
				HttpMethod.POST,
				bearerEntity(token("user"), request),
				ExternalTaskResponse.class
		);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createResponse.getBody()).isNotNull();

		Long taskId = createResponse.getBody().getId();
		ResponseEntity<ExternalTaskResponse> getResponse = restTemplate.exchange(
				"/api/v1/tasks/" + taskId,
				HttpMethod.GET,
				bearerEntity(token("user")),
				ExternalTaskResponse.class
		);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().getTitle()).isEqualTo("Gateway task");

		ResponseEntity<ExternalTaskResponse[]> listResponse = restTemplate.exchange(
				"/api/v1/tasks?completed=false&limit=10",
				HttpMethod.GET,
				bearerEntity(token("user")),
				ExternalTaskResponse[].class
		);
		assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(listResponse.getBody()).isNotNull();
		assertThat(listResponse.getBody()).extracting(ExternalTaskResponse::getId).contains(taskId);

		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				"/api/v1/tasks/" + taskId,
				HttpMethod.DELETE,
				bearerEntity(token("user")),
				Void.class
		);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void gatewayMissingTaskReturnsMeaningfulNotFound() {
		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/v1/tasks/999999",
				HttpMethod.GET,
				bearerEntity(token("user")),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("External task not found");
	}

	@Test
	void gatewayWithoutTokenReturnsUnauthorized() {
		ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/v1/tasks", ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(401);
	}

	@Test
	void unstableExternalFailureReturnsGracefulFallback() {
		ResponseEntity<String> response = restTemplate.exchange(
				"/api/v1/tasks/unstable?mode=500",
				HttpMethod.GET,
				bearerEntity(token("user")),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Graceful degradation");
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

	private ExternalTaskRequest externalTaskRequest(String title, boolean completed) {
		ExternalTaskRequest request = new ExternalTaskRequest();
		request.setTitle(title);
		request.setDescription("Gateway test");
		request.setCompleted(completed);
		return request;
	}

	private HttpEntity<Void> bearerEntity(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(headers);
	}

	private HttpEntity<ExternalTaskRequest> bearerEntity(String token, ExternalTaskRequest request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(request, headers);
	}
}
