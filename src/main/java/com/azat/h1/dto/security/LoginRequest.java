package com.azat.h1.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Login request for issuing a JWT access token.
 */
@Schema(description = "Login request")
public class LoginRequest {

	@NotBlank
	@Schema(description = "Username", example = "user")
	private String username;

	@NotBlank
	@Schema(description = "Password", example = "password")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
