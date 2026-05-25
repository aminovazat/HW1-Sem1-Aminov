package com.azat.h1.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Encodes and checks passwords with a local pepper.
 */
@Service
public class PasswordService {

	private final PasswordEncoder passwordEncoder;
	private final String pepper;

	public PasswordService(PasswordEncoder passwordEncoder, @Value("${security.password.pepper}") String pepper) {
		this.passwordEncoder = passwordEncoder;
		this.pepper = pepper;
	}

	public String encode(String rawPassword) {
		return passwordEncoder.encode(rawPassword + pepper);
	}

	public boolean matches(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword + pepper, encodedPassword);
	}
}
