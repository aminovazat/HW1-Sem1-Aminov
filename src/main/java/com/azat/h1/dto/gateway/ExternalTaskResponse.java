package com.azat.h1.dto.gateway;

/**
 * Response body returned by the task gateway and external task emulator.
 */
public class ExternalTaskResponse {

	private Long id;
	private String title;
	private String description;
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
