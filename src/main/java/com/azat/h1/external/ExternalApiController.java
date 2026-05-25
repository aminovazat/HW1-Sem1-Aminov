package com.azat.h1.external;

import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
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
 * Emulates an external HTTP API used by the gateway layer.
 */
@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

	private final Map<Long, ExternalTaskResponse> tasks = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@PostMapping("/tasks")
	public ResponseEntity<ExternalTaskResponse> createTask(@RequestBody ExternalTaskRequest request) {
		Long id = idGenerator.getAndIncrement();
		ExternalTaskResponse response = new ExternalTaskResponse(
				id,
				request.getTitle(),
				request.getDescription(),
				Boolean.TRUE.equals(request.getCompleted())
		);
		tasks.put(id, response);
		return ResponseEntity.created(URI.create("/external/v1/tasks/" + id)).body(response);
	}

	@GetMapping("/tasks/{id}")
	public ResponseEntity<?> getTask(@PathVariable Long id) {
		ExternalTaskResponse response = tasks.get(id);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem(HttpStatus.NOT_FOUND, "External task not found: " + id));
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/tasks")
	public ResponseEntity<List<ExternalTaskResponse>> getTasks(
			@RequestParam(required = false) Boolean completed,
			@RequestParam(required = false) Integer limit) {
		List<ExternalTaskResponse> response = tasks.values().stream()
				.filter(task -> completed == null || task.isCompleted() == completed)
				.sorted(Comparator.comparing(ExternalTaskResponse::getId))
				.limit(limit == null ? Long.MAX_VALUE : Math.max(limit, 0))
				.toList();
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/tasks/{id}")
	public ResponseEntity<?> deleteTask(@PathVariable Long id) {
		if (tasks.remove(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem(HttpStatus.NOT_FOUND, "External task not found: " + id));
		}
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/unstable")
	public ResponseEntity<?> unstable(@RequestParam String mode) throws InterruptedException {
		return switch (mode) {
			case "timeout" -> {
				Thread.sleep(2500);
				yield ResponseEntity.ok("slow response");
			}
			case "500" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "External API failure"));
			case "429" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.header(HttpHeaders.RETRY_AFTER, "3")
					.body(problem(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"));
			case "html" -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
					.contentType(MediaType.TEXT_HTML)
					.body("<html><body><h1>Bad Gateway</h1><p>External HTML error</p></body></html>");
			default -> ResponseEntity.ok("stable response");
		};
	}

	private ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(status.getReasonPhrase());
		return problemDetail;
	}
}
