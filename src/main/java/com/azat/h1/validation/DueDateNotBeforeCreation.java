package com.azat.h1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a due date is not earlier than the creation date.
 */
@Documented
@Constraint(validatedBy = DueDateNotBeforeCreationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DueDateNotBeforeCreation {

	String message() default "dueDate must not be before createdAt";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
