package com.mipt.azataminov.collections;

public interface CustomList<A> extends Iterable<A> {
    boolean add(A element);
    A get(int index);
    A remove(int index);
    int size();
    boolean isEmpty();
}