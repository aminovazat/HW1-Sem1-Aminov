package com.azat.h1.dto;

import com.azat.h1.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request body used to update a task.
 */
@Schema(description = "Task update payload")
public class TaskUpdateDto {

	@Size(min = 3, max = 100)
	private String title;

	@Size(max = 500)
	private String description;

	private Boolean completed;

	@FutureOrPresent
	private LocalDate dueDate;

	private Priority priority;

	@Size(max = 5)
	private Set<String> tags;

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

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
}
