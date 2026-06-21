package com.azat.h1.controller;

import com.azat.h1.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void getAllTasksReturnsTasks() {
		ResponseEntity<Task[]> response = restTemplate.getForEntity("/api/tasks", Task[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).extracting(Task::getTitle)
				.contains("Learn Spring", "Write Tests", "Finish Homework");
	}

	@Test
	void getAllTasksWithUnsupportedAcceptHeaderReturnsNotAcceptable() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_XML_VALUE));

		ResponseEntity<String> response = restTemplate.exchange(
				"/api/tasks",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void getTaskByIdReturnsTask() {
		Task createdTask = createTask("Get by id", "Positive get by id test");

		ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/" + createdTask.getId(), Task.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(createdTask.getId());
		assertThat(response.getBody().getTitle()).isEqualTo("Get by id");
	}

	@Test
	void getTaskByIdReturnsNotFoundWhenTaskDoesNotExist() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/999999", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void createTaskReturnsCreatedTask() {
		Task request = new Task(null, "Created task", "Created from controller test", false);

		ResponseEntity<Task> response = restTemplate.postForEntity("/api/tasks", request, Task.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Created task");
		assertThat(response.getBody().isCompleted()).isFalse();
	}

	@Test
	void createTaskWithMalformedJsonReturnsBadRequest() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>("{bad json", headers);

		ResponseEntity<String> response = restTemplate.postForEntity("/api/tasks", request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	void updateTaskReturnsUpdatedTask() {
		Task createdTask = createTask("Before update", "Will be updated");
		Task request = new Task(null, "After update", "Updated through PUT", true);

		ResponseEntity<Task> response = restTemplate.exchange(
				"/api/tasks/" + createdTask.getId(),
				HttpMethod.PUT,
				new HttpEntity<>(request),
				Task.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(createdTask.getId());
		assertThat(response.getBody().getTitle()).isEqualTo("After update");
		assertThat(response.getBody().isCompleted()).isTrue();
	}

	@Test
	void updateTaskReturnsNotFoundWhenTaskDoesNotExist() {
		Task request = new Task(null, "Missing update", "This task does not exist", true);

		ResponseEntity<String> response = restTemplate.exchange(
				"/api/tasks/999999",
				HttpMethod.PUT,
				new HttpEntity<>(request),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void deleteTaskReturnsNoContent() {
		Task createdTask = createTask("Delete me", "Positive delete test");
		HttpEntity<Void> request = new HttpEntity<>(null);

		ResponseEntity<Void> response = restTemplate.exchange(
				"/api/tasks/" + createdTask.getId(),
				HttpMethod.DELETE,
				request,
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		assertThat(restTemplate.getForEntity("/api/tasks/" + createdTask.getId(), String.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteTaskReturnsNotFoundWhenTaskDoesNotExist() {
		HttpEntity<Void> request = new HttpEntity<>(null);

		ResponseEntity<String> response = restTemplate.exchange(
				"/api/tasks/999999",
				HttpMethod.DELETE,
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void statisticsReturnsRepositoryCounts() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/statistics", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).contains("Primary repository tasks:");
		assertThat(response.getBody()).contains("stub repository tasks: 2");
	}

	@Test
	void scopeEndpointReturnsRequestAndPrototypeDetails() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/scope", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).contains("requestId=");
		assertThat(response.getBody()).contains("prototypeTaskId=");
	}

	@Test
	void statisticsUnknownNestedPathReturnsNotFound() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/statistics/details", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
	}

	private Task createTask(String title, String description) {
		ResponseEntity<Task> response = restTemplate.postForEntity(
				"/api/tasks",
				new Task(null, title, description, false),
				Task.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}
}
