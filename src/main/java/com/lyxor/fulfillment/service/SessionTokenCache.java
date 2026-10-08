package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.CacheKey;

import java.util.HashMap;
import java.util.Map;

public class SessionTokenCache {

    private final Map<CacheKey, String> tokenStore = new HashMap<>();
    private CacheMetrics metrics;

    public synchronized void putToken(CacheKey key, String token) {
        tokenStore.put(key, token);
    }

    public synchronized String getToken(CacheKey key) {
        key.touch();
        String token = tokenStore.get(key);
        if (token != null) {
            getMetrics().recordHit();
        } else {
            getMetrics().recordMiss();
        }
        return token;
    }

    public synchronized boolean contains(CacheKey key) {
        return tokenStore.containsKey(key);
    }

    public synchronized int size() {
        return tokenStore.size();
    }

    public CacheMetrics getMetrics() {
        if (metrics == null) {
            synchronized (this) {
                if (metrics == null) {
                    metrics = new CacheMetrics();
                }
            }
        }
        return metrics;
    }

    public static class CacheMetrics {
        private long hits = 0;
        private long misses = 0;

        public void recordHit() {
            hits++;
        }

        public void recordMiss() {
            misses++;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }
    }
}
