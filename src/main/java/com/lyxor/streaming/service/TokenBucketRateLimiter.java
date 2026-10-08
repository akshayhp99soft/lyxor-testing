package com.lyxor.streaming.service;

public class TokenBucketRateLimiter {

    private final long capacity;
    private final long refillRatePerSecond;
    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, long refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.availableTokens = capacity;
        this.lastRefillNanos = System.nanoTime();
    }

    public synchronized boolean tryAcquire(long tokens) {
        refill();
        if (availableTokens >= tokens) {
            availableTokens -= tokens;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) {
            return;
        }

        long tokensToAdd = (elapsedNanos * refillRatePerSecond) / 1_000_000_000L;
        if (tokensToAdd > 0) {
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillNanos = now;
        }
    }

    public synchronized double getAvailableTokens() {
        refill();
        return availableTokens;
    }
}
