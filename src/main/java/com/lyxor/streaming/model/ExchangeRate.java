package com.lyxor.streaming.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class ExchangeRate {
    private final String baseCurrency;
    private final String targetCurrency;
    private final BigDecimal rate;
    private final Instant expiresAt;

    public ExchangeRate(String baseCurrency, String targetCurrency, BigDecimal rate, Instant expiresAt) {
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.targetCurrency = Objects.requireNonNull(targetCurrency);
        this.rate = Objects.requireNonNull(rate);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
