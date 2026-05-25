package com.azat.h1.service;

import com.azat.h1.client.ExternalTasksClient;
import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import com.azat.h1.exception.ExternalTaskNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gateway service that delegates task operations to the external API client.
 */
@Service
public class TasksGatewayService {

	private final ExternalTasksClient externalTasksClient;

	public TasksGatewayService(ExternalTasksClient externalTasksClient) {
		this.externalTasksClient = externalTasksClient;
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "createTaskRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "createTaskCircuitBreakerFallback")
	public ExternalTaskResponse createTask(ExternalTaskRequest request) {
		return externalTasksClient.createTask(request);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "getTaskRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "getTaskCircuitBreakerFallback")
	@Retry(name = "externalApi")
	public ExternalTaskResponse getTask(Long id) {
		return externalTasksClient.getTask(id);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "getTasksRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "getTasksCircuitBreakerFallback")
	public List<ExternalTaskResponse> getTasks(Boolean completed, Integer limit) {
		return externalTasksClient.getTasks(completed, limit);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "deleteTaskRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "deleteTaskCircuitBreakerFallback")
	public void deleteTask(Long id) {
		externalTasksClient.deleteTask(id);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "callUnstableRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "callUnstableCircuitBreakerFallback")
	@Retry(name = "externalApi")
	public String callUnstable(String mode) {
		return externalTasksClient.callUnstable(mode);
	}

	private ExternalTaskResponse createTaskRateLimitFallback(ExternalTaskRequest request, Throwable ex) {
		return fallbackTask(-1L, "Rate limit fallback task", "External task creation is temporarily limited");
	}

	private ExternalTaskResponse createTaskCircuitBreakerFallback(ExternalTaskRequest request, Throwable ex) {
		return fallbackTask(-1L, "Circuit breaker fallback task", "External task creation is temporarily unavailable");
	}

	private ExternalTaskResponse getTaskRateLimitFallback(Long id, Throwable ex) {
		rethrowNotFound(ex);
		return fallbackTask(id, "Rate limit fallback task", "External task lookup is temporarily limited");
	}

	private ExternalTaskResponse getTaskCircuitBreakerFallback(Long id, Throwable ex) {
		rethrowNotFound(ex);
		return fallbackTask(id, "Circuit breaker fallback task", "External task lookup is temporarily unavailable");
	}

	private List<ExternalTaskResponse> getTasksRateLimitFallback(Boolean completed, Integer limit, Throwable ex) {
		return List.of(fallbackTask(-1L, "Rate limit fallback task", "External task list is temporarily limited"));
	}

	private List<ExternalTaskResponse> getTasksCircuitBreakerFallback(Boolean completed, Integer limit, Throwable ex) {
		return List.of(fallbackTask(-1L, "Circuit breaker fallback task", "External task list is temporarily unavailable"));
	}

	private void deleteTaskRateLimitFallback(Long id, Throwable ex) {
		rethrowNotFound(ex);
	}

	private void deleteTaskCircuitBreakerFallback(Long id, Throwable ex) {
		rethrowNotFound(ex);
	}

	private String callUnstableRateLimitFallback(String mode, Throwable ex) {
		return "Graceful degradation: external API rate limit reached for mode " + mode;
	}

	private String callUnstableCircuitBreakerFallback(String mode, Throwable ex) {
		return "Graceful degradation: external API is temporarily unavailable for mode " + mode;
	}

	private ExternalTaskResponse fallbackTask(Long id, String title, String description) {
		return new ExternalTaskResponse(id, title, description, false);
	}

	private void rethrowNotFound(Throwable ex) {
		if (ex instanceof ExternalTaskNotFoundException notFoundException) {
			throw notFoundException;
		}
	}
}
