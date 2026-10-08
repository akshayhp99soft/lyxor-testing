package com.lyxor.streaming.service;

import com.lyxor.fulfillment.model.HoldResult;
import com.lyxor.fulfillment.model.WalletAccount;
import com.lyxor.streaming.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiCurrencyLedger {

    private final Map<String, ExchangeRate> exchangeRates = new ConcurrentHashMap<>();

    public void registerRate(ExchangeRate rate) {
        String pair = rate.getBaseCurrency() + "_" + rate.getTargetCurrency();
        exchangeRates.put(pair, rate);
    }

    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        String pair = fromCurrency + "_" + toCurrency;
        ExchangeRate rate = exchangeRates.get(pair);
        if (rate == null || rate.isExpired()) {
            throw new IllegalArgumentException("Unsupported or expired currency pair: " + pair);
        }

        return amount.multiply(rate.getRate()).setScale(2, RoundingMode.FLOOR);
    }

    public boolean transferFunds(WalletAccount fromAccount, WalletAccount toAccount, BigDecimal amount) {
        if (fromAccount == null || toAccount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        synchronized (fromAccount) {
            synchronized (toAccount) {
                HoldResult result = fromAccount.holdFunds(amount);
                if (result == HoldResult.SUCCESS) {
                    fromAccount.commitHold(amount);
                    toAccount.credit(amount);
                    return true;
                }
                return false;
            }
        }
    }
}
