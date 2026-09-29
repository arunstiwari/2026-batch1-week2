package com.fil.week2.exception;

public class CustomerNotFoundException extends RuntimeException {
    private final Long customerId;

    public CustomerNotFoundException(Long customerId) {
        super("Customer " + customerId + " does not exist in the system");
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
