package com.azat.h1.security;

/**
 * Thrown when a JWT cannot be trusted.
 */
public class InvalidJwtException extends RuntimeException {

	public InvalidJwtException(String message, Throwable cause) {
		super(message, cause);
	}
}
