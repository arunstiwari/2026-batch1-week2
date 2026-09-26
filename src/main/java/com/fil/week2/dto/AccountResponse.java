package com.fil.week2.dto;

import com.fil.week2.model.Account;
import com.fil.week2.model.AccountType;
import com.fil.week2.model.Money;

import java.time.Instant;

/**
 * No surrogate key: the Account Number is the account's public identity, and
 * publishing a sequential database id beside it muddles which one is canonical.
 */
public record AccountResponse(String accountNumber,
                              AccountType accountType,
                              Money balance,
                              long version,
                              Long customerId,
                              Instant createdAt,
                              Instant updatedAt) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountNumber().value(),
                account.getAccountType(),
                account.getBalance(),
                account.getVersion(),
                account.getCustomer().getId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
