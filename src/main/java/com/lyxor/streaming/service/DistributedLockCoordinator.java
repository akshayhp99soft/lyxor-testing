package com.lyxor.streaming.service;

import com.lyxor.streaming.model.LockLease;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedLockCoordinator {

    private final Map<String, LockLease> activeLeases = new ConcurrentHashMap<>();

    public synchronized boolean acquireLock(String lockKey, String ownerToken, long leaseDurationMs) {
        LockLease existing = activeLeases.get(lockKey);
        if (existing != null && !existing.isExpired()) {
            return false;
        }

        activeLeases.put(lockKey, new LockLease(lockKey, ownerToken, leaseDurationMs));
        return true;
    }

    public boolean releaseLock(String lockKey, String ownerToken) {
        LockLease lease = activeLeases.get(lockKey);
        if (lease != null && lease.getOwnerToken().equals(ownerToken)) {
            activeLeases.remove(lockKey);
            return true;
        }
        return false;
    }

    public boolean isLocked(String lockKey) {
        LockLease lease = activeLeases.get(lockKey);
        return lease != null && !lease.isExpired();
    }
}
