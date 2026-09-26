package com.fil.week2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An amount in a single currency. Never negative, always at the currency's scale,
 * and impossible to combine with a different currency - which is the whole reason
 * the type exists rather than a bare {@link BigDecimal}.
 * <p>
 * The system is single-currency. The currency is stored rather than assumed, so
 * the schema states the fact and multi-currency needs no migration.
 */
@Embeddable
public class Money implements Comparable<Money> {

    public static final Currency SYSTEM_CURRENCY = Currency.getInstance("GBP");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    protected Money() { }

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency.getCurrencyCode();
    }

    public static Money of(BigDecimal amount) {
        return of(amount, SYSTEM_CURRENCY);
    }

    public static Money of(String amount) {
        return of(new BigDecimal(amount), SYSTEM_CURRENCY);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("An amount is required");
        }
        Objects.requireNonNull(currency, "A currency is required");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
        return new Money(amount.setScale(currency.getDefaultFractionDigits(), ROUNDING), currency);
    }

    public static Money zero() {
        return of(BigDecimal.ZERO);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Money plus(Money other) {
        return new Money(amount.add(sameCurrency(other).amount), currencyOf(this));
    }

    /** Callers must establish that this is enough: Money is never negative. */
    public Money minus(Money other) {
        BigDecimal remaining = amount.subtract(sameCurrency(other).amount);
        if (remaining.signum() < 0) {
            throw new IllegalArgumentException(
                    "Subtracting " + other + " from " + this + " would make money negative");
        }
        return new Money(remaining, currencyOf(this));
    }

    /** Deliberately not named isZero: that is a bean getter, and it leaked into the API. */
    public boolean hasZeroAmount() {
        return amount.signum() == 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(sameCurrency(other).amount);
    }

    private Money sameCurrency(Money other) {
        Objects.requireNonNull(other, "An amount is required");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot combine " + currency + " with " + other.currency);
        }
        return other;
    }

    private static Currency currencyOf(Money money) {
        return Currency.getInstance(money.currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}
