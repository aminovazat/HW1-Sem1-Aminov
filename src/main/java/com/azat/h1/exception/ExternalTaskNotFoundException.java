package com.azat.h1.exception;

/**
 * Signals that the external task API did not find a requested task.
 */
public class ExternalTaskNotFoundException extends RuntimeException {

	public ExternalTaskNotFoundException(String message) {
		super(message);
	}
}
