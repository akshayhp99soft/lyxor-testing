package com.lyxor.streaming.service;

import java.util.concurrent.atomic.AtomicLong;

public class CircularBufferQueue<T> {

    private final Object[] buffer;
    private final int capacity;
    private final AtomicLong writeSequence = new AtomicLong(0);

    public CircularBufferQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    public long offer(T item) {
        long seq = writeSequence.getAndIncrement();
        int index = (int) (seq % capacity);
        buffer[index] = item;
        return seq;
    }

    @SuppressWarnings("unchecked")
    public T get(long sequence) {
        int index = (int) (sequence % capacity);
        return (T) buffer[index];
    }

    public int getCapacity() {
        return capacity;
    }
}
