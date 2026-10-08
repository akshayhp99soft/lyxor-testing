package com.lyxor.streaming.model;

public enum DeliveryStatus {
    PENDING,
    IN_FLIGHT,
    ACKNOWLEDGED,
    FAILED,
    DEAD_LETTER
}
