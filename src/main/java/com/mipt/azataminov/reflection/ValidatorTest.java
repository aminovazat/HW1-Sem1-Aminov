package com.mipt.azataminov.reflection;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mipt.azataminov.reflection.annotations.*;

class ValidatorTest {
    static class TestClass {
        @NotNull(message = "Field cannot be null")
        private String notNullField;

        @Size(min = 3, max = 10, message = "Length must be between 3 and 10")
        private String sizeField;

        @Range(min = 1, max = 100, message = "Value must be between 1 and 100")
        private Integer rangeField;

        @Email(message = "Invalid email")
        private String emailField;

        public String getNotNullField() { return notNullField; }
        public void setNotNullField(String notNullField) { this.notNullField = notNullField; }
        public String getSizeField() { return sizeField; }
        public void setSizeField(String sizeField) { this.sizeField = sizeField; }
        public Integer getRangeField() { return rangeField; }
        public void setRangeField(Integer rangeField) { this.rangeField = rangeField; }
        public String getEmailField() { return emailField; }
        public void setEmailField(String emailField) { this.emailField = emailField; }
    }

    @Test
    void testValidObject() {
        TestClass valid = new TestClass();
        valid.setNotNullField("test");
        valid.setSizeField("abcde");
        valid.setRangeField(50);
        valid.setEmailField("test@example.com");

        ValidationResult result = Validator.validate(valid);
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void testNotNullValidation() {
        TestClass obj = new TestClass();
        obj.setNotNullField(null);

        ValidationResult result = Validator.validate(obj);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Field cannot be null"));
    }
}