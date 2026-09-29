package com.fil.week2.service;

import com.fil.week2.model.AccountNumber;

/**
 * Issues Account Numbers. Uniqueness is a domain rule, so it is enforced here
 * rather than left to a database constraint to discover at flush time. The
 * format rule lives on {@link AccountNumber} itself.
 */
public interface AccountNumberGenerator {
    AccountNumber generate();
}
