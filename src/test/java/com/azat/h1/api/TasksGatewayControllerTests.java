package com.azat.h1.api;

import com.azat.h1.dto.LoginRequest;
import com.azat.h1.dto.LoginResponse;
import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TasksGatewayControllerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void createTaskWithTokenCreatesExternalTask() {
		ResponseEntity<ExternalTaskResponse> response = restTemplate.exchange(
				"/api/v1/tasks",
				HttpMethod.POST,
				authorizedEntity(request("Gateway create", false)),
				ExternalTaskResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Gateway create");
	}

	@Test
	void getTaskWithTokenReturnsExternalTask() {
		ExternalTaskResponse createdTask = createTask("Gateway get", true);

		ResponseEntity<ExternalTaskResponse> response = restTemplate.exchange(
				"/api/v1/tasks/" + createdTask.getId(),
				HttpMethod.GET,
				authorizedEntity(null),
				ExternalTaskResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(createdTask.getId());
		assertThat(response.getBody().isCompleted()).isTrue();
	}

	@Test
	void getTasksWithQueryParamsReturnsFilteredExternalTasks() {
		createTask("Gateway list false", false);
		createTask("Gateway list true", true);

		ResponseEntity<ExternalTaskResponse[]> response = restTemplate.exchange(
				"/api/v1/tasks?completed=false&limit=10",
				HttpMethod.GET,
				authorizedEntity(null),
				ExternalTaskResponse[].class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).allMatch(task -> !task.isCompleted());
		assertThat(response.getBody().length).isLessThanOrEqualTo(10);
	}

	@Test
	void deleteTaskWithTokenReturnsNoContent() {
		ExternalTaskResponse createdTask = createTask("Gateway delete", false);

		ResponseEntity<Void> response = restTemplate.exchange(
				"/api/v1/tasks/" + createdTask.getId(),
				HttpMethod.DELETE,
				authorizedEntity(null),
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void getMissingTaskReturnsNotFoundError() {
		ResponseEntity<String> response = restTemplate.exchange(
				"/api/v1/tasks/999999",
				HttpMethod.GET,
				authorizedEntity(null),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).contains("\"status\":404");
		assertThat(response.getBody()).contains("External task not found: 999999");
	}

	@Test
	void gatewayTasksWithoutTokenReturnsUnauthorized() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/tasks", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).contains("\"status\":401");
	}

	private ExternalTaskResponse createTask(String title, boolean completed) {
		ResponseEntity<ExternalTaskResponse> response = restTemplate.exchange(
				"/api/v1/tasks",
				HttpMethod.POST,
				authorizedEntity(request(title, completed)),
				ExternalTaskResponse.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

	private HttpEntity<?> authorizedEntity(Object body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(loginAndGetToken());
		return new HttpEntity<>(body, headers);
	}

	private String loginAndGetToken() {
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUsername("user");
		loginRequest.setPassword("password");
		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
				"/api/v1/auth/login",
				loginRequest,
				LoginResponse.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		return response.getBody().getAccessToken();
	}

	private ExternalTaskRequest request(String title, boolean completed) {
		ExternalTaskRequest request = new ExternalTaskRequest();
		request.setTitle(title);
		request.setDescription("Gateway test task");
		request.setCompleted(completed);
		return request;
	}
}
