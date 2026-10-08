package com.lyxor.fulfillment.model;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private final String sku;
    private final String name;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String sku, String name, int quantity, BigDecimal unitPrice) {
        this.sku = Objects.requireNonNull(sku, "sku must not be null");
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(sku, orderItem.sku);
    }
}
