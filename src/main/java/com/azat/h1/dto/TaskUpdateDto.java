package com.azat.h1.dto;

import com.azat.h1.model.Priority;
import com.azat.h1.validation.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request body used to partially update a task.
 */
@Schema(description = "Task update request")
public class TaskUpdateDto {

	@Size(min = 3, max = 100, groups = OnUpdate.class)
	@Schema(description = "Task title", example = "Finish homework")
	private String title;

	@Size(max = 500, groups = OnUpdate.class)
	@Schema(description = "Task description", example = "Finish all HW2 requirements")
	private String description;

	@Schema(description = "Whether the task is completed", example = "false")
	private Boolean completed;

	@FutureOrPresent(groups = OnUpdate.class)
	@Schema(description = "Task due date", example = "2026-06-30")
	private LocalDate dueDate;

	@Schema(description = "Task priority", example = "HIGH")
	private Priority priority;

	@Size(max = 5, groups = OnUpdate.class)
	@Schema(description = "Task tags", example = "[\"spring\", \"validation\"]")
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
