package com.mipt.azataminov.collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;

class CustomArrayListTest {
    private CustomList<String> list;

    @BeforeEach
    void setUp() {
        list = new CustomArrayList<>();
    }

    @Test
    void testAddAndGet() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    void testAddNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            list.add(null);
        });
    }

    @Test
    void testGetWithInvalidIndex() {
        list.add("A");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.get(-1);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.get(1);
        });
    }

    @Test
    void testRemove() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        String removed = list.remove(1);
        assertEquals("B", removed);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("D", list.get(2));
        removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(2, list.size());
        assertEquals("C", list.get(0));
        assertEquals("D", list.get(1));
        removed = list.remove(1);
        assertEquals("D", removed);
        assertEquals(1, list.size());
        assertEquals("C", list.get(0));
    }

    @Test
    void testRemoveWithInvalidIndex() {
        list.add("A");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.remove(-1);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.remove(1);
        });
    }

    @Test
    void testSizeAndIsEmpty() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        list.add("A");
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        list.remove(0);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void testDynamicExpansion() {
        CustomList<Integer> numbers = new CustomArrayList<>(3);
        for (int i = 1; i <= 10; i++) {
            numbers.add(i);
        }
        assertEquals(10, numbers.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, numbers.get(i));
        }
    }

    @Test
    void testIterator() {
        list.add("A");
        list.add("B");
        list.add("C");
        Iterator<String> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertEquals("B", iterator.next());
        assertEquals("C", iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, () -> {
            iterator.next();
        });
    }

    @Test
    void testIteratorRemoveNotSupported() {
        list.add("A");
        Iterator<String> iterator = list.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, () -> {
            iterator.remove();
        });
    }

    @Test
    void testForEachLoop() {
        list.add("A");
        list.add("B");
        list.add("C");
        StringBuilder result = new StringBuilder();
        for (String item : list) {
            result.append(item);
        }
        assertEquals("ABC", result.toString());
    }
}