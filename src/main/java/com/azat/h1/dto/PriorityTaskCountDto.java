package com.azat.h1.dto;

import com.azat.h1.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Number of tasks grouped by priority.
 */
@Schema(description = "Task count grouped by priority")
public class PriorityTaskCountDto {

	@Schema(description = "Task priority", example = "MEDIUM")
	private Priority priority;

	@Schema(description = "Number of tasks", example = "5")
	private long count;

	public PriorityTaskCountDto() {
	}

	public PriorityTaskCountDto(Priority priority, long count) {
		this.priority = priority;
		this.count = count;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
