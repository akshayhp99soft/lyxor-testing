package com.lyxor.streaming.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class WebhookDispatcher {

    public enum CircuitState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private CircuitState state = CircuitState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
    private final int failureThreshold;
    private final int recoverySuccessThreshold;

    public WebhookDispatcher(int failureThreshold, int recoverySuccessThreshold) {
        this.failureThreshold = failureThreshold;
        this.recoverySuccessThreshold = recoverySuccessThreshold;
    }

    public synchronized void recordSuccess() {
        if (state == CircuitState.HALF_OPEN) {
            if (consecutiveSuccesses.incrementAndGet() >= recoverySuccessThreshold) {
                state = CircuitState.CLOSED;
                consecutiveSuccesses.set(0);
            }
        } else if (state == CircuitState.CLOSED) {
            failureCount.set(0);
        }
    }

    public synchronized void recordFailure() {
        consecutiveSuccesses.set(0);
        if (failureCount.incrementAndGet() >= failureThreshold) {
            state = CircuitState.OPEN;
        }
    }

    public boolean dispatchWithRetry(String endpoint, String payload, int maxRetries, Function<String, Integer> httpSender) {
        if (state == CircuitState.OPEN) {
            return false;
        }

        int attempts = 0;
        while (attempts <= maxRetries) {
            attempts++;
            try {
                int statusCode = httpSender.apply(endpoint);
                if (statusCode >= 200 && statusCode < 300) {
                    recordSuccess();
                    return true;
                }
            } catch (Exception e) {
                recordFailure();
            }
        }

        recordFailure();
        return false;
    }

    public synchronized CircuitState getState() {
        return state;
    }

    public synchronized void transitionToHalfOpen() {
        this.state = CircuitState.HALF_OPEN;
        this.consecutiveSuccesses.set(0);
    }
}
