package com.azat.h1.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JWT login response.
 */
@Schema(description = "Login response")
public class LoginResponse {

	@Schema(description = "JWT access token")
	private String accessToken;

	@Schema(description = "Token type", example = "Bearer")
	private String tokenType;

	@Schema(description = "Token lifetime in minutes", example = "60")
	private long expiresInMinutes;

	public LoginResponse() {
	}

	public LoginResponse(String accessToken, String tokenType, long expiresInMinutes) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresInMinutes = expiresInMinutes;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public long getExpiresInMinutes() {
		return expiresInMinutes;
	}

	public void setExpiresInMinutes(long expiresInMinutes) {
		this.expiresInMinutes = expiresInMinutes;
	}
}
