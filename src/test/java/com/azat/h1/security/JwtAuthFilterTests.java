package com.azat.h1.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTests {

	private final JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(
			new JwtUtils("test-secret-test-secret-test-secret-test-secret", 60),
			new ObjectMapper()
	);

	@Test
	void maskTokenMasksLongTokens() {
		assertThat(jwtAuthFilter.maskToken("abcdef1234567890")).isEqualTo("abcdef...567890");
	}

	@Test
	void maskTokenHidesShortTokens() {
		assertThat(jwtAuthFilter.maskToken("short")).isEqualTo("***");
	}
}
