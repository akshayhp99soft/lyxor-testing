package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.OrderItem;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService {

    private final Map<String, Integer> stockLevels = new ConcurrentHashMap<>();

    public void setStock(String sku, int quantity) {
        stockLevels.put(sku, quantity);
    }

    public int getStock(String sku) {
        return stockLevels.getOrDefault(sku, 0);
    }

    public boolean reserveStock(String sku, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        Integer currentStock = stockLevels.get(sku);
        if (currentStock != null && currentStock >= quantity) {
            stockLevels.put(sku, currentStock - quantity);
            return true;
        }
        return false;
    }

    public boolean reserveAll(Collection<OrderItem> items) {
        for (OrderItem item : items) {
            if (!reserveStock(item.getSku(), item.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    public void releaseStock(String sku, int quantity) {
        stockLevels.compute(sku, (k, v) -> (v == null ? 0 : v) + quantity);
    }

    public void releaseAll(Collection<OrderItem> items) {
        for (OrderItem item : items) {
            releaseStock(item.getSku(), item.getQuantity());
        }
    }
}
