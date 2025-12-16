package com.mipt.azataminov;

public class MainClass {
    private int uninitializedInt;
    private String uninitializedString;

    protected static double uninitializedDouble;

    public final long initializedLong = 1000L;

    public static void main(String[] args) {
        for (int i = 0; i <= 15; i++) {
            System.out.println("Iter: " + i);
        }
    }
}
