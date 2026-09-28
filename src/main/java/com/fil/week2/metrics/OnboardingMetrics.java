package com.fil.week2.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.TimeWindowSum;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OnboardingMetrics {
    static final int WINDOW_BUCKETS = 5;
    static final Duration BUCKET_LENGTH = Duration.ofMinutes(1);

    private final AtomicLong pending = new AtomicLong();
    private Counter succeeded;
    private Counter failed;
    private Counter totalAttempts;

    private final TimeWindowSum recentAttempts;
    private final TimeWindowSum recentFailures;

    public OnboardingMetrics(MeterRegistry meterRegistry) {
        this.succeeded =  deriveCounterValue(meterRegistry, "succeeded");
        this.failed =  deriveCounterValue(meterRegistry, "failed");

        this.recentAttempts = new TimeWindowSum(WINDOW_BUCKETS, BUCKET_LENGTH);
        this.recentFailures = new TimeWindowSum(WINDOW_BUCKETS, BUCKET_LENGTH);

        this.totalAttempts = deriveCounterValue(meterRegistry, "totalAttempts");
    }

    private static Counter deriveCounterValue(MeterRegistry meterRegistry, String outcome) {
        return Counter.builder("onboarding.attempts")
                .tag("outcome", outcome)
                .description("Onboarding attempts since startup")
                .register(meterRegistry);
    }

    public void  recordSuccess() {
        succeeded.increment();
        recentAttempts.record(1);
    }

    public void  recordFailed() {
        failed.increment();
        recentAttempts.record(1);
        recentFailures.record(1);
    }

    public double failedRate() {
        double attempts = recentAttempts.poll();
        return attempts == 0 ? 0 : recentFailures.poll()/attempts;
    }
}
