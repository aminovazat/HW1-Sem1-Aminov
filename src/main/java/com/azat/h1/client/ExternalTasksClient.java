package com.azat.h1.client;

import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.azat.h1.dto.gateway.ExternalTaskResponse;
import com.azat.h1.exception.ExternalApiException;
import com.azat.h1.exception.ExternalTaskNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RestClient adapter for the external task API.
 */
@Component
public class ExternalTasksClient {

	private static final Logger logger = LoggerFactory.getLogger(ExternalTasksClient.class);
	private static final int BODY_LOG_LIMIT = 300;

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public ExternalTasksClient(RestClient externalApiRestClient, ObjectMapper objectMapper) {
		this.restClient = externalApiRestClient;
		this.objectMapper = objectMapper;
	}

	public ExternalTaskResponse createTask(ExternalTaskRequest request) {
		ResponseEntity<ExternalTaskResponse> response = restClient.post()
				.uri(uriBuilder -> uriBuilder.path("/tasks").build())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::handleError)
				.toEntity(ExternalTaskResponse.class);
		logger.debug("External task created at {}", response.getHeaders().getLocation());
		return response.getBody();
	}

	public ExternalTaskResponse getTask(Long id) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/tasks/{id}").build(id))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::handleError)
				.body(ExternalTaskResponse.class);
	}

	public List<ExternalTaskResponse> getTasks(Boolean completed, Integer limit) {
		return restClient.get()
				.uri(uriBuilder -> {
					var builder = uriBuilder.path("/tasks");
					if (completed != null) {
						builder.queryParam("completed", completed);
					}
					if (limit != null) {
						builder.queryParam("limit", limit);
					}
					return builder.build();
				})
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::handleError)
				.body(new ParameterizedTypeReference<>() {
				});
	}

	public void deleteTask(Long id) {
		restClient.delete()
				.uri(uriBuilder -> uriBuilder.path("/tasks/{id}").build(id))
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::handleError)
				.toBodilessEntity();
	}

	public String callUnstable(String mode) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/unstable").queryParam("mode", mode).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::handleError)
				.body(String.class);
	}

	private void handleError(org.springframework.http.HttpRequest request,
			org.springframework.http.client.ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		MediaType contentType = response.getHeaders().getContentType();
		HttpStatusCode statusCode = response.getStatusCode();

		if (contentType != null && MediaType.TEXT_HTML.includes(contentType)) {
			logger.warn("External API returned HTML error: {}", snippet(body));
			throw new ExternalApiException("External API returned an HTML error response", statusCode);
		}

		if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
			throw new ExternalTaskNotFoundException(problemMessage(body, "External task not found"));
		}

		if (statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
			String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
			throw new ExternalApiException("External API rate limit exceeded. Retry-After: " + retryAfter, statusCode);
		}

		if (statusCode.is5xxServerError()) {
			throw new ExternalApiException(problemMessage(body, "External API server error"), statusCode);
		}

		throw new ExternalApiException(problemMessage(body, "External API request failed"), statusCode);
	}

	private String problemMessage(String body, String fallback) {
		if (body == null || body.isBlank()) {
			return fallback;
		}
		try {
			ProblemDetail problemDetail = objectMapper.readValue(body, ProblemDetail.class);
			if (problemDetail.getDetail() != null) {
				return problemDetail.getDetail();
			}
			if (problemDetail.getTitle() != null) {
				return problemDetail.getTitle();
			}
		} catch (IOException ignored) {
			return fallback + ": " + snippet(body);
		}
		return fallback;
	}

	private String snippet(String body) {
		if (body == null) {
			return "";
		}
		return body.length() <= BODY_LOG_LIMIT ? body : body.substring(0, BODY_LOG_LIMIT);
	}
}
