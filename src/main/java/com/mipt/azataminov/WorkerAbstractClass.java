package com.mipt.azataminov;

public abstract class WorkerAbstractClass {

    public abstract void work(int hours);

    public boolean goHome(String str1, String str2) {
        return str1.equals(str2);
    }
}