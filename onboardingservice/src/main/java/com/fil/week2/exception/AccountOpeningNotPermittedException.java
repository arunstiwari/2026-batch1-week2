package com.fil.week2.exception;

/** The Account Opening Policy refused: see {@code reason}. */
public class AccountOpeningNotPermittedException extends RuntimeException {
    private final Long customerId;
    private final String reason;

    public AccountOpeningNotPermittedException(Long customerId, String reason) {
        super("Customer " + customerId + " may not open an account: " + reason);
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
