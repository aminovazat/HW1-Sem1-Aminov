package com.azat.h1.service;

import com.azat.h1.client.ExternalTasksClient;
import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import com.azat.h1.exception.ExternalApiException;
import com.azat.h1.exception.ExternalRateLimitException;
import com.azat.h1.exception.ExternalTaskNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gateway service that protects calls to the external task API.
 */
@Service
public class TasksGatewayService {

	private final ExternalTasksClient externalTasksClient;

	public TasksGatewayService(ExternalTasksClient externalTasksClient) {
		this.externalTasksClient = externalTasksClient;
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "createTaskRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "createTaskFallback")
	public ExternalTaskResponse createTask(ExternalTaskRequest request) {
		return externalTasksClient.createTask(request);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "getTaskRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "getTaskFallback")
	public ExternalTaskResponse getTask(Long id) {
		return externalTasksClient.getTask(id);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "getTasksRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "getTasksFallback")
	public List<ExternalTaskResponse> getTasks(Boolean completed, Integer limit) {
		return externalTasksClient.getTasks(completed, limit);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "deleteTaskRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "deleteTaskFallback")
	public void deleteTask(Long id) {
		externalTasksClient.deleteTask(id);
	}

	@RateLimiter(name = "externalApi", fallbackMethod = "callUnstableRateLimitFallback")
	@CircuitBreaker(name = "externalApi", fallbackMethod = "callUnstableFallback")
	public String callUnstable(String mode) {
		return externalTasksClient.callUnstable(mode);
	}

	public ExternalTaskResponse createTaskFallback(ExternalTaskRequest request, Throwable throwable) {
		rethrowControlledException(throwable);
		return new ExternalTaskResponse(-1L, "Fallback task", "External API is temporarily unavailable", false);
	}

	public ExternalTaskResponse getTaskFallback(Long id, Throwable throwable) {
		rethrowControlledException(throwable);
		return new ExternalTaskResponse(id, "Fallback task", "External API is temporarily unavailable", false);
	}

	public List<ExternalTaskResponse> getTasksFallback(Boolean completed, Integer limit, Throwable throwable) {
		rethrowControlledException(throwable);
		return List.of();
	}

	public void deleteTaskFallback(Long id, Throwable throwable) {
		rethrowControlledException(throwable);
		throw new ExternalApiException("External API delete is temporarily unavailable", throwable);
	}

	public String callUnstableFallback(String mode, Throwable throwable) {
		if (throwable instanceof ExternalRateLimitException rateLimitException) {
			throw rateLimitException;
		}
		return "Graceful degradation: external API is temporarily unavailable";
	}

	public ExternalTaskResponse createTaskRateLimitFallback(ExternalTaskRequest request, RequestNotPermitted exception) {
		throw new ExternalRateLimitException("Gateway rate limit exceeded");
	}

	public ExternalTaskResponse getTaskRateLimitFallback(Long id, RequestNotPermitted exception) {
		throw new ExternalRateLimitException("Gateway rate limit exceeded");
	}

	public List<ExternalTaskResponse> getTasksRateLimitFallback(Boolean completed, Integer limit,
			RequestNotPermitted exception) {
		throw new ExternalRateLimitException("Gateway rate limit exceeded");
	}

	public void deleteTaskRateLimitFallback(Long id, RequestNotPermitted exception) {
		throw new ExternalRateLimitException("Gateway rate limit exceeded");
	}

	public String callUnstableRateLimitFallback(String mode, RequestNotPermitted exception) {
		throw new ExternalRateLimitException("Gateway rate limit exceeded");
	}

	private void rethrowControlledException(Throwable throwable) {
		if (throwable instanceof ExternalTaskNotFoundException notFoundException) {
			throw notFoundException;
		}
		if (throwable instanceof ExternalRateLimitException rateLimitException) {
			throw rateLimitException;
		}
		if (throwable instanceof RequestNotPermitted) {
			throw new ExternalRateLimitException("Gateway rate limit exceeded");
		}
	}
}
