package com.fil.week2.model;

/**
 * Whether a Customer's relationship is in good order. Says nothing about whether
 * their identity is verified: that is {@link KycStatus}, asked separately.
 */
public enum Standing {
    ACTIVE, SUSPENDED, CLOSED
}
