package com.lyxor.fulfillment.model;

import java.time.Instant;

public class AuditEntry {
    private final String eventId;
    private final String entityType;
    private final String entityId;
    private final String action;
    private final Instant timestamp;

    public AuditEntry(String eventId, String entityType, String entityId, String action) {
        this.eventId = eventId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.timestamp = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s:%s - %s (ID: %s)", timestamp, entityType, entityId, action, eventId);
    }
}
