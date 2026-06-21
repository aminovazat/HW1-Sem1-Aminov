package com.azat.h1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configures the RestClient used by the external task gateway.
 */
@Configuration
public class RestClientConfig {

	@Bean
	public RestClient externalApiRestClient(
			@Value("${external-api.base-url:http://localhost:8080/external/v1}") String baseUrl,
			@Value("${external-api.user-agent:h1-gateway/1.0}") String userAgent,
			RestClient.Builder builder) {
		return builder.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.USER_AGENT, userAgent)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	@Bean
	public RestClientCustomizer externalApiTimeoutCustomizer(
			@Value("${external-api.connect-timeout-ms:1000}") long connectTimeoutMs,
			@Value("${external-api.read-timeout-ms:1500}") long readTimeoutMs) {
		return builder -> {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
			requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
			builder.requestFactory(requestFactory);
		};
	}
}
