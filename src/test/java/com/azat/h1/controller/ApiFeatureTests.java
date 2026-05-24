package com.azat.h1.controller;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.model.Priority;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFeatureTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void validationRejectsBlankTitle() throws Exception {
		TaskCreateDto dto = validCreateDto();
		dto.setTitle("");

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.details.title").exists());
	}

	@Test
	void validationRejectsShortTitle() throws Exception {
		TaskCreateDto dto = validCreateDto();
		dto.setTitle("Hi");

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.details.title").exists());
	}

	@Test
	void validationRejectsPastDueDate() throws Exception {
		TaskCreateDto dto = validCreateDto();
		dto.setDueDate(LocalDate.now().minusDays(1));

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.details.dueDate").exists());
	}

	@Test
	void validationRejectsTooManyTags() throws Exception {
		TaskCreateDto dto = validCreateDto();
		dto.setTags(Set.of("one", "two", "three", "four", "five", "six"));

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.details.tags").exists());
	}

	@Test
	void uploadDownloadAndDeleteAttachment() throws Exception {
		TaskResponseDto task = createTask("Attachment task");
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"note.txt",
				MediaType.TEXT_PLAIN_VALUE,
				"hello attachment".getBytes()
		);

		String uploadBody = mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fileName").value("note.txt"))
				.andExpect(jsonPath("$.size").value(16))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AttachmentResponseDto attachment = objectMapper.readValue(uploadBody, AttachmentResponseDto.class);

		mockMvc.perform(get("/api/tasks/{taskId}/attachments", task.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(attachment.getId()));

		mockMvc.perform(get("/api/attachments/{attachmentId}", attachment.getId()))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", containsString("note.txt")))
				.andExpect(content().string("hello attachment"));

		mockMvc.perform(delete("/api/attachments/{attachmentId}", attachment.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/attachments/{attachmentId}", attachment.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void uploadAttachmentForMissingTaskReturnsNotFound() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "missing.txt", MediaType.TEXT_PLAIN_VALUE, "x".getBytes());

		mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 999999L).file(file))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Task not found: 999999"));
	}

	@Test
	void favoritesUseHttpSession() throws Exception {
		TaskResponseDto task = createTask("Favorite task");
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/api/favorites/{taskId}", task.getId()).session(session))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/favorites").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(task.getId()))
				.andExpect(jsonPath("$[0].title").value("Favorite task"));

		mockMvc.perform(delete("/api/favorites/{taskId}", task.getId()).session(session))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/favorites").session(session))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void preferencesReadDefaultAndSetCookie() throws Exception {
		mockMvc.perform(get("/api/preferences/view"))
				.andExpect(status().isOk())
				.andExpect(content().string("compact"));

		mockMvc.perform(post("/api/preferences/view").param("mode", "detailed"))
				.andExpect(status().isOk())
				.andExpect(content().string("detailed"))
				.andExpect(cookie().value("viewPreference", "detailed"));

		mockMvc.perform(get("/api/preferences/view").cookie(new Cookie("viewPreference", "detailed")))
				.andExpect(status().isOk())
				.andExpect(content().string("detailed"));
	}

	@Test
	void globalExceptionHandlerReturnsErrorResponse() throws Exception {
		mockMvc.perform(get("/api/tasks/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Task not found: 999999"))
				.andExpect(jsonPath("$.path").value("/api/tasks/999999"));
	}

	@Test
	void missingRequestParameterReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/preferences/view"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void getTasksIncludesRequiredHeaders() throws Exception {
		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(header().exists("X-Total-Count"))
				.andExpect(header().string("X-API-Version", "2.0.0"));
	}

	@Test
	void updateRejectsDueDateBeforeCreationDate() throws Exception {
		TaskResponseDto task = createTask("Due date task");
		TaskUpdateDto dto = new TaskUpdateDto();
		dto.setDueDate(LocalDate.now().minusDays(1));

		mockMvc.perform(put("/api/tasks/{id}", task.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	private TaskResponseDto createTask(String title) throws Exception {
		String body = mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validCreateDto(title))))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readValue(body, TaskResponseDto.class);
	}

	private TaskCreateDto validCreateDto() {
		return validCreateDto("Valid task");
	}

	private TaskCreateDto validCreateDto(String title) {
		TaskCreateDto dto = new TaskCreateDto();
		dto.setTitle(title);
		dto.setDescription("Valid description");
		dto.setDueDate(LocalDate.now().plusDays(1));
		dto.setPriority(Priority.MEDIUM);
		dto.setTags(Set.of("test"));
		return dto;
	}
}
