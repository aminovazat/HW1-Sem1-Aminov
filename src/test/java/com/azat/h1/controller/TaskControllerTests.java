package com.azat.h1.controller;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.dto.ErrorResponse;
import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void getAllTasksReturnsTasksAndTotalCountHeader() {
		ResponseEntity<TaskResponseDto[]> response = restTemplate.getForEntity("/api/tasks", TaskResponseDto[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst("X-Total-Count")).isNotBlank();
		assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).extracting(TaskResponseDto::getTitle)
				.contains("Learn Spring", "Write Tests", "Finish Homework");
	}

	@Test
	void getAllTasksWithUnsupportedAcceptHeaderReturnsNotAcceptable() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_XML_VALUE));

		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/tasks",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
		assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
	}

	@Test
	void getTaskByIdReturnsTask() {
		TaskResponseDto createdTask = createTask("Get by id", "Positive get by id test");

		ResponseEntity<TaskResponseDto> response = restTemplate.getForEntity(
				"/api/tasks/" + createdTask.getId(),
				TaskResponseDto.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(createdTask.getId());
		assertThat(response.getBody().getTitle()).isEqualTo("Get by id");
	}

	@Test
	void getTaskByIdReturnsStructuredNotFoundWhenTaskDoesNotExist() {
		ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/tasks/999999", ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(404);
		assertThat(response.getBody().getMessage()).contains("Task not found");
	}

	@Test
	void createTaskReturnsCreatedTask() {
		TaskCreateDto request = taskCreateDto("Created task", "Created from controller test");

		ResponseEntity<TaskResponseDto> response = restTemplate.postForEntity("/api/tasks", request,
				TaskResponseDto.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isNotNull();
		assertThat(response.getBody().getCreatedAt()).isNotNull();
		assertThat(response.getBody().getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(response.getBody().getTitle()).isEqualTo("Created task");
	}

	@Test
	void createTaskWithInvalidTitleReturnsBadRequestWithDetails() {
		TaskCreateDto request = taskCreateDto("", "Invalid title");

		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/tasks", request,
				ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getDetails()).containsKey("title");
	}

	@Test
	void createTaskWithTooManyTagsReturnsBadRequestWithDetails() {
		TaskCreateDto request = taskCreateDto("Too many tags", "Invalid tags");
		request.setTags(Set.of("one", "two", "three", "four", "five", "six"));

		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/tasks", request,
				ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getDetails()).containsKey("tags");
	}

	@Test
	void createTaskWithPastDueDateReturnsBadRequestWithDetails() {
		TaskCreateDto request = taskCreateDto("Past due date", "Invalid date");
		request.setDueDate(LocalDate.now().minusDays(1));

		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/tasks", request,
				ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getDetails()).containsKey("dueDate");
	}

	@Test
	void createTaskWithMalformedJsonReturnsBadRequest() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>("{bad json", headers);

		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/tasks", request, ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(400);
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
	}

	@Test
	void updateTaskWithInvalidTitleReturnsBadRequest() {
		TaskResponseDto createdTask = createTask("Valid update target", "Will be updated");
		TaskUpdateDto request = new TaskUpdateDto();
		request.setTitle("no");

		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/tasks/" + createdTask.getId(),
				HttpMethod.PUT,
				new HttpEntity<>(request),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getDetails()).containsKey("title");
	}

	@Test
	void updateTaskReturnsNotFoundWhenTaskDoesNotExist() {
		TaskUpdateDto request = new TaskUpdateDto();
		request.setTitle("Missing update");

		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/tasks/999999",
				HttpMethod.PUT,
				new HttpEntity<>(request),
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Task not found");
	}

	@Test
	void deleteTaskReturnsNoContent() {
		TaskResponseDto createdTask = createTask("Delete me", "Positive delete test");
		HttpEntity<Void> request = new HttpEntity<>(null);

		ResponseEntity<Void> response = restTemplate.exchange(
				"/api/tasks/" + createdTask.getId(),
				HttpMethod.DELETE,
				request,
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(restTemplate.getForEntity("/api/tasks/" + createdTask.getId(), ErrorResponse.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteTaskReturnsNotFoundWhenTaskDoesNotExist() {
		HttpEntity<Void> request = new HttpEntity<>(null);

		ResponseEntity<ErrorResponse> response = restTemplate.exchange(
				"/api/tasks/999999",
				HttpMethod.DELETE,
				request,
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(404);
	}

	@Test
	void statisticsAndScopeEndpointsReturnDemoValues() {
		ResponseEntity<String> statistics = restTemplate.getForEntity("/api/tasks/statistics", String.class);
		ResponseEntity<String> scope = restTemplate.getForEntity("/api/tasks/scope", String.class);

		assertThat(statistics.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(statistics.getBody()).contains("Primary repository tasks:");
		assertThat(scope.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(scope.getBody()).contains("requestId=", "prototypeTaskId=");
	}

	@Test
	void attachmentUploadListDownloadAndDeleteWork() {
		TaskResponseDto task = createTask("Attachment task", "Task with file");
		AttachmentResponseDto attachment = uploadAttachment(task.getId(), "hello.txt", "hello".getBytes());

		ResponseEntity<AttachmentResponseDto[]> listResponse = restTemplate.getForEntity(
				"/api/tasks/" + task.getId() + "/attachments",
				AttachmentResponseDto[].class
		);
		assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(listResponse.getBody()).isNotNull();
		assertThat(listResponse.getBody()).hasSize(1);

		ResponseEntity<byte[]> downloadResponse = restTemplate.getForEntity(
				"/api/attachments/" + attachment.getId(),
				byte[].class
		);
		assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(downloadResponse.getBody()).isEqualTo("hello".getBytes());
		assertThat(downloadResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("hello.txt");

		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				"/api/attachments/" + attachment.getId(),
				HttpMethod.DELETE,
				new HttpEntity<>(null),
				Void.class
		);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void attachmentUploadForMissingTaskReturnsNotFound() {
		ResponseEntity<ErrorResponse> response = uploadAttachmentExpectingError(999999L, "missing.txt", "data".getBytes());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Task not found");
	}

	@Test
	void attachmentDownloadMissingReturnsNotFound() {
		ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/attachments/999999",
				ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Attachment not found");
	}

	@Test
	void favoritesAddGetAndDeleteWorkWithSession() {
		TaskResponseDto task = createTask("Favorite task", "Task to favorite");

		ResponseEntity<Void> addResponse = restTemplate.postForEntity("/api/favorites/" + task.getId(), null,
				Void.class);
		assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		HttpHeaders sessionHeaders = new HttpHeaders();
		sessionHeaders.set(HttpHeaders.COOKIE, toCookieHeader(addResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
		ResponseEntity<TaskResponseDto[]> getResponse = restTemplate.exchange(
				"/api/favorites",
				HttpMethod.GET,
				new HttpEntity<>(sessionHeaders),
				TaskResponseDto[].class
		);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody()).extracting(TaskResponseDto::getId).contains(task.getId());

		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				"/api/favorites/" + task.getId(),
				HttpMethod.DELETE,
				new HttpEntity<>(sessionHeaders),
				Void.class
		);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void favoriteAddMissingTaskReturnsNotFound() {
		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/favorites/999999", null,
				ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).contains("Task not found");
	}

	@Test
	void preferencesGetDefaultAndSetValidMode() {
		ResponseEntity<String> defaultResponse = restTemplate.getForEntity("/api/preferences/view", String.class);
		assertThat(defaultResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(defaultResponse.getBody()).isEqualTo("detailed");

		ResponseEntity<String> setResponse = restTemplate.postForEntity(
				"/api/preferences/view?mode=compact",
				null,
				String.class
		);
		assertThat(setResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(setResponse.getBody()).isEqualTo("compact");
		assertThat(setResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("viewPreference=compact");
	}

	@Test
	void preferencesSetInvalidModeReturnsBadRequest() {
		ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
				"/api/preferences/view?mode=wide",
				null,
				ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(400);
	}

	private TaskResponseDto createTask(String title, String description) {
		ResponseEntity<TaskResponseDto> response = restTemplate.postForEntity(
				"/api/tasks",
				taskCreateDto(title, description),
				TaskResponseDto.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

	private TaskCreateDto taskCreateDto(String title, String description) {
		TaskCreateDto request = new TaskCreateDto();
		request.setTitle(title);
		request.setDescription(description);
		request.setDueDate(LocalDate.now().plusDays(1));
		request.setPriority(Priority.MEDIUM);
		request.setTags(Set.of("test"));
		return request;
	}

	private AttachmentResponseDto uploadAttachment(Long taskId, String fileName, byte[] content) {
		ResponseEntity<AttachmentResponseDto> response = restTemplate.postForEntity(
				"/api/tasks/" + taskId + "/attachments",
				multipartRequest(fileName, content),
				AttachmentResponseDto.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

	private ResponseEntity<ErrorResponse> uploadAttachmentExpectingError(Long taskId, String fileName, byte[] content) {
		return restTemplate.postForEntity(
				"/api/tasks/" + taskId + "/attachments",
				multipartRequest(fileName, content),
				ErrorResponse.class
		);
	}

	private HttpEntity<MultiValueMap<String, Object>> multipartRequest(String fileName, byte[] content) {
		ByteArrayResource resource = new ByteArrayResource(content) {
			@Override
			public String getFilename() {
				return fileName;
			}
		};
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", resource);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		return new HttpEntity<>(body, headers);
	}

	private String toCookieHeader(String setCookieHeader) {
		assertThat(setCookieHeader).isNotBlank();
		return setCookieHeader.split(";", 2)[0];
	}
}
