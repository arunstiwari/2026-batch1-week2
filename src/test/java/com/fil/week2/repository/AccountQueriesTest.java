package com.fil.week2.repository;

import com.fil.week2.model.Account;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The rule that keeps a read path legitimate is that it cannot hand back something
 * you can mutate and save. A managed entity returned from any query is a writable
 * handle, and Hibernate's dirty checking will flush changes made through it without
 * the aggregate root ever being involved - a comment saying "read only" stops
 * nobody. This asserts the property structurally.
 */
class AccountQueriesTest {

    @Test
    void noQueryHandsBackAWritableAccount() {
        assertThat(Arrays.stream(AccountQueries.class.getMethods())
                .filter(method -> referencesAccount(method.getGenericReturnType()))
                .map(Method::getName))
                .as("methods on AccountQueries returning the Account entity")
                .isEmpty();
    }

    private static boolean referencesAccount(Type type) {
        if (type instanceof Class<?> clazz) {
            return Account.class.equals(clazz);
        }
        if (type instanceof ParameterizedType parameterized) {
            return Arrays.stream(parameterized.getActualTypeArguments())
                    .anyMatch(AccountQueriesTest::referencesAccount);
        }
        return false;
    }

    /** Extending JpaRepository would inherit save() and findById() and undo all of it. */
    @Test
    void theQuerySideIsNotAJpaRepository() {
        assertThat(Arrays.stream(AccountQueries.class.getMethods()).map(Method::getName))
                .doesNotContain("save", "saveAll", "findById", "findAll", "delete", "deleteById", "getReferenceById");
    }
}
