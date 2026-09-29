package com.fil.week2.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The public identity of an Account: the number a Customer quotes. The format rule
 * lives here rather than in the code that happens to issue one.
 */
public final class AccountNumber {

    private static final Pattern FORMAT = Pattern.compile("AC\\d{12}");

    private final String value;

    private AccountNumber(String value) {
        this.value = value;
    }

    public static AccountNumber of(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "An account number is AC followed by 12 digits, not " + value);
        }
        return new AccountNumber(value);
    }

    /** The one place the format is produced, so issuing code cannot invent its own. */
    public static AccountNumber fromSequence(long sequence) {
        if (sequence < 0 || sequence > 999_999_999_999L) {
            throw new IllegalArgumentException("Out of range for an account number: " + sequence);
        }
        return new AccountNumber("AC%012d".formatted(sequence));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value.equals(((AccountNumber) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
