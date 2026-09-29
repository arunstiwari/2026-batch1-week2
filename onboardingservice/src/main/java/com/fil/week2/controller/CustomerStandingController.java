package com.fil.week2.controller;

import com.fil.week2.dto.CustomerResponse;
import com.fil.week2.service.CustomerService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A change of standing is an event, so it POSTs to its own sub-resource,
 * the same shape as a KYC decision.
 */
@RestController
public class CustomerStandingController {

    private final CustomerService customerService;

    public CustomerStandingController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customers/{customerId}/suspension")
    public CustomerResponse suspend(@PathVariable Long customerId) {
        return customerService.suspend(customerId);
    }

    @PostMapping("/customers/{customerId}/reinstatement")
    public CustomerResponse reinstate(@PathVariable Long customerId) {
        return customerService.reinstate(customerId);
    }
}
