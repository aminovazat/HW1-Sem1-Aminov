package com.azat.h1.exception;

import java.util.List;

/**
 * Signals that a bulk task update cannot be applied atomically.
 */
public class BulkTaskUpdateException extends RuntimeException {

	public BulkTaskUpdateException(List<Long> missingTaskIds) {
		super("Bulk task update failed. Missing task ids: " + missingTaskIds);
	}
}
