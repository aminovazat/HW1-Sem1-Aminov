package com.mipt.azataminov.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapUtils {
    public static List<Student> findStudentsByGradeRange(
            Map<Integer, Student> map,
            double minGrade,
            double maxGrade) {
        if (minGrade > maxGrade) {
            throw new IllegalArgumentException(
                    String.format("minGrade (%.2f) не может быть больше maxGrade (%.2f)", minGrade, maxGrade)
            );
        }
        List<Student> result = new ArrayList<>();
        for (Student student : map.values()) {
            if (student.getGrade() >= minGrade && student.getGrade() <= maxGrade) {
                result.add(student);
            }
        }
        return result;
    }

    public static List<Student> getTopNStudents(
            TreeMap<Integer, Student> map,
            int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n должно быть положительным числом: " + n);
        }
        if (n > map.size()) {
            n = map.size();
        }
        List<Student> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Integer, Student> entry : map.descendingMap().entrySet()) {
            if (count >= n) {
                break;
            }
            result.add(entry.getValue());
            count++;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Демонстрация работы с HashMap и TreeMap ===");
        Map<Integer, Student> hashMap = new HashMap<>();
        hashMap.put(101, new Student(101, "Alice", 4.5));
        hashMap.put(102, new Student(102, "Bob", 3.8));
        hashMap.put(103, new Student(103, "Charlie", 4.2));
        hashMap.put(104, new Student(104, "Diana", 4.9));
        hashMap.put(105, new Student(105, "Eve", 3.5));
        System.out.println("HashMap содержимое:");
        hashMap.forEach((id, student) ->
                System.out.println("  " + id + " -> " + student));
        List<Student> studentsInRange = findStudentsByGradeRange(hashMap, 4.0, 4.7);
        System.out.println("\nСтуденты с оценкой от 4.0 до 4.7:");
        studentsInRange.forEach(System.out::println);
        TreeMap<Integer, Student> treeMap = new TreeMap<>((a, b) -> b.compareTo(a));
        treeMap.putAll(hashMap);
        System.out.println("\nTreeMap (сортировка по убыванию id):");
        treeMap.forEach((id, student) ->
                System.out.println("  " + id + " -> " + student));
        List<Student> top3Students = getTopNStudents(treeMap, 3);
        System.out.println("\nТоп-3 студента с наибольшими id:");
        top3Students.forEach(System.out::println);
        System.out.println("\n=== Тестирование equals() и hashCode() ===");
        Student student1 = new Student(101, "Alice", 4.5);
        Student student2 = new Student(101, "Alice", 4.5);
        Student student3 = new Student(102, "Bob", 3.8);
        System.out.println("student1.equals(student2): " + student1.equals(student2));
        System.out.println("student1.equals(student3): " + student1.equals(student3));
        System.out.println("student1.hashCode() == student2.hashCode(): " +
                (student1.hashCode() == student2.hashCode()));
    }
}