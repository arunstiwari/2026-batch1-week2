package com.fil.week2.exception;

public class AccountNotFoundException extends RuntimeException {
    private final Long customerId;
    private final String accountNumber;

    public AccountNotFoundException(Long customerId, String accountNumber) {
        super("Customer " + customerId + " holds no account " + accountNumber);
        this.customerId = customerId;
        this.accountNumber = accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
