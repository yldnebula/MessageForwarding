package com.example.demo.common;

import java.util.concurrent.atomic.AtomicInteger;

public class MinuteCounter {
    private static final int MASK = 0x7FFFFFFF;
    private final AtomicInteger atom;

    public MinuteCounter() {
        atom = new AtomicInteger(0);
    }

    public final int incrementAndGet() {
        return atom.incrementAndGet() & MASK;
    }

    public int get() {
        return atom.get() & MASK;
    }

    public void set(int newValue) {
        atom.set(newValue & MASK);
    }
}