package com.fil.week2.exception;

public class KycNotFoundException extends RuntimeException {
    private final Long customerId;

    public KycNotFoundException(Long customerId) {
        super("Customer " + customerId + " has not submitted a KYC verification");
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
