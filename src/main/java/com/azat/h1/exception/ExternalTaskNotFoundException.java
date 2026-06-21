package com.azat.h1.exception;

/**
 * Thrown when the external task API reports a missing task.
 */
public class ExternalTaskNotFoundException extends RuntimeException {

	public ExternalTaskNotFoundException(Long taskId) {
		super("External task not found: " + taskId);
	}

	public ExternalTaskNotFoundException(String message) {
		super(message);
	}
}
