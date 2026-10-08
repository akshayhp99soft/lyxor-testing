package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.HoldResult;
import com.lyxor.fulfillment.model.WalletAccount;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WalletPaymentService {

    private final Map<String, WalletAccount> accounts = new ConcurrentHashMap<>();

    public void registerAccount(String customerId, BigDecimal initialBalance) {
        accounts.put(customerId, new WalletAccount(customerId, initialBalance));
    }

    public WalletAccount getAccount(String customerId) {
        return accounts.get(customerId);
    }

    public boolean processPayment(String customerId, BigDecimal amount) {
        WalletAccount account = accounts.get(customerId);
        if (account == null) {
            return false;
        }

        HoldResult result = account.holdFunds(amount);
        if (result != HoldResult.INSUFFICIENT_FUNDS) {
            account.commitHold(amount);
            return true;
        }

        return false;
    }

    public void refundPayment(String customerId, BigDecimal amount) {
        WalletAccount account = accounts.get(customerId);
        if (account != null) {
            account.credit(amount);
        }
    }
}
