package com.fil.week2.dto;

import com.fil.week2.model.Account;
import com.fil.week2.model.AccountType;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(Long id,
                              String accountNumber,
                              AccountType accountType,
                              BigDecimal balance,
                              long version,
                              Long customerId,
                              Instant createdAt,
                              Instant updatedAt) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getVersion(),
                account.getCustomer().getId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
