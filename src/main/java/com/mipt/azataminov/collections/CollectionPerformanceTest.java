package com.mipt.azataminov.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CollectionPerformanceTest {
    private static final int ELEMENT_COUNT = 10000;

    public static void main(String[] args) {
        System.out.println("=== Сравнение производительности ArrayList и LinkedList ===");
        System.out.println("Количество элементов: " + ELEMENT_COUNT);
        System.out.println();
        System.out.printf("%-25s %-15s %-15s%n", "Операция", "ArrayList (мс)", "LinkedList (мс)");
        System.out.println("-----------------------------------------------------------");

        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        long arrayListTime = measureTime(() -> {
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                arrayList.add(i);
            }
        });
        long linkedListTime = measureTime(() -> {
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                linkedList.add(i);
            }
        });
        System.out.printf("%-25s %-15d %-15d%n", "Добавление в конец", arrayListTime, linkedListTime);

        arrayList.clear();
        linkedList.clear();

        arrayListTime = measureTime(() -> {
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                arrayList.add(0, i);
            }
        });
        linkedListTime = measureTime(() -> {
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                linkedList.add(0, i);
            }
        });
        System.out.printf("%-25s %-15d %-15d%n", "Добавление в начало", arrayListTime, linkedListTime);

        for (int i = 0; i < ELEMENT_COUNT; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        int middleIndex = ELEMENT_COUNT / 2;
        arrayListTime = measureTime(() -> {
            for (int i = 0; i < 100; i++) {
                arrayList.add(middleIndex, i);
            }
        });
        linkedListTime = measureTime(() -> {
            for (int i = 0; i < 100; i++) {
                linkedList.add(middleIndex, i);
            }
        });
        System.out.printf("%-25s %-15d %-15d%n", "Вставка в середину (100)", arrayListTime, linkedListTime);

        arrayListTime = measureTime(() -> {
            for (int i = 0; i < 10000; i++) {
                arrayList.get(i % ELEMENT_COUNT);
            }
        });
        linkedListTime = measureTime(() -> {
            for (int i = 0; i < 1000; i++) {
                linkedList.get(i % ELEMENT_COUNT);
            }
        });
        System.out.printf("%-25s %-15d %-15d%n", "Доступ по индексу", arrayListTime, linkedListTime);

        arrayListTime = measureTime(() -> {
            while (!arrayList.isEmpty()) {
                arrayList.remove(0);
            }
        });
        linkedListTime = measureTime(() -> {
            while (!linkedList.isEmpty()) {
                linkedList.remove(0);
            }
        });
        System.out.printf("%-25s %-15d %-15d%n", "Удаление из начала", arrayListTime, linkedListTime);

        for (int i = 0; i < ELEMENT_COUNT; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        arrayListTime = measureTime(() -> {
            while (!arrayList.isEmpty()) {
                arrayList.remove(arrayList.size() - 1);
            }
        });
        linkedListTime = measureTime(() -> {
            while (!linkedList.isEmpty()) {
                linkedList.remove(linkedList.size() - 1);
            }
        });
        System.out.printf("%-25s %-15d %-15d%n", "Удаление из конца", arrayListTime, linkedListTime);
    }

    private static long measureTime(Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}