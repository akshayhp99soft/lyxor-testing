package com.lyxor.fulfillment.model;

import java.util.Collections;
import java.util.List;

public class BatchResult {
    private final int totalSubmitted;
    private final int successfulCount;
    private final List<String> failedOrderIds;

    public BatchResult(int totalSubmitted, int successfulCount, List<String> failedOrderIds) {
        this.totalSubmitted = totalSubmitted;
        this.successfulCount = successfulCount;
        this.failedOrderIds = failedOrderIds;
    }

    public int getTotalSubmitted() {
        return totalSubmitted;
    }

    public int getSuccessfulCount() {
        return successfulCount;
    }

    public List<String> getFailedOrderIds() {
        return Collections.unmodifiableList(failedOrderIds);
    }

    public boolean isAllSuccessful() {
        return failedOrderIds.isEmpty() && totalSubmitted == successfulCount;
    }
}
