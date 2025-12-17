package com.mipt.azataminov.reflection;

public class Main {
    public static void main(String[] args) {
        User user = new User();
        user.setName("A");
        user.setEmail("invalid-email");

        ValidationResult result = Validator.validate(user);

        if (!result.isValid()) {
            System.out.println("Ошибки валидации:");
            result.getErrors().forEach(System.out::println);
        }
    }
}