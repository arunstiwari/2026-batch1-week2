package com.fil.week2.controller;

import com.fil.week2.dto.CustomerResponse;
import com.fil.week2.model.Customer;
import com.fil.week2.service.CustomerService;
import com.fil.week2.service.OnboardingService;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OnboardingController {

    private final OnboardingService onboardingService;
    private CustomerService customerService;

    public OnboardingController(OnboardingService onboardingService, CustomerService customerService) {
        this.onboardingService = onboardingService;
        this.customerService = customerService;
    }
    @PostMapping("/customers")
    public Customer onboarding(@RequestBody Customer customer) {
        return onboardingService.onboard(customer);
    }

    @GetMapping("/customers")
    public List<CustomerResponse> findAllCustomers() {
        return customerService.findAllCustomer();
    }
}
