package com.fil.week2.service;

import com.fil.week2.model.Customer;
import com.fil.week2.model.Standing;
import com.fil.week2.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OnboardingServiceTest {

    @Autowired
    OnboardingService onboardingService;

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void onboardingIssuesAnIdentityAndLeavesTheCustomerActiveButUnverified() {
        Customer saved = onboardingService.onboard(new Customer("Demo User", "demo@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStanding()).isEqualTo(Standing.ACTIVE);
        assertThat(saved.currentVerification()).isEmpty();
    }

    @Test
    void anOnboardedCustomerIsRetrievable() {
        Customer saved = onboardingService.onboard(new Customer("Second User", "second@example.com"));
        customerRepository.flush();

        assertThat(customerRepository.findById(saved.getId()))
                .get()
                .extracting(Customer::getEmail)
                .isEqualTo("second@example.com");
    }
}
