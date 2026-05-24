package com.azat.h1.controller;

import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.model.Priority;
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

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void getAllTasksReturnsDtosAndHeaders() {
		ResponseEntity<TaskResponseDto[]> response = restTemplate.getForEntity("/api/tasks", TaskResponseDto[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
		assertThat(response.getHeaders().getFirst("X-Total-Count")).isNotBlank();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).extracting(TaskResponseDto::getTitle)
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
		assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
	}

	@Test
	void getTaskByIdReturnsTask() {
		TaskResponseDto createdTask = createTask("Get by id", "Positive get by id test");

		ResponseEntity<TaskResponseDto> response =
				restTemplate.getForEntity("/api/tasks/" + createdTask.getId(), TaskResponseDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(createdTask.getId());
		assertThat(response.getBody().getTitle()).isEqualTo("Get by id");
	}

	@Test
	void getTaskByIdReturnsNotFoundWhenTaskDoesNotExist() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/999999", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).contains("\"status\":404");
	}

	@Test
	void createTaskReturnsCreatedTask() {
		TaskCreateDto request = createRequest("Created task", "Created from controller test");

		ResponseEntity<TaskResponseDto> response = restTemplate.postForEntity("/api/tasks", request, TaskResponseDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Created task");
		assertThat(response.getBody().getPriority()).isEqualTo(Priority.MEDIUM);
	}

	@Test
	void createTaskWithMalformedJsonReturnsBadRequest() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>("{bad json", headers);

		ResponseEntity<String> response = restTemplate.postForEntity("/api/tasks", request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("\"status\":400");
	}

	@Test
	void updateTaskReturnsUpdatedTask() {
		TaskResponseDto createdTask = createTask("Before update", "Will be updated");
		TaskUpdateDto request = new TaskUpdateDto();
		request.setTitle("After update");
		request.setDescription("Updated through PUT");
		request.setCompleted(true);
		request.setPriority(Priority.HIGH);

		ResponseEntity<TaskResponseDto> response = restTemplate.exchange(
				"/api/tasks/" + createdTask.getId(),
				HttpMethod.PUT,
				new HttpEntity<>(request),
				TaskResponseDto.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(createdTask.getId());
		assertThat(response.getBody().getTitle()).isEqualTo("After update");
		assertThat(response.getBody().isCompleted()).isTrue();
		assertThat(response.getBody().getPriority()).isEqualTo(Priority.HIGH);
	}

	@Test
	void updateTaskReturnsNotFoundWhenTaskDoesNotExist() {
		TaskUpdateDto request = new TaskUpdateDto();
		request.setTitle("Missing update");

		ResponseEntity<String> response = restTemplate.exchange(
				"/api/tasks/999999",
				HttpMethod.PUT,
				new HttpEntity<>(request),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).contains("Task not found");
	}

	@Test
	void deleteTaskReturnsNoContent() {
		TaskResponseDto createdTask = createTask("Delete me", "Positive delete test");

		ResponseEntity<Void> response = restTemplate.exchange(
				"/api/tasks/" + createdTask.getId(),
				HttpMethod.DELETE,
				HttpEntity.EMPTY,
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
		assertThat(restTemplate.getForEntity("/api/tasks/" + createdTask.getId(), String.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteTaskReturnsNotFoundWhenTaskDoesNotExist() {
		ResponseEntity<String> response = restTemplate.exchange(
				"/api/tasks/999999",
				HttpMethod.DELETE,
				HttpEntity.EMPTY,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).contains("Task not found");
	}

	@Test
	void statisticsReturnsRepositoryCounts() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/statistics", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Primary repository tasks:");
		assertThat(response.getBody()).contains("stub repository tasks: 2");
	}

	@Test
	void statisticsUnknownNestedPathReturnsNotFound() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/statistics/details", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).contains("\"status\":404");
	}

	private TaskResponseDto createTask(String title, String description) {
		ResponseEntity<TaskResponseDto> response = restTemplate.postForEntity(
				"/api/tasks",
				createRequest(title, description),
				TaskResponseDto.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

	private TaskCreateDto createRequest(String title, String description) {
		TaskCreateDto request = new TaskCreateDto();
		request.setTitle(title);
		request.setDescription(description);
		request.setDueDate(LocalDate.now().plusDays(1));
		request.setPriority(Priority.MEDIUM);
		request.setTags(Set.of("test"));
		return request;
	}
}
