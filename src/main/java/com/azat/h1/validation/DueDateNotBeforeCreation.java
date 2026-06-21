package com.azat.h1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that an update due date is not earlier than the task creation date.
 */
@Documented
@Constraint(validatedBy = DueDateNotBeforeCreationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DueDateNotBeforeCreation {

	String message() default "Due date must not be before task creation date";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
