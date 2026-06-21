package com.azat.h1.dto.gateway;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Task response returned by the external API and gateway.
 */
@Schema(description = "External task response")
public class ExternalTaskResponse {

	@Schema(description = "Task id", example = "1")
	private Long id;

	@Schema(description = "Task title", example = "Gateway task")
	private String title;

	@Schema(description = "Task description", example = "Created through the gateway")
	private String description;

	@Schema(description = "Task completion flag", example = "false")
	private boolean completed;

	public ExternalTaskResponse() {
	}

	public ExternalTaskResponse(Long id, String title, String description, boolean completed) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.completed = completed;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
