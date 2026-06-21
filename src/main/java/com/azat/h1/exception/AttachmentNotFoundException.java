package com.azat.h1.exception;

/**
 * Thrown when an attachment cannot be found.
 */
public class AttachmentNotFoundException extends RuntimeException {

	public AttachmentNotFoundException(Long attachmentId) {
		super("Attachment not found: " + attachmentId);
	}
}
