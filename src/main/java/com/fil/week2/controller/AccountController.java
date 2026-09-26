package com.fil.week2.controller;

import com.fil.week2.dto.AccountDepositResponse;
import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.dto.AccountResponse;
import com.fil.week2.dto.AccountSummary;
import com.fil.week2.dto.DepositRequest;
import com.fil.week2.dto.WithdrawalRequest;
import com.fil.week2.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/customers/{customerId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@PathVariable("customerId") Long customerId,
                                       @Valid @RequestBody AccountOpenRequest accountOpenRequest) {
        return accountService.openAccount(customerId, accountOpenRequest);
    }

    @GetMapping("/customers/{customerId}/accounts")
    public List<AccountSummary> findAccounts(@PathVariable("customerId") Long customerId) {
        return accountService.findAccounts(customerId);
    }

    @PostMapping("/customers/{customerId}/deposits")
    public AccountDepositResponse depositAmount(@PathVariable("customerId") Long customerId,
                                                @Valid @RequestBody DepositRequest depositRequest) {
        return accountService.depositAmount(customerId, depositRequest);
    }

    @PostMapping("/customers/{customerId}/withdrawals")
    public AccountDepositResponse withdrawAmount(@PathVariable("customerId") Long customerId,
                                                 @Valid @RequestBody WithdrawalRequest withdrawalRequest) {
        return accountService.withdrawAmount(customerId, withdrawalRequest);
    }
}
