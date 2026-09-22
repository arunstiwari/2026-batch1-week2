package com.fil.week2.controller;

import com.fil.week2.model.Customer;
import com.fil.week2.service.OnboardingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }
    @PostMapping("/customers")
    public Customer onboarding(@RequestBody Customer customer) {
        return onboardingService.onboard(customer);
    }
}
