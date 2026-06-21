package com.azat.h1.dto.gateway;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request sent to the external task API.
 */
@Schema(description = "External task request")
public class ExternalTaskRequest {

	@Schema(description = "Task title", example = "Gateway task")
	private String title;

	@Schema(description = "Task description", example = "Created through the gateway")
	private String description;

	@Schema(description = "Task completion flag", example = "false")
	private Boolean completed;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
}
