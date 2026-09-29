package com.fil.week2.exception;

import java.math.BigDecimal;

/** An account may not be overdrawn. */
public class InsufficientFundsException extends RuntimeException {
    private final String accountNumber;
    private final BigDecimal requested;

    public InsufficientFundsException(String accountNumber, BigDecimal requested) {
        super("Account " + accountNumber + " has insufficient funds for a withdrawal of " + requested);
        this.accountNumber = accountNumber;
        this.requested = requested;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getRequested() {
        return requested;
    }
}
