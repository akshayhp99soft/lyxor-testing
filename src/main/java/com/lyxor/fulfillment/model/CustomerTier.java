package com.lyxor.fulfillment.model;

public enum CustomerTier {
    REGULAR(0.00),
    SILVER(0.05),
    GOLD(0.15),
    PLATINUM(0.25);

    private final double discountRate;

    CustomerTier(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}
