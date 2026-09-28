package com.fil.week2.metrics;

import com.fil.week2.repository.CustomerRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OnboardingHealthIndicator implements HealthIndicator {
    private CustomerRepository customerRepository;
    private OnboardingMetrics onboardingMetrics;

    public OnboardingHealthIndicator(OnboardingMetrics onboardingMetrics, CustomerRepository customerRepository) {
        this.onboardingMetrics = onboardingMetrics;
        this.customerRepository = customerRepository;
    }


    @Override
    public @Nullable Health health() {
        long total = customerRepository.count();
        double failedRate = onboardingMetrics.failedRate();
        System.out.println("failedRate: "+failedRate);
        Health.Builder builder = failedRate > 0.5 ? Health.up():  Health.down();
        return builder.withDetail("customers",total)
                .withDetail("failedRatio", failedRate)
                .build();

    }
}
