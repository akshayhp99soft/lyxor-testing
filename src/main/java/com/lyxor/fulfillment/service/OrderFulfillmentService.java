package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.Order;
import com.lyxor.fulfillment.model.OrderStatus;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderFulfillmentService {

    private final InventoryService inventoryService;
    private final DiscountService discountService;
    private final WalletPaymentService paymentService;

    public OrderFulfillmentService(InventoryService inventoryService,
                                   DiscountService discountService,
                                   WalletPaymentService paymentService) {
        this.inventoryService = Objects.requireNonNull(inventoryService);
        this.discountService = Objects.requireNonNull(discountService);
        this.paymentService = Objects.requireNonNull(paymentService);
    }

    public boolean fulfillOrder(Order order) {
        if (order == null || order.getItems().isEmpty()) {
            return false;
        }

        order.setStatus(OrderStatus.VALIDATED);

        boolean reserved = inventoryService.reserveAll(order.getItems());
        if (!reserved) {
            return false;
        }
        order.setStatus(OrderStatus.RESERVED);

        BigDecimal discount = discountService.calculateFinalDiscount(order, BigDecimal.ZERO, BigDecimal.valueOf(1000));
        order.setDiscountAmount(discount);

        boolean paid = paymentService.processPayment(order.getCustomerId(), order.getTotalAmount());
        if (!paid) {
            inventoryService.releaseAll(order.getItems());
            return false;
        }
        order.setStatus(OrderStatus.PAID);
        order.setStatus(OrderStatus.COMPLETED);
        return true;
    }

    public boolean cancelOrder(Order order) {
        if (order == null) {
            return false;
        }

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus != OrderStatus.PAID && currentStatus != OrderStatus.RESERVED && currentStatus != OrderStatus.COMPLETED) {
            return false;
        }

        order.setStatus(OrderStatus.CANCELLED);

        if (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.COMPLETED) {
            paymentService.refundPayment(order.getCustomerId(), order.getTotalAmount());
        }

        inventoryService.releaseAll(order.getItems());
        return true;
    }
}
