package com.azat.h1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configures RestClient for the external task API.
 */
@Configuration
public class RestClientConfig {

	@Bean
	public RestClient externalApiRestClient(
			@Value("${external.api.base-url}") String baseUrl,
			@Value("${external.api.user-agent}") String userAgent,
			ClientHttpRequestFactory externalApiRequestFactory) {
		return RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.USER_AGENT, userAgent)
				.requestFactory(externalApiRequestFactory)
				.build();
	}

	@Bean
	public ClientHttpRequestFactory externalApiRequestFactory(
			@Value("${external.api.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${external.api.read-timeout-ms}") int readTimeoutMs) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
		return requestFactory;
	}
}
