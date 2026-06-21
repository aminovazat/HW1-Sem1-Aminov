package com.azat.h1.validation;

import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.model.Task;

/**
 * Validation command used to compare update data with the current task state.
 */
@DueDateNotBeforeCreation
public class TaskUpdateValidationCommand {

	private final Task task;
	private final TaskUpdateDto updateDto;

	public TaskUpdateValidationCommand(Task task, TaskUpdateDto updateDto) {
		this.task = task;
		this.updateDto = updateDto;
	}

	public Task getTask() {
		return task;
	}

	public TaskUpdateDto getUpdateDto() {
		return updateDto;
	}
}
