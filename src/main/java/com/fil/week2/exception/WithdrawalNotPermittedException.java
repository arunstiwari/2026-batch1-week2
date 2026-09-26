package com.fil.week2.exception;

/** Money may come in under a hold; none goes out. */
public class WithdrawalNotPermittedException extends RuntimeException {
    private final Long customerId;
    private final String reason;

    public WithdrawalNotPermittedException(Long customerId, String reason) {
        super("Customer " + customerId + " may not withdraw: " + reason);
        this.customerId = customerId;
        this.reason = reason;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getReason() {
        return reason;
    }
}
