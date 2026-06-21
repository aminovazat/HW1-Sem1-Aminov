package com.azat.h1.dto;

import com.azat.h1.model.Priority;
import com.azat.h1.validation.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request body used to create a task.
 */
@Schema(description = "Task creation request")
public class TaskCreateDto {

	@NotBlank(groups = OnCreate.class)
	@Size(min = 3, max = 100, groups = OnCreate.class)
	@Schema(description = "Task title", example = "Learn Spring")
	private String title;

	@Size(max = 500, groups = OnCreate.class)
	@Schema(description = "Task description", example = "Read about validation and DTO mapping")
	private String description;

	@FutureOrPresent(groups = OnCreate.class)
	@Schema(description = "Task due date", example = "2026-06-30")
	private LocalDate dueDate;

	@NotNull(groups = OnCreate.class)
	@Schema(description = "Task priority", example = "MEDIUM")
	private Priority priority;

	@Size(max = 5, groups = OnCreate.class)
	@Schema(description = "Task tags", example = "[\"spring\", \"homework\"]")
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
