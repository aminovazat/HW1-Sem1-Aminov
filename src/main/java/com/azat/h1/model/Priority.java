package com.azat.h1.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Priority level assigned to a task.
 */
@Schema(description = "Task priority")
public enum Priority {
	LOW,
	MEDIUM,
	HIGH
}
