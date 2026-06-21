package com.azat.h1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Checks that a requested due date is not before the existing task creation date.
 */
public class DueDateNotBeforeCreationValidator
		implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateValidationCommand> {

	@Override
	public boolean isValid(TaskUpdateValidationCommand value, ConstraintValidatorContext context) {
		if (value == null || value.getTask() == null || value.getUpdateDto() == null
				|| value.getUpdateDto().getDueDate() == null || value.getTask().getCreatedAt() == null) {
			return true;
		}

		LocalDate creationDate = value.getTask().getCreatedAt().toLocalDate();
		return !value.getUpdateDto().getDueDate().isBefore(creationDate);
	}
}
