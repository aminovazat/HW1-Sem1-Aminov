package com.azat.h1.api;

import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import com.azat.h1.service.TasksGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Protected gateway endpoints that proxy task operations to the external API.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TasksGatewayController {

	private final TasksGatewayService tasksGatewayService;

	public TasksGatewayController(TasksGatewayService tasksGatewayService) {
		this.tasksGatewayService = tasksGatewayService;
	}

	@Operation(summary = "Create a task through the external gateway")
	@ApiResponse(responseCode = "201", description = "External task created")
	@PostMapping
	public ResponseEntity<ExternalTaskResponse> createTask(@RequestBody ExternalTaskRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tasksGatewayService.createTask(request));
	}

	@Operation(summary = "Get an external task through the gateway")
	@ApiResponse(responseCode = "200", description = "External task returned")
	@GetMapping("/{id}")
	public ResponseEntity<ExternalTaskResponse> getTask(@PathVariable Long id) {
		return ResponseEntity.ok(tasksGatewayService.getTask(id));
	}

	@Operation(summary = "List external tasks through the gateway")
	@ApiResponse(responseCode = "200", description = "External tasks returned")
	@GetMapping
	public ResponseEntity<List<ExternalTaskResponse>> getTasks(@RequestParam(required = false) Boolean completed,
			@RequestParam(required = false) Integer limit) {
		return ResponseEntity.ok(tasksGatewayService.getTasks(completed, limit));
	}

	@Operation(summary = "Delete an external task through the gateway")
	@ApiResponse(responseCode = "204", description = "External task deleted")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
		tasksGatewayService.deleteTask(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Call unstable external endpoint")
	@ApiResponse(responseCode = "200", description = "External unstable call completed or degraded gracefully")
	@GetMapping("/unstable")
	public ResponseEntity<String> callUnstable(@RequestParam(defaultValue = "500") String mode) {
		return ResponseEntity.ok(tasksGatewayService.callUnstable(mode));
	}
}
