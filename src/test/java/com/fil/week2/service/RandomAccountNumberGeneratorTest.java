package com.fil.week2.service;

import com.fil.week2.model.AccountNumber;
import com.fil.week2.repository.AccountQueries;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The collision branch used to be untestable: the random source was constructed
 * inside the method, so no test could force a duplicate. That is why the retry
 * was left as a //TODO that fell through and created the duplicate anyway.
 */
class RandomAccountNumberGeneratorTest {

    private static LongSupplier returning(Long... values) {
        List<Long> sequence = List.of(values);
        AtomicInteger cursor = new AtomicInteger();
        return () -> sequence.get(cursor.getAndIncrement());
    }

    @Test
    void retriesPastACollision() {
        AccountQueries repository = mock(AccountQueries.class);
        when(repository.existsByAccountNumber(AccountNumber.of("AC000000000001"))).thenReturn(true);
        when(repository.existsByAccountNumber(AccountNumber.of("AC000000000002"))).thenReturn(false);

        RandomAccountNumberGenerator generator =
                new RandomAccountNumberGenerator(repository, returning(1L, 2L));

        assertThat(generator.generate()).isEqualTo(AccountNumber.of("AC000000000002"));
    }

    @Test
    void givesUpRatherThanIssuingADuplicate() {
        AccountQueries repository = mock(AccountQueries.class);
        when(repository.existsByAccountNumber(any())).thenReturn(true);

        RandomAccountNumberGenerator generator =
                new RandomAccountNumberGenerator(repository, returning(1L, 1L, 1L, 1L, 1L));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(generator::generate)
                .withMessageContaining("Could not allocate a unique account number");
    }

    @Test
    void formatsTheNumberToTwelveDigits() {
        AccountQueries repository = mock(AccountQueries.class);
        when(repository.existsByAccountNumber(any())).thenReturn(false);

        RandomAccountNumberGenerator generator =
                new RandomAccountNumberGenerator(repository, returning(42L));

        assertThat(generator.generate()).isEqualTo(AccountNumber.of("AC000000000042"));
    }
}
