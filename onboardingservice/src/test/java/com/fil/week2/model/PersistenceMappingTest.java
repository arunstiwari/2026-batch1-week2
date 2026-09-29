package com.fil.week2.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The system is single-currency, but the currency is stored rather than assumed,
 * so the schema states the fact and multi-currency needs no migration.
 */
@SpringBootTest
class PersistenceMappingTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void aBalanceIsStoredAsAnAmountAndACurrency() {
        assertThat(jdbcTemplate.queryForList(
                "select column_name from information_schema.columns "
                        + "where upper(table_name) = 'ACCOUNT' and upper(column_name) like 'BALANCE%'",
                String.class))
                .extracting(String::toUpperCase)
                .containsExactlyInAnyOrder("BALANCE_AMOUNT", "BALANCE_CURRENCY");
    }

    /**
     * The @Table constraint used to name the property, "accountNumber", rather than
     * the column. Hibernate resolved it through the naming strategy, but tooling
     * could not, so the constraint now names account_number. This asserts the
     * database really does refuse a duplicate.
     */
    @Test
    void theDatabaseRefusesADuplicateAccountNumber() {
        jdbcTemplate.update("insert into customer (id, name, email, standing, version, created_at, updated_at) "
                + "values (9001, 'Dup Holder', 'dup@example.com', 'ACTIVE', 0, now(), now())");
        String insert = "insert into account "
                + "(id, account_number, account_type, balance_amount, balance_currency, customer_id, version, created_at, updated_at) "
                + "values (?, 'AC000000009999', 'SAVINGS', 0.00, 'GBP', 9001, 0, now(), now())";
        jdbcTemplate.update(insert, 9001);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> jdbcTemplate.update(insert, 9002));
    }

    /** The public identity of an account carried no NOT NULL constraint until now. */
    @Test
    void anAccountCannotExistWithoutAnAccountNumber() {
        jdbcTemplate.update("insert into customer (id, name, email, standing, version, created_at, updated_at) "
                + "values (9101, 'Null Holder', 'null-number@example.com', 'ACTIVE', 0, now(), now())");

        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() ->
                jdbcTemplate.update("insert into account "
                        + "(id, account_number, account_type, balance_amount, balance_currency, customer_id, version, created_at, updated_at) "
                        + "values (9101, null, 'SAVINGS', 0.00, 'GBP', 9101, 0, now(), now())"));
    }

    /**
     * The command path builds Money through its factory (scale 2); the read path has
     * Hibernate inject fields straight from the column. If the column's scale is not
     * pinned, the same balance serialises two different ways.
     */
    @Test
    void theBalanceColumnPinsItsScale() {
        assertThat(jdbcTemplate.queryForObject(
                "select numeric_scale from information_schema.columns "
                        + "where upper(table_name) = 'ACCOUNT' and upper(column_name) = 'BALANCE_AMOUNT'",
                Integer.class))
                .isEqualTo(2);
    }
}
