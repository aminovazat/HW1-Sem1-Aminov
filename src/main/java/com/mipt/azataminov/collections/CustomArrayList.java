package com.mipt.azataminov.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomArrayList<A> implements CustomList<A> {
    private static final int DEFAULT_CAPACITY = 10;
    private static final double GROWTH_FACTOR = 1.5;
    private Object[] elements;
    private int size;

    public CustomArrayList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public CustomArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            int newCapacity = (int) (elements.length * GROWTH_FACTOR);
            if (newCapacity <= elements.length) {
                newCapacity = elements.length + 1;
            }
            Object[] newElements = new Object[newCapacity];
            System.arraycopy(elements, 0, newElements, 0, size);
            elements = newElements;
        }
    }

    @Override
    public boolean add(A element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        ensureCapacity();
        elements[size] = element;
        size++;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public A get(int index) {
        checkIndex(index);
        return (A) elements[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public A remove(int index) {
        checkIndex(index);
        A removedElement = (A) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[size - 1] = null;
        size--;
        return removedElement;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    String.format("Index: %d, Size: %d", index, size)
            );
        }
    }

    @Override
    public Iterator<A> iterator() {
        return new CustomArrayListIterator();
    }

    private class CustomArrayListIterator implements Iterator<A> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public A next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (A) elements[currentIndex++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation is not supported by this iterator");
        }
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        CustomList<String> list = new CustomArrayList<>();
        System.out.println("=== Тестирование CustomArrayList ===");
        System.out.println("Список пуст? " + list.isEmpty());
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        System.out.println("После добавления 3 элементов: " + list);
        System.out.println("Размер: " + list.size());
        System.out.println("Элемент с индексом 1: " + list.get(1));
        String removed = list.remove(1);
        System.out.println("Удаленный элемент: " + removed);
        System.out.println("После удаления: " + list);
        System.out.println("Размер: " + list.size());
        System.out.print("Итерация по списку: ");
        for (String item : list ) {
            System.out.print(item + " ");
        }
        System.out.println();
        CustomList<Integer> numbers = new CustomArrayList<>(3);
        for (int i = 1; i <= 10; i++) {
            numbers.add(i * 10);
        }
        System.out.println("Динамическое расширение: " + numbers);
        System.out.println("Размер: " + numbers.size());
    }
}
