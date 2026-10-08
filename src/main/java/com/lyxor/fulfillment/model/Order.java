package com.lyxor.fulfillment.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Order {
    private final String orderId;
    private final String customerId;
    private final CustomerTier customerTier;
    private final Set<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(String orderId, String customerId, CustomerTier customerTier) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerTier = customerTier;
        this.items = new HashSet<>();
        this.status = OrderStatus.CREATED;
        this.totalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(item.getSubtotal());
        }
        this.totalAmount = sum.subtract(discountAmount);
        this.updatedAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CustomerTier getCustomerTier() {
        return customerTier;
    }

    public Set<OrderItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        recalculateTotal();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
