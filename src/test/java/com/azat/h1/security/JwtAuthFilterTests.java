package com.azat.h1.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTests {

	@Test
	void maskTokenKeepsOnlyEdgesOfLongToken() {
		assertThat(JwtAuthFilter.maskToken("abcdef1234567890")).isEqualTo("abcdef...567890");
	}

	@Test
	void maskTokenHidesShortToken() {
		assertThat(JwtAuthFilter.maskToken("short")).isEqualTo("***");
	}
}
