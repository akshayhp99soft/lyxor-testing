package com.lyxor.fulfillment.model;

import java.util.Objects;

public class CacheKey {
    private final String userId;
    private final String tenantId;
    private long accessCount;

    public CacheKey(String userId, String tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.accessCount = 0;
    }

    public void touch() {
        this.accessCount++;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public long getAccessCount() {
        return accessCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return accessCount == cacheKey.accessCount &&
                Objects.equals(userId, cacheKey.userId) &&
                Objects.equals(tenantId, cacheKey.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, tenantId, accessCount);
    }
}
