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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * HTTP client for the external task API.
 */
@Component
public class ExternalTasksClient {

	private static final Logger logger = LoggerFactory.getLogger(ExternalTasksClient.class);
	private static final ParameterizedTypeReference<List<ExternalTaskResponse>> TASK_LIST_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public ExternalTasksClient(RestClient externalApiRestClient, ObjectMapper objectMapper) {
		this.restClient = externalApiRestClient;
		this.objectMapper = objectMapper;
	}

	public ExternalTaskResponse createTask(ExternalTaskRequest request) {
		try {
			ResponseEntity<ExternalTaskResponse> response = restClient.post()
					.uri("/tasks")
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.onStatus(HttpStatusCode::isError, this::handleError)
					.toEntity(ExternalTaskResponse.class);
			if (!response.getStatusCode().is2xxSuccessful() || !response.getHeaders().containsKey(HttpHeaders.LOCATION)) {
				throw new ExternalApiException("External API returned unexpected create response");
			}
			return response.getBody();
		} catch (RestClientException ex) {
			throw new ExternalApiException("External API create request failed", ex);
		}
	}

	public ExternalTaskResponse getTask(Long id) {
		try {
			return restClient.get()
					.uri("/tasks/{id}", id)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.onStatus(HttpStatusCode::isError, this::handleError)
					.body(ExternalTaskResponse.class);
		} catch (ExternalTaskNotFoundException | ExternalApiException ex) {
			throw ex;
		} catch (RestClientException ex) {
			throw new ExternalApiException("External API get request failed", ex);
		}
	}

	public List<ExternalTaskResponse> getTasks(Boolean completed, Integer limit) {
		try {
			List<ExternalTaskResponse> response = restClient.get()
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
					.body(TASK_LIST_TYPE);
			return response == null ? List.of() : response;
		} catch (ExternalApiException ex) {
			throw ex;
		} catch (RestClientException ex) {
			throw new ExternalApiException("External API list request failed", ex);
		}
	}

	public void deleteTask(Long id) {
		try {
			ResponseEntity<Void> response = restClient.delete()
					.uri("/tasks/{id}", id)
					.retrieve()
					.onStatus(HttpStatusCode::isError, this::handleError)
					.toBodilessEntity();
			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new ExternalApiException("External API returned unexpected delete response");
			}
		} catch (ExternalTaskNotFoundException | ExternalApiException ex) {
			throw ex;
		} catch (RestClientException ex) {
			throw new ExternalApiException("External API delete request failed", ex);
		}
	}

	public String callUnstable(String mode) {
		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/unstable").queryParam("mode", mode).build())
					.retrieve()
					.onStatus(HttpStatusCode::isError, this::handleError)
					.body(String.class);
		} catch (ExternalApiException ex) {
			throw ex;
		} catch (RestClientException ex) {
			throw new ExternalApiException("External API unstable request failed", ex);
		}
	}

	private void handleError(org.springframework.http.HttpRequest request,
			org.springframework.http.client.ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		MediaType contentType = response.getHeaders().getContentType();
		if (contentType != null && MediaType.TEXT_HTML.includes(contentType)) {
			logger.warn("External API returned HTML error body: {}", limited(body));
			throw new ExternalApiException("External API returned non-JSON error response");
		}

		ProblemDetail problemDetail = parseProblemDetail(body);
		String message = problemDetail == null || problemDetail.getDetail() == null
				? "External API request failed with status " + response.getStatusCode().value()
				: problemDetail.getDetail();

		if (response.getStatusCode().value() == 404) {
			throw new ExternalTaskNotFoundException(message);
		}
		throw new ExternalApiException(message);
	}

	private ProblemDetail parseProblemDetail(String body) {
		try {
			return objectMapper.readValue(body, ProblemDetail.class);
		} catch (IOException ex) {
			return null;
		}
	}

	private String limited(String body) {
		if (body == null) {
			return "";
		}
		return body.length() <= 300 ? body : body.substring(0, 300);
	}
}
