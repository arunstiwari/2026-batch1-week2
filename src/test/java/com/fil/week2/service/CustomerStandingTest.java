package com.fil.week2.service;

import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.dto.DepositRequest;
import com.fil.week2.dto.WithdrawalRequest;
import com.fil.week2.exception.AccountOpeningNotPermittedException;
import com.fil.week2.exception.InvalidStateTransitionException;
import com.fil.week2.exception.WithdrawalNotPermittedException;
import com.fil.week2.model.AccountType;
import com.fil.week2.model.Customer;
import com.fil.week2.model.Money;
import com.fil.week2.model.DocumentNumber;
import com.fil.week2.model.DocumentType;
import com.fil.week2.model.KycStatus;
import com.fil.week2.model.KycVerification;
import com.fil.week2.model.Standing;
import com.fil.week2.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Standing and verification are two independent questions. A customer can be
 * fully verified and suspended at the same time - the state the old fused
 * CustomerStatus enum could not express.
 */
@SpringBootTest
@Transactional
class CustomerStandingTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerRepository customerRepository;

    private Customer verified(String email) {
        Customer customer = new Customer("Verified Person", email);
        customerRepository.save(customer);
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P123456789"));
        customer.approveKyc("compliance-officer");
        customerRepository.flush();
        return customer;
    }

    @Test
    void approvingKycDoesNotTouchStanding() {
        Customer customer = new Customer("Fresh Person", "fresh@example.com");
        customerRepository.save(customer);
        assertThat(customer.getStanding()).isEqualTo(Standing.ACTIVE);

        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P000111222"));
        customer.approveKyc("compliance-officer");

        assertThat(customer.currentVerification()).get()
                .extracting(KycVerification::getStatus).isEqualTo(KycStatus.APPROVED);
        assertThat(customer.getStanding()).isEqualTo(Standing.ACTIVE);
    }

    @Test
    void aSuspendedCustomerIsStillVerified() {
        Customer customer = verified("suspended-but-verified@example.com");
        customerService.suspend(customer.getId());

        assertThat(customer.getStanding()).isEqualTo(Standing.SUSPENDED);
        assertThat(customer.currentVerification()).get()
                .extracting(KycVerification::getStatus).isEqualTo(KycStatus.APPROVED);
    }

    @Test
    void aSuspendedCustomerCannotOpenAnAccount() {
        Customer customer = verified("no-opening@example.com");
        customerService.suspend(customer.getId());

        assertThatExceptionOfType(AccountOpeningNotPermittedException.class).isThrownBy(() ->
                accountService.openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)));
    }

    @Test
    void aSuspendedCustomerCannotWithdraw() {
        Customer customer = verified("no-withdrawal@example.com");
        String account = accountService
                .openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)).accountNumber();
        accountService.depositAmount(customer.getId(), new DepositRequest(account, new BigDecimal("500.00")));
        customerService.suspend(customer.getId());

        assertThatExceptionOfType(WithdrawalNotPermittedException.class).isThrownBy(() ->
                accountService.withdrawAmount(customer.getId(),
                        new WithdrawalRequest(account, new BigDecimal("100.00"))));
    }

    /** Money can come in under a hold; none goes out. */
    @Test
    void aSuspendedCustomerCanStillReceiveMoney() {
        Customer customer = verified("can-receive@example.com");
        String account = accountService
                .openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)).accountNumber();
        customerService.suspend(customer.getId());

        assertThat(accountService.depositAmount(customer.getId(),
                new DepositRequest(account, new BigDecimal("250.00"))).balance())
                .isEqualTo(Money.of("250.00"));
    }

    @Test
    void reinstatementRestoresWithdrawals() {
        Customer customer = verified("reinstated@example.com");
        String account = accountService
                .openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)).accountNumber();
        accountService.depositAmount(customer.getId(), new DepositRequest(account, new BigDecimal("500.00")));
        customerService.suspend(customer.getId());
        customerService.reinstate(customer.getId());

        assertThat(accountService.withdrawAmount(customer.getId(),
                new WithdrawalRequest(account, new BigDecimal("200.00"))).balance())
                .isEqualTo(Money.of("300.00"));
    }

    @Test
    void anActiveCustomerCannotBeReinstated() {
        Customer customer = verified("already-active@example.com");

        assertThatExceptionOfType(InvalidStateTransitionException.class)
                .isThrownBy(() -> customerService.reinstate(customer.getId()));
    }

    @Test
    void aSuspendedCustomerCannotBeSuspendedAgain() {
        Customer customer = verified("double-suspend@example.com");
        customerService.suspend(customer.getId());

        assertThatExceptionOfType(InvalidStateTransitionException.class)
                .isThrownBy(() -> customerService.suspend(customer.getId()));
    }
}
