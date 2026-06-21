package com.azat.h1.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Authenticated user profile response.
 */
@Schema(description = "Profile response")
public class ProfileResponse {

	@Schema(description = "Authenticated username", example = "user")
	private String username;

	@Schema(description = "Granted authorities")
	private List<String> authorities;

	@Schema(description = "Profile message")
	private String message;

	public ProfileResponse() {
	}

	public ProfileResponse(String username, List<String> authorities, String message) {
		this.username = username;
		this.authorities = authorities;
		this.message = message;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
