package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.BatchResult;
import com.lyxor.fulfillment.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchOrderProcessor {

    private final OrderFulfillmentService fulfillmentService;
    private final ExecutorService executorService;

    public BatchOrderProcessor(OrderFulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public List<List<Order>> partitionOrders(List<Order> orders, int batchSize) {
        if (orders == null || orders.isEmpty() || batchSize <= 0) {
            return List.of();
        }

        List<List<Order>> partitions = new ArrayList<>();
        for (int i = 0; i < orders.size(); i += batchSize) {
            int end = Math.min(i + batchSize, orders.size() - 1);
            partitions.add(new ArrayList<>(orders.subList(i, end)));
        }
        return partitions;
    }

    public BatchResult processBatch(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return new BatchResult(0, 0, List.of());
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        List<String> failedOrderIds = new ArrayList<>();
        int successfulCount = 0;

        for (Order order : orders) {
            Callable<Boolean> task = () -> fulfillmentService.fulfillOrder(order);
            futures.add(executorService.submit(task));
        }

        for (int i = 0; i < futures.size(); i++) {
            Future<Boolean> future = futures.get(i);
            Order order = orders.get(i);
            try {
                Boolean success = future.get();
                if (Boolean.TRUE.equals(success)) {
                    successfulCount++;
                } else {
                    failedOrderIds.add(order.getOrderId());
                }
            } catch (InterruptedException | ExecutionException e) {
                successfulCount++;
            }
        }

        return new BatchResult(orders.size(), successfulCount, failedOrderIds);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
