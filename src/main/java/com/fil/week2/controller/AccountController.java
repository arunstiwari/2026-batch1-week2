package com.fil.week2.controller;

import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.dto.AccountResponse;
import com.fil.week2.service.AccountService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    private AccountService acccountService;

    public AccountController(AccountService acccountService){
        this.acccountService = acccountService;
    }

    @PostMapping("/customers/{customerId}/accounts")
    public AccountResponse openAccount(@PathVariable("customerId") Long customerId,
                                       @RequestBody AccountOpenRequest accountOpenRequest){

        AccountResponse accountResponse = acccountService.openAccount(customerId, accountOpenRequest);
        return accountResponse;
    }
}
