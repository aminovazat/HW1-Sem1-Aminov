package com.mipt.azataminov.model;

public class Human {
    private String firstName;
    private String lastName;
    private int age;
    private boolean isWorking;

    public Human() {
    }

    public Human(String firstName, String lastName, int age, boolean isWorking) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.isWorking = isWorking;
    }

    // Геттеры
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public boolean isWorking() {
        return isWorking;
    }

    // Сеттеры
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }
}
