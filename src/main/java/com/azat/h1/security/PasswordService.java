package com.azat.h1.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoder that applies a local pepper before BCrypt hashing.
 */
public class PasswordService implements PasswordEncoder {

	private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
	private final String pepper;

	public PasswordService(String pepper) {
		this.pepper = pepper;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return delegate.encode(withPepper(rawPassword));
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return delegate.matches(withPepper(rawPassword), encodedPassword);
	}

	private String withPepper(CharSequence rawPassword) {
		return String.valueOf(rawPassword) + pepper;
	}
}
