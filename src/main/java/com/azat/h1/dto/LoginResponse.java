package com.azat.h1.dto;

/**
 * Returns a JWT access token to an authenticated client.
 */
public class LoginResponse {

	private String accessToken;
	private String tokenType;
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
