package com.azat.h1.validation;

import com.azat.h1.dto.TaskResponseDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks due date against creation date for DTOs that expose both values.
 */
public class DueDateNotBeforeCreationValidator implements ConstraintValidator<DueDateNotBeforeCreation, TaskResponseDto> {

	@Override
	public boolean isValid(TaskResponseDto value, ConstraintValidatorContext context) {
		if (value == null || value.getCreatedAt() == null || value.getDueDate() == null) {
			return true;
		}
		return !value.getDueDate().isBefore(value.getCreatedAt().toLocalDate());
	}
}
