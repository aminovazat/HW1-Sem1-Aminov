package com.azat.h1.controller;

import com.azat.h1.dto.BulkCompleteRequestDto;
import com.azat.h1.dto.PriorityTaskCountDto;
import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.mapper.TaskMapper;
import com.azat.h1.model.Task;
import com.azat.h1.service.TaskService;
import com.azat.h1.service.TaskStatisticsJdbcService;
import com.azat.h1.service.TaskStatisticsService;
import com.azat.h1.validation.OnCreate;
import com.azat.h1.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
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
	private final TaskStatisticsJdbcService taskStatisticsJdbcService;
	private final TaskMapper taskMapper;

	public TaskController(TaskService taskService, TaskStatisticsService taskStatisticsService,
			TaskStatisticsJdbcService taskStatisticsJdbcService, TaskMapper taskMapper) {
		this.taskService = taskService;
		this.taskStatisticsService = taskStatisticsService;
		this.taskStatisticsJdbcService = taskStatisticsJdbcService;
		this.taskMapper = taskMapper;
	}

	@Operation(summary = "Get all tasks")
	@ApiResponse(responseCode = "200", description = "Tasks returned")
	@GetMapping
	public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
		List<TaskResponseDto> tasks = taskMapper.toResponseDtos(taskService.getAllTasks());
		return ResponseEntity.ok()
				.header("X-Total-Count", String.valueOf(tasks.size()))
				.body(tasks);
	}

	@Operation(summary = "Compare primary and stub repositories")
	@ApiResponse(responseCode = "200", description = "Statistics returned")
	@GetMapping("/statistics")
	public ResponseEntity<String> getStatistics() {
		return ResponseEntity.ok(taskStatisticsService.compareRepositories());
	}

	@Operation(summary = "Show request and prototype scope details")
	@ApiResponse(responseCode = "200", description = "Scope details returned")
	@GetMapping("/scope")
	public ResponseEntity<String> getScopeDetails() {
		return ResponseEntity.ok(taskStatisticsService.getScopeDetails());
	}

	@Operation(summary = "Get task counts grouped by priority")
	@ApiResponse(responseCode = "200", description = "Priority statistics returned")
	@GetMapping("/statistics/priority")
	public ResponseEntity<List<PriorityTaskCountDto>> getPriorityStatistics() {
		return ResponseEntity.ok(taskStatisticsJdbcService.getTasksCountByPriority());
	}

	@Operation(summary = "Get tasks due within the next seven days")
	@ApiResponse(responseCode = "200", description = "Due soon tasks returned")
	@GetMapping("/due-soon")
	public ResponseEntity<List<TaskResponseDto>> getTasksDueWithinNextSevenDays() {
		return ResponseEntity.ok(taskMapper.toResponseDtos(taskService.getTasksDueWithinNextSevenDays()));
	}

	@Operation(summary = "Get tasks with attachments loaded")
	@ApiResponse(responseCode = "200", description = "Tasks returned with attachments loaded")
	@GetMapping("/with-attachments")
	public ResponseEntity<List<TaskResponseDto>> getTasksWithAttachments() {
		return ResponseEntity.ok(taskMapper.toResponseDtos(taskService.getTasksWithAttachments()));
	}

	@Operation(summary = "Complete several tasks transactionally")
	@ApiResponse(responseCode = "200", description = "Tasks completed")
	@ApiResponse(responseCode = "400", description = "At least one task was not found")
	@PostMapping("/bulk-complete")
	public ResponseEntity<List<TaskResponseDto>> bulkCompleteTasks(@Valid @RequestBody BulkCompleteRequestDto request) {
		return ResponseEntity.ok(taskMapper.toResponseDtos(taskService.bulkCompleteTasks(request.getIds())));
	}

	@Operation(summary = "Get a task by id")
	@ApiResponse(responseCode = "200", description = "Task found")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@GetMapping("/{id}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
		return ResponseEntity.ok(taskMapper.toResponseDto(taskService.getTaskOrThrow(id)));
	}

	@Operation(summary = "Create a task")
	@ApiResponse(responseCode = "201", description = "Task created")
	@ApiResponse(responseCode = "400", description = "Request validation failed")
	@PostMapping
	public ResponseEntity<TaskResponseDto> createTask(@Validated(OnCreate.class) @RequestBody TaskCreateDto taskDto) {
		Task createdTask = taskService.createTask(taskMapper.toEntity(taskDto));
		return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toResponseDto(createdTask));
	}

	@Operation(summary = "Update a task")
	@ApiResponse(responseCode = "200", description = "Task updated")
	@ApiResponse(responseCode = "400", description = "Request validation failed")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@PutMapping("/{id}")
	public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id,
			@Validated(OnUpdate.class) @RequestBody TaskUpdateDto taskDto) {
		Task task = taskService.getTaskOrThrow(id);
		taskService.validateUpdate(task, taskDto);
		taskMapper.updateEntity(taskDto, task);
		return ResponseEntity.ok(taskMapper.toResponseDto(taskService.updateExistingTask(id, task)));
	}

	@Operation(summary = "Delete a task")
	@ApiResponse(responseCode = "204", description = "Task deleted")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
		taskService.getTaskOrThrow(id);
		taskService.deleteTask(id);
		return ResponseEntity.noContent().build();
	}
}
