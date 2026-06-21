package com.azat.h1.exception;

/**
 * Thrown when an uploaded file cannot be stored or loaded.
 */
public class FileStorageException extends RuntimeException {

	public FileStorageException(String message) {
		super(message);
	}

	public FileStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
