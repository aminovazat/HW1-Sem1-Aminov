package com.azat.h1.exception;

/**
 * Signals that the external task API returned an unusable response.
 */
public class ExternalApiException extends RuntimeException {

	public ExternalApiException(String message) {
		super(message);
	}

	public ExternalApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
