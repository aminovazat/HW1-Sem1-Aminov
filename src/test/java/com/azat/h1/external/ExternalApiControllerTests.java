package com.azat.h1.external;

import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExternalApiControllerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void postCreatesTaskAndReturnsLocation() {
		ResponseEntity<ExternalTaskResponse> response = createExternalTask("External create", false);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isNotNull();
	}

	@Test
	void getExistingTaskReturnsTask() {
		ExternalTaskResponse createdTask = createExternalTask("External get", false).getBody();
		assertThat(createdTask).isNotNull();

		ResponseEntity<ExternalTaskResponse> response = restTemplate.getForEntity(
				"/external/v1/tasks/" + createdTask.getId(),
				ExternalTaskResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("External get");
	}

	@Test
	void getMissingTaskReturnsProblemDetail() {
		ResponseEntity<String> response = restTemplate.getForEntity("/external/v1/tasks/999999", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).contains("External task not found");
	}

	@Test
	void deleteExistingTaskReturnsNoContent() {
		ExternalTaskResponse createdTask = createExternalTask("External delete", false).getBody();
		assertThat(createdTask).isNotNull();

		ResponseEntity<Void> response = restTemplate.exchange(
				"/external/v1/tasks/" + createdTask.getId(),
				org.springframework.http.HttpMethod.DELETE,
				null,
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void unstableHtmlReturnsTextHtmlBadGateway() {
		ResponseEntity<String> response = restTemplate.getForEntity("/external/v1/unstable?mode=html", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_HTML);
		assertThat(response.getBody()).contains("<html>");
	}

	@Test
	void unstableRateLimitReturnsRetryAfter() {
		ResponseEntity<String> response = restTemplate.getForEntity("/external/v1/unstable?mode=429", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("2");
	}

	private ResponseEntity<ExternalTaskResponse> createExternalTask(String title, boolean completed) {
		ExternalTaskRequest request = new ExternalTaskRequest();
		request.setTitle(title);
		request.setDescription("External test");
		request.setCompleted(completed);
		return restTemplate.postForEntity("/external/v1/tasks", request, ExternalTaskResponse.class);
	}
}
