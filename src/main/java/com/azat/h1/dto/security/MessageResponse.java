package com.azat.h1.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Simple message response.
 */
@Schema(description = "Message response")
public class MessageResponse {

	@Schema(description = "Response message")
	private String message;

	public MessageResponse() {
	}

	public MessageResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
