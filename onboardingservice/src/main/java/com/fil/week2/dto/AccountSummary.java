package com.fil.week2.dto;

import com.fil.week2.model.AccountNumber;
import com.fil.week2.model.AccountType;
import com.fil.week2.model.Money;

import java.time.Instant;

/**
 * A read projection. Deliberately not an {@code Account}: a managed entity returned
 * from a query is a writable handle, and Hibernate will flush changes made through
 * it without the aggregate root ever being involved.
 */
public record AccountSummary(String accountNumber,
                             AccountType accountType,
                             Money balance,
                             Instant openedAt) {

    /** What the JPQL constructor expression binds to; the wire keeps a plain string. */
    public AccountSummary(AccountNumber accountNumber, AccountType accountType, Money balance, Instant openedAt) {
        this(accountNumber.value(), accountType, balance, openedAt);
    }
}
