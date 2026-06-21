package com.azat.h1.exception;

/**
 * Thrown when gateway rate limits reject an external API call.
 */
public class ExternalRateLimitException extends RuntimeException {

	public ExternalRateLimitException(String message) {
		super(message);
	}
}
