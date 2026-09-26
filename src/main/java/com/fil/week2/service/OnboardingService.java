package com.fil.week2.service;

import com.fil.week2.model.Customer;
import com.fil.week2.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class OnboardingService {
    private final NotificationSender notificationSender;
    private final ExecutorService executor;
    private final CustomerRepository customerRepository;

    public OnboardingService(NotificationSender notificationSender,
                             ExecutorService executor,
                             CustomerRepository customerRepository) {
        this.notificationSender = notificationSender;
        this.executor = executor;
        this.customerRepository = customerRepository;
    }

    /**
     * Onboarding does not verify anyone: a new customer is in good standing and
     * unverified, and those are two separate facts.
     */
    public Customer onboard(Customer customer) {
        Customer saved = customerRepository.save(customer);

        executor.submit(() ->
                notificationSender.send(saved.getEmail(),
                        "Welcome to the bank, " + saved.getName(),
                        "Next step: submit your identity documents so we can verify your account."));
        return saved;
    }
}
