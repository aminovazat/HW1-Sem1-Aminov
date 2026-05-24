package com.azat.h1.controller;

import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.exception.TaskNotFoundException;
import com.azat.h1.mapper.TaskMapper;
import com.azat.h1.model.Task;
import com.azat.h1.service.TaskService;
import com.azat.h1.service.TaskStatisticsService;
import com.azat.h1.validation.OnCreate;
import com.azat.h1.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes REST endpoints for task management.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;
	private final TaskStatisticsService taskStatisticsService;
	private final TaskMapper taskMapper;

	public TaskController(TaskService taskService, TaskStatisticsService taskStatisticsService, TaskMapper taskMapper) {
		this.taskService = taskService;
		this.taskStatisticsService = taskStatisticsService;
		this.taskMapper = taskMapper;
	}

	@Operation(summary = "Get all tasks")
	@ApiResponse(responseCode = "200", description = "Tasks returned")
	@GetMapping
	public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
		List<TaskResponseDto> tasks = taskService.getAllTasks().stream()
				.map(taskMapper::toResponseDto)
				.toList();
		return ResponseEntity.ok()
				.header("X-Total-Count", String.valueOf(tasks.size()))
				.body(tasks);
	}

	@Operation(summary = "Compare primary and stub repositories")
	@GetMapping("/statistics")
	public ResponseEntity<String> getStatistics() {
		return ResponseEntity.ok(taskStatisticsService.compareRepositories());
	}

	@Operation(summary = "Get task by id")
	@ApiResponse(responseCode = "200", description = "Task found")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@GetMapping("/{id}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
		return ResponseEntity.ok(taskMapper.toResponseDto(taskService.getRequiredTask(id)));
	}

	@Operation(summary = "Create task")
	@ApiResponse(responseCode = "201", description = "Task created")
	@PostMapping
	public ResponseEntity<TaskResponseDto> createTask(@RequestBody @Validated(OnCreate.class) TaskCreateDto task) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(taskMapper.toResponseDto(taskService.createTask(taskMapper.toEntity(task))));
	}

	@Operation(summary = "Update task")
	@ApiResponse(responseCode = "200", description = "Task updated")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@PutMapping("/{id}")
	public ResponseEntity<TaskResponseDto> updateTask(
			@PathVariable Long id,
			@RequestBody @Validated(OnUpdate.class) TaskUpdateDto task) {
		Task taskToUpdate = copyTask(taskService.getRequiredTask(id));
		taskMapper.updateEntity(task, taskToUpdate);
		return taskService.updateTask(id, taskToUpdate)
				.map(taskMapper::toResponseDto)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Delete task")
	@ApiResponse(responseCode = "204", description = "Task deleted")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
		if (taskService.deleteTask(id)) {
			return ResponseEntity.noContent().build();
		}

		throw new TaskNotFoundException(id);
	}

	private Task copyTask(Task task) {
		return new Task(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.isCompleted(),
				task.getCreatedAt(),
				task.getDueDate(),
				task.getPriority(),
				task.getTags()
		);
	}
}
