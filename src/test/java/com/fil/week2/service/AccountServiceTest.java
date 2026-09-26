package com.fil.week2.service;

import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.dto.DepositRequest;
import com.fil.week2.exception.AccountNotFoundException;
import com.fil.week2.exception.AccountOpeningNotPermittedException;
import com.fil.week2.model.AccountType;
import com.fil.week2.model.Customer;
import com.fil.week2.model.Money;
import com.fil.week2.model.DocumentNumber;
import com.fil.week2.model.DocumentType;
import com.fil.week2.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The three holes Phase 0 closes. Each of these passed silently before the fix:
 * money moved between unrelated customers, accounts opened without verification,
 * and a negative deposit drained a balance past the overdraft guard.
 */
@SpringBootTest
@Transactional
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerRepository customerRepository;

    private Customer verifiedCustomer(String email) {
        Customer customer = new Customer("Verified Person", email);
        customerRepository.save(customer);
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P123456789"));
        customer.approveKyc("compliance-officer");
        customerRepository.flush();
        return customer;
    }

    private Customer unverifiedCustomer(String email) {
        Customer customer = new Customer("Unverified Person", email);
        customerRepository.save(customer);
        customerRepository.flush();
        return customer;
    }

    @Test
    void aCustomerCannotDepositIntoAnotherCustomersAccount() {
        Customer owner = verifiedCustomer("owner@example.com");
        Customer stranger = verifiedCustomer("stranger@example.com");

        String ownersAccount = accountService
                .openAccount(owner.getId(), new AccountOpenRequest(AccountType.SAVINGS))
                .accountNumber();

        assertThatExceptionOfType(AccountNotFoundException.class).isThrownBy(() ->
                accountService.depositAmount(stranger.getId(),
                        new DepositRequest(ownersAccount, new BigDecimal("500.00"))));
    }

    @Test
    void anUnverifiedCustomerCannotOpenAnAccount() {
        Customer customer = unverifiedCustomer("unverified@example.com");

        assertThatExceptionOfType(AccountOpeningNotPermittedException.class).isThrownBy(() ->
                accountService.openAccount(customer.getId(), new AccountOpenRequest(AccountType.CURRENT)));
    }

    @Test
    void aRejectedCustomerCannotOpenAnAccount() {
        Customer customer = unverifiedCustomer("rejected@example.com");
        customer.submitKyc(DocumentType.NATIONAL_ID, DocumentNumber.of("N987654321"));
        customer.rejectKyc("compliance-officer", "Document illegible");
        customerRepository.flush();

        assertThatExceptionOfType(AccountOpeningNotPermittedException.class).isThrownBy(() ->
                accountService.openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)));
    }

    /** Money cannot hold a negative, so this is refused before it reaches the account. */
    @Test
    void aNegativeDepositIsRefusedRatherThanTreatedAsAWithdrawal() {
        Customer customer = verifiedCustomer("negative@example.com");
        String account = accountService
                .openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS))
                .accountNumber();
        accountService.depositAmount(customer.getId(), new DepositRequest(account, new BigDecimal("100.00")));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                accountService.depositAmount(customer.getId(),
                        new DepositRequest(account, new BigDecimal("-500.00"))));
    }

    @Test
    void anAccountOpensAtZeroRegardlessOfWhatTheClientAsksFor() {
        Customer customer = verifiedCustomer("zero@example.com");

        assertThat(accountService.openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)).balance())
                .isEqualTo(Money.zero());
    }

    @Test
    void depositingIntoAnUnknownAccountFailsInsteadOfThrowingNullPointer() {
        Customer customer = verifiedCustomer("unknown@example.com");

        assertThatExceptionOfType(AccountNotFoundException.class).isThrownBy(() ->
                accountService.depositAmount(customer.getId(),
                        new DepositRequest("AC000000000000", new BigDecimal("10.00"))));
    }
}
