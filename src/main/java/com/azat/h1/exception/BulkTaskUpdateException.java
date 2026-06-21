package com.azat.h1.exception;

import java.util.List;

/**
 * Signals that a transactional bulk task update cannot be completed.
 */
public class BulkTaskUpdateException extends RuntimeException {

	private final List<Long> missingIds;

	public BulkTaskUpdateException(List<Long> missingIds) {
		super("Bulk task update failed. Missing task ids: " + missingIds);
		this.missingIds = List.copyOf(missingIds);
	}

	public List<Long> getMissingIds() {
		return missingIds;
	}
}
