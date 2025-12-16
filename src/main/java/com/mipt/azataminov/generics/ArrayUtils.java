package com.mipt.azataminov.generics;

public class ArrayUtils {

    public static <T> int findFirst(T[] array, T element) {
        if (array == null) {
            return -1;
        }

        for (int i = 0; i < array.length; i++) {
            if (element == null) {
                if (array[i] == null) {
                    return i;
                }
            } else {
                if (element.equals(array[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static void main(String[] args) {

        final String[] names = {"Alice", "Bob", "Charlie"};
        final int index = ArrayUtils.findFirst(names, "Bob");
        System.out.println("Index of 'Bob': " + index);

        final String[] withNull = {"Alice", null, "Charlie"};
        final int nullIndex = ArrayUtils.findFirst(withNull, null);
        System.out.println("Index of null: " + nullIndex);

        final int nullArrayResult = ArrayUtils.findFirst(null, "test");
        System.out.println("Result with null array: " + nullArrayResult);

        final int notFound = ArrayUtils.findFirst(names, null);
        System.out.println("Not found result: " + notFound);
    }
}
