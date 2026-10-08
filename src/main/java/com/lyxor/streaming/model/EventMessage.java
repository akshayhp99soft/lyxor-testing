package com.lyxor.streaming.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EventMessage {
    private final String messageId;
    private final String topic;
    private final String payload;
    private final Map<String, String> headers;
    private final Instant timestamp;
    private DeliveryStatus status;
    private int retryCount;

    public EventMessage(String messageId, String topic, String payload, Map<String, String> headers) {
        this.messageId = Objects.requireNonNull(messageId, "messageId must not be null");
        this.topic = Objects.requireNonNull(topic, "topic must not be null");
        this.payload = payload;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.timestamp = Instant.now();
        this.status = DeliveryStatus.PENDING;
        this.retryCount = 0;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
