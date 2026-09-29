package com.fil.week2.controller;

import com.fil.week2.dto.CustomerRequest;
import com.fil.week2.dto.CustomerResponse;
import com.fil.week2.service.CustomerService;
import com.fil.week2.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final CustomerService customerService;

    public OnboardingController(OnboardingService onboardingService, CustomerService customerService) {
        this.onboardingService = onboardingService;
        this.customerService = customerService;
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse onboarding(@Valid @RequestBody CustomerRequest customerRequest) {
        return CustomerResponse.from(onboardingService.onboard(customerRequest.toCustomer()));
    }

    @GetMapping("/customers")
    public List<CustomerResponse> findAllCustomers() {
        return customerService.findAllCustomer();
    }
}
