package com.fil.week2.service;

import com.fil.week2.dto.AccountDepositResponse;
import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.dto.AccountResponse;
import com.fil.week2.dto.DepositRequest;
import com.fil.week2.exception.CustomerNotFoundException;
import com.fil.week2.model.Account;
import com.fil.week2.model.Customer;
import com.fil.week2.repository.AccountRepository;
import com.fil.week2.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class AccountService {
    private CustomerRepository customerRepository;
    private AccountRepository accountRepository;

    public AccountService(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse openAccount(Long customerId, AccountOpenRequest accountOpenRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer with id " + customerId + " does not exist in the system"));

        // Generate account number
        String accNumber = "AC%012d".formatted(Math.abs(new Random().nextLong() % 1000000000));
        // validate accNumber is already there in Account table.
        Account acc = accountRepository.findByAccountNumber(accNumber);
        if (acc != null) {
            //TODO we need to retry generating a new number
        }
        Account account = new Account(accNumber, accountOpenRequest.type(), accountOpenRequest.balance());
        customer.addAccount(account);
        customerRepository.flush();
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountDepositResponse depositAmount(Long customerId, DepositRequest depositRequest) {
        Account acc = getAcc(customerId, depositRequest);
        if (acc == null) {
            //TODO We can throw an Exception AccountNotFoundException();
        }
        System.out.println("depositAmount " + depositRequest.toString());
        acc.deposit(depositRequest.amount());
        customerRepository.flush();
        System.out.println("Account : " + acc);
        return new AccountDepositResponse("Balance is Updated successfully", acc.getBalance());
    }

    private Account getAcc(Long customerId, DepositRequest depositRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer with id " + customerId + " does not exist in the system"));

        // Validating the accountNumber passed in DepositRequest
        Account acc = accountRepository.findByAccountNumber(depositRequest.accountNumber());
        return acc;
    }

    @Transactional
    public AccountDepositResponse withdrawAmount(Long customerId, DepositRequest depositRequest) {
//        Customer customer = customerRepository.findById(customerId)
//                .orElseThrow(() ->
//                        new CustomerNotFoundException("Customer with id " + customerId + " does not exist in the system"));
//
//        // Validating the accountNumber passed in DepositRequest
        Account acc = getAcc(customerId, depositRequest);
        if (acc == null) {
            //TODO We can throw an Exception AccountNotFoundException();
        }
        acc.withdraw(depositRequest.amount());
        customerRepository.flush();
        return new AccountDepositResponse("Balance is Updated successfully", acc.getBalance());
    }
}
