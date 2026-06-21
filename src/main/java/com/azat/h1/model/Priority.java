package com.azat.h1.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Describes task priority levels.
 */
@Schema(description = "Task priority level")
public enum Priority {

	LOW,
	MEDIUM,
	HIGH
}
