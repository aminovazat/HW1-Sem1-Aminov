package com.azat.h1.exception;

/**
 * Thrown when a task cannot be found.
 */
public class TaskNotFoundException extends RuntimeException {

	public TaskNotFoundException(Long taskId) {
		super("Task not found: " + taskId);
	}
}
