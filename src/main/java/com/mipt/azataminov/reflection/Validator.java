package com.mipt.azataminov.reflection;

import com.mipt.azataminov.reflection.annotations.*;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static ValidationResult validate(Object object) {
        ValidationResult result = new ValidationResult();

        if (object == null) {
            result.addError("Object cannot be null");
            return result;
        }

        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                Object value = field.get(object);

                NotNull notNull = field.getAnnotation(NotNull.class);
                if (notNull != null && value == null) {
                    result.addError(notNull.message());
                }

                Size size = field.getAnnotation(Size.class);
                if (size != null && value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.length() < size.min() || strValue.length() > size.max()) {
                        String message = size.message()
                                .replace("{min}", String.valueOf(size.min()))
                                .replace("{max}", String.valueOf(size.max()));
                        result.addError(message);
                    }
                }

                Range range = field.getAnnotation(Range.class);
                if (range != null && value != null && value instanceof Number) {
                    long numValue = ((Number) value).longValue();
                    if (numValue < range.min() || numValue > range.max()) {
                        String message = range.message()
                                .replace("{min}", String.valueOf(range.min()))
                                .replace("{max}", String.valueOf(range.max()));
                        result.addError(message);
                    }
                }

                Email email = field.getAnnotation(Email.class);
                if (email != null && value instanceof String) {
                    String emailValue = (String) value;
                    if (emailValue != null && !EMAIL_PATTERN.matcher(emailValue).matches()) {
                        result.addError(email.message());
                    }
                }

            } catch (IllegalAccessException e) {
                result.addError("Cannot access field: " + field.getName());
            }
        }

        return result;
    }
}