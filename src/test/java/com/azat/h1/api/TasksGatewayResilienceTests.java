package com.azat.h1.api;

import com.azat.h1.dto.LoginRequest;
import com.azat.h1.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
		properties = {
				"server.port=8081",
				"external.api.base-url=http://localhost:8081/external/v1",
				"resilience4j.ratelimiter.instances.externalApi.limitForPeriod=2",
				"resilience4j.ratelimiter.instances.externalApi.limitRefreshPeriod=30s",
				"resilience4j.ratelimiter.instances.externalApi.timeoutDuration=0",
				"resilience4j.circuitbreaker.instances.externalApi.slidingWindowSize=5",
				"resilience4j.circuitbreaker.instances.externalApi.minimumNumberOfCalls=3"
		}
)
class TasksGatewayResilienceTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void unstableFailureReturnsGracefulFallback() {
		String token = loginAndGetToken();

		for (int index = 0; index < 3; index++) {
			ResponseEntity<String> response = restTemplate.exchange(
					"/api/v1/tasks/unstable?mode=500",
					HttpMethod.GET,
					authorizedEntity(token),
					String.class
			);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).contains("Graceful degradation");
		}
	}

	@Test
	void rateLimiterReturnsMeaningfulFallback() {
		String token = loginAndGetToken();

		ResponseEntity<String> firstResponse = callTaskList(token);
		ResponseEntity<String> secondResponse = callTaskList(token);
		ResponseEntity<String> limitedResponse = callTaskList(token);

		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(limitedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(limitedResponse.getBody()).contains("fallback task");
	}

	@Test
	void actuatorHealthIsPublic() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\"");
	}

	private ResponseEntity<String> callTaskList(String token) {
		return restTemplate.exchange(
				"/api/v1/tasks?completed=false&limit=10",
				HttpMethod.GET,
				authorizedEntity(token),
				String.class
		);
	}

	private HttpEntity<?> authorizedEntity(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(headers);
	}

	private String loginAndGetToken() {
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUsername("user");
		loginRequest.setPassword("password");
		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
				"/api/v1/auth/login",
				loginRequest,
				LoginResponse.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		return response.getBody().getAccessToken();
	}
}
