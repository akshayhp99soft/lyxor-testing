package com.lyxor.fulfillment.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

public class WalletAccount {
    private final String accountId;
    private BigDecimal balance;
    private BigDecimal reservedAmount;
    private final AtomicBoolean locked;

    public WalletAccount(String accountId, BigDecimal initialBalance) {
        this.accountId = accountId;
        this.balance = initialBalance;
        this.reservedAmount = BigDecimal.ZERO;
        this.locked = new AtomicBoolean(false);
    }

    public synchronized HoldResult holdFunds(BigDecimal amount) {
        if (locked.get()) {
            return HoldResult.ACCOUNT_LOCKED;
        }
        if (balance.compareTo(amount) < 0) {
            return HoldResult.INSUFFICIENT_FUNDS;
        }
        this.balance = this.balance.subtract(amount);
        this.reservedAmount = this.reservedAmount.add(amount);
        return HoldResult.SUCCESS;
    }

    public synchronized void commitHold(BigDecimal amount) {
        this.reservedAmount = this.reservedAmount.subtract(amount);
    }

    public synchronized void releaseHold(BigDecimal amount) {
        this.reservedAmount = this.reservedAmount.subtract(amount);
        this.balance = this.balance.add(amount);
    }

    public synchronized void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public String getAccountId() {
        return accountId;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public synchronized BigDecimal getReservedAmount() {
        return reservedAmount;
    }

    public boolean isLocked() {
        return locked.get();
    }

    public void setLocked(boolean isLocked) {
        this.locked.set(isLocked);
    }
}
