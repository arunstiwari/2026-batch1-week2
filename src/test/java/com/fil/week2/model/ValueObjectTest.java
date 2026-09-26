package com.fil.week2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ValueObjectTest {

    /** The reason Money exists rather than a bare BigDecimal. */
    @Test
    void moneyRefusesToCombineCurrencies() {
        Money pounds = Money.of(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        Money euros = Money.of(new BigDecimal("100.00"), Currency.getInstance("EUR"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pounds.plus(euros))
                .withMessageContaining("Cannot combine GBP with EUR");
    }

    @Test
    void moneyCannotBeNegative() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Money.of(new BigDecimal("-0.01")))
                .withMessageContaining("cannot be negative");
    }

    @Test
    void subtractingMoreThanYouHaveIsRefused() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Money.of("10.00").minus(Money.of("10.01")));
    }

    @Test
    void moneyIsHeldAtTheCurrencysScale() {
        assertThat(Money.of(new BigDecimal("10.005")).getAmount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(Money.of(new BigDecimal("10")).getAmount()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void moneyIsComparedByValue() {
        assertThat(Money.of("10.00")).isEqualTo(Money.of(new BigDecimal("10")));
        assertThat(Money.of("10.00")).isNotEqualTo(Money.of("10.01"));
    }

    @Test
    void anAccountNumberHasAFormat() {
        assertThat(AccountNumber.of("AC000000000042").value()).isEqualTo("AC000000000042");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> AccountNumber.of("12345"))
                .withMessageContaining("AC followed by 12 digits");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> AccountNumber.of("AC00000000004X"));
    }

    @Test
    void accountNumbersAreProducedInOnePlace() {
        assertThat(AccountNumber.fromSequence(42L)).isEqualTo(AccountNumber.of("AC000000000042"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> AccountNumber.fromSequence(-1L));
    }

    @Test
    void aDocumentNumberDisclosesOnlyItsLastFourCharacters() {
        assertThat(DocumentNumber.of("P1234567890").masked()).isEqualTo("*******7890");
        assertThat(DocumentNumber.of("123").masked()).isEqualTo("****");
        assertThat(DocumentNumber.of("P1234567890").value()).isEqualTo("P1234567890");
    }

    /** It ends up in logs, so the full value must never be its string form. */
    @Test
    void aDocumentNumberNeverPrintsItselfInFull() {
        assertThat(DocumentNumber.of("P1234567890")).hasToString("*******7890");
    }

    @Test
    void addressesWithTheSameContentsAreEqual() {
        Address one = new Address("1 High Street", "London", "SW1A 1AA");
        Address other = new Address("1 High Street", "London", "SW1A 1AA");

        assertThat(one).isEqualTo(other).hasSameHashCodeAs(other);
    }
}
