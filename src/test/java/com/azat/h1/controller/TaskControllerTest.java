package com.azat.h1.controller;

import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.exception.GlobalExceptionHandler;
import com.azat.h1.mapper.TaskMapper;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.security.JwtUtils;
import com.azat.h1.service.TaskService;
import com.azat.h1.service.TaskStatisticsJdbcService;
import com.azat.h1.service.TaskStatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private TaskStatisticsService taskStatisticsService;

	@MockitoBean
	private TaskStatisticsJdbcService taskStatisticsJdbcService;

	@MockitoBean
	private TaskMapper taskMapper;

	@MockitoBean
	private JwtUtils jwtUtils;

	@Test
	void createTaskReturnsCreatedTaskJson() throws Exception {
		TaskCreateDto request = taskCreateDto("Write slice tests", "Verify controller JSON");
		Task taskToCreate = task(1L, request.getTitle(), request.getDescription(), false);
		Task savedTask = task(10L, request.getTitle(), request.getDescription(), false);
		TaskResponseDto response = taskResponseDto(savedTask);

		when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(taskToCreate);
		when(taskService.createTask(taskToCreate)).thenReturn(savedTask);
		when(taskMapper.toResponseDto(savedTask)).thenReturn(response);

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.title").value("Write slice tests"))
				.andExpect(jsonPath("$.description").value("Verify controller JSON"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.priority").value("MEDIUM"))
				.andExpect(jsonPath("$.dueDate").value(request.getDueDate().toString()))
				.andExpect(jsonPath("$.tags[0]").value("slice"));
	}

	@Test
	void getTaskByIdReturnsExistingTaskJson() throws Exception {
		Task task = task(5L, "Existing task", "Returned from mocked service", false);
		TaskResponseDto response = taskResponseDto(task);

		when(taskService.getTaskOrThrow(5L)).thenReturn(task);
		when(taskMapper.toResponseDto(task)).thenReturn(response);

		mockMvc.perform(get("/api/tasks/{id}", 5L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.title").value("Existing task"))
				.andExpect(jsonPath("$.description").value("Returned from mocked service"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.priority").value("MEDIUM"));
	}

	@Test
	void createTaskWithBlankTitleReturnsBadRequest() throws Exception {
		TaskCreateDto request = taskCreateDto("", "Invalid title");

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.details.title").exists());
	}

	private TaskCreateDto taskCreateDto(String title, String description) {
		TaskCreateDto request = new TaskCreateDto();
		request.setTitle(title);
		request.setDescription(description);
		request.setDueDate(LocalDate.now().plusDays(1));
		request.setPriority(Priority.MEDIUM);
		request.setTags(Set.of("slice"));
		return request;
	}

	private Task task(Long id, String title, String description, boolean completed) {
		return new Task(id, title, description, completed, LocalDateTime.now(), LocalDate.now().plusDays(1),
				Priority.MEDIUM, Set.of("slice"));
	}

	private TaskResponseDto taskResponseDto(Task task) {
		TaskResponseDto response = new TaskResponseDto();
		response.setId(task.getId());
		response.setTitle(task.getTitle());
		response.setDescription(task.getDescription());
		response.setCompleted(task.isCompleted());
		response.setCreatedAt(task.getCreatedAt());
		response.setDueDate(task.getDueDate());
		response.setPriority(task.getPriority());
		response.setTags(task.getTags());
		return response;
	}
}
