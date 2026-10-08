package com.lyxor.streaming.model;

import java.time.Instant;
import java.util.Objects;

public class LockLease {
    private final String lockKey;
    private final String ownerToken;
    private final Instant acquiredAt;
    private final long leaseDurationMs;

    public LockLease(String lockKey, String ownerToken, long leaseDurationMs) {
        this.lockKey = Objects.requireNonNull(lockKey);
        this.ownerToken = Objects.requireNonNull(ownerToken);
        this.acquiredAt = Instant.now();
        this.leaseDurationMs = leaseDurationMs;
    }

    public String getLockKey() {
        return lockKey;
    }

    public String getOwnerToken() {
        return ownerToken;
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() > (acquiredAt.toEpochMilli() + leaseDurationMs);
    }
}
