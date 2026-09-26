package com.fil.week2.service;

import com.fil.week2.dto.AccountDepositResponse;
import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.dto.AccountResponse;
import com.fil.week2.dto.AccountSummary;
import com.fil.week2.dto.DepositRequest;
import com.fil.week2.dto.WithdrawalRequest;
import com.fil.week2.exception.CustomerNotFoundException;
import com.fil.week2.model.Account;
import com.fil.week2.model.AccountNumber;
import com.fil.week2.model.Customer;
import com.fil.week2.model.Money;
import com.fil.week2.repository.AccountQueries;
import com.fil.week2.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestration only. Every rule about accounts lives on the Customer root, which
 * is what stops a caller from forgetting one.
 */
@Service
public class AccountService {
    private final CustomerRepository customerRepository;
    private final AccountQueries accountQueries;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountService(CustomerRepository customerRepository,
                          AccountQueries accountQueries,
                          AccountNumberGenerator accountNumberGenerator) {
        this.customerRepository = customerRepository;
        this.accountQueries = accountQueries;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    @Transactional
    public AccountResponse openAccount(Long customerId, AccountOpenRequest accountOpenRequest) {
        Customer customer = requireCustomer(customerId);
        Account account = customer.openAccount(accountOpenRequest.type(), accountNumberGenerator.generate());
        customerRepository.flush();
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountDepositResponse depositAmount(Long customerId, DepositRequest depositRequest) {
        Account account = requireCustomer(customerId).depositTo(
                AccountNumber.of(depositRequest.accountNumber()), Money.of(depositRequest.amount()));
        customerRepository.flush();
        return new AccountDepositResponse("Deposit accepted", account.getBalance());
    }

    @Transactional
    public AccountDepositResponse withdrawAmount(Long customerId, WithdrawalRequest withdrawalRequest) {
        Account account = requireCustomer(customerId).withdrawFrom(
                AccountNumber.of(withdrawalRequest.accountNumber()), Money.of(withdrawalRequest.amount()));
        customerRepository.flush();
        return new AccountDepositResponse("Withdrawal accepted", account.getBalance());
    }

    /** Reads go around the root, but only ever as projections. */
    @Transactional(readOnly = true)
    public List<AccountSummary> findAccounts(Long customerId) {
        requireCustomer(customerId);
        return accountQueries.findSummaries(customerId);
    }

    private Customer requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}
