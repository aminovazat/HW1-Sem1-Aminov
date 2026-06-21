package com.azat.h1.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTests {

	private final JwtUtils jwtUtils = new JwtUtils("test-secret-test-secret-test-secret-test-secret", 60);

	@Test
	void generatedTokenValidatesAndRestoresAuthorities() {
		var user = User.withUsername("reader")
				.password("ignored")
				.authorities("ROLE_USER", "READ_PRIVILEGE")
				.build();

		String token = jwtUtils.generateToken(user);

		assertThat(jwtUtils.getUsername(token)).isEqualTo("reader");
		assertThat(jwtUtils.getAuthorities(token)).containsExactlyInAnyOrder("ROLE_USER", "READ_PRIVILEGE");
		assertThat(jwtUtils.getRoles(token)).containsExactly("USER");
	}

	@Test
	void invalidTokenIsRejected() {
		assertThatThrownBy(() -> jwtUtils.getUsername("not-a-token"))
				.isInstanceOf(InvalidJwtException.class);
	}
}
