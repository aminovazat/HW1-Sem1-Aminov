package com.azat.h1.external;

import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local emulator for an external task API.
 */
@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

	private final Map<Long, ExternalTaskResponse> tasks = new ConcurrentHashMap<>();
	private final AtomicLong idSequence = new AtomicLong();

	@Operation(summary = "Create an external task")
	@ApiResponse(responseCode = "201", description = "External task created")
	@PostMapping("/tasks")
	public ResponseEntity<ExternalTaskResponse> createTask(@RequestBody ExternalTaskRequest request) {
		Long id = idSequence.incrementAndGet();
		ExternalTaskResponse task = new ExternalTaskResponse(
				id,
				request.getTitle(),
				request.getDescription(),
				Boolean.TRUE.equals(request.getCompleted())
		);
		tasks.put(id, task);
		return ResponseEntity.created(URI.create("/external/v1/tasks/" + id)).body(task);
	}

	@Operation(summary = "Get an external task")
	@ApiResponse(responseCode = "200", description = "External task returned")
	@ApiResponse(responseCode = "404", description = "External task not found")
	@GetMapping("/tasks/{id}")
	public ResponseEntity<?> getTask(@PathVariable Long id) {
		ExternalTaskResponse task = tasks.get(id);
		if (task == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundProblem(id));
		}
		return ResponseEntity.ok(task);
	}

	@Operation(summary = "List external tasks")
	@ApiResponse(responseCode = "200", description = "External tasks returned")
	@GetMapping("/tasks")
	public ResponseEntity<List<ExternalTaskResponse>> getTasks(@RequestParam(required = false) Boolean completed,
			@RequestParam(required = false) Integer limit) {
		var stream = tasks.values().stream()
				.sorted(Comparator.comparing(ExternalTaskResponse::getId));
		if (completed != null) {
			stream = stream.filter(task -> task.isCompleted() == completed);
		}
		if (limit != null && limit >= 0) {
			stream = stream.limit(limit);
		}
		return ResponseEntity.ok(stream.toList());
	}

	@Operation(summary = "Delete an external task")
	@ApiResponse(responseCode = "204", description = "External task deleted")
	@ApiResponse(responseCode = "404", description = "External task not found")
	@DeleteMapping("/tasks/{id}")
	public ResponseEntity<ProblemDetail> deleteTask(@PathVariable Long id) {
		if (tasks.remove(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundProblem(id));
		}
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Simulate unstable external API behavior")
	@ApiResponse(responseCode = "200", description = "Stable response")
	@GetMapping("/unstable")
	public ResponseEntity<?> unstable(@RequestParam(defaultValue = "500") String mode) throws InterruptedException {
		return switch (mode) {
			case "timeout" -> {
				Thread.sleep(2_000);
				yield ResponseEntity.ok(Map.of("message", "slow response"));
			}
			case "429" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.header(HttpHeaders.RETRY_AFTER, "2")
					.body(problem(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"));
			case "html" -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
					.contentType(MediaType.TEXT_HTML)
					.body("<html><body>External gateway error</body></html>");
			case "500" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "External server error"));
			default -> ResponseEntity.ok(Map.of("message", "stable response"));
		};
	}

	private ProblemDetail notFoundProblem(Long id) {
		return problem(HttpStatus.NOT_FOUND, "External task not found: " + id);
	}

	private ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(status.getReasonPhrase());
		return problemDetail;
	}
}
