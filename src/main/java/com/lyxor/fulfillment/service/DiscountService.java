package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.CustomerTier;
import com.lyxor.fulfillment.model.Order;
import com.lyxor.fulfillment.model.OrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiscountService {

    public BigDecimal calculateTierDiscount(Order order) {
        CustomerTier tier = order.getCustomerTier();
        if (tier == null || tier.getDiscountRate() <= 0.0) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        return subtotal.multiply(BigDecimal.valueOf(tier.getDiscountRate()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinalDiscount(Order order, BigDecimal couponAmount, BigDecimal maxAllowedDiscount) {
        BigDecimal tierDiscount = calculateTierDiscount(order);
        
        if (couponAmount == null || couponAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return tierDiscount.min(maxAllowedDiscount);
        }

        BigDecimal applicableCoupon = couponAmount;
        if (applicableCoupon.compareTo(maxAllowedDiscount) > 0) {
            applicableCoupon = maxAllowedDiscount;
        }

        return tierDiscount.add(applicableCoupon);
    }
}
