package com.fil.week2.service;

import com.fil.week2.model.AccountNumber;
import com.fil.week2.repository.AccountQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

@Component
public class RandomAccountNumberGenerator implements AccountNumberGenerator {

    private static final int MAX_ATTEMPTS = 5;
    private static final long UPPER_BOUND = 1_000_000_000L;

    private final AccountQueries accountQueries;
    private final LongSupplier candidates;

    @Autowired
    public RandomAccountNumberGenerator(AccountQueries accountQueries) {
        this(accountQueries, () -> ThreadLocalRandom.current().nextLong(UPPER_BOUND));
    }

    /** The candidate source is injectable so that the collision path can be tested. */
    RandomAccountNumberGenerator(AccountQueries accountQueries, LongSupplier candidates) {
        this.accountQueries = accountQueries;
        this.candidates = candidates;
    }

    @Override
    public AccountNumber generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            AccountNumber candidate = AccountNumber.fromSequence(candidates.getAsLong());
            if (!accountQueries.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Could not allocate a unique account number in " + MAX_ATTEMPTS + " attempts");
    }
}
