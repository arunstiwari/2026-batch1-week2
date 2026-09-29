package com.fil.week2.aop;

import com.fil.week2.metrics.OnboardingMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class OnboardingsMetricAspect {

    private final OnboardingMetrics metrics;

    public OnboardingsMetricAspect(OnboardingMetrics metrics) {
        this.metrics = metrics;
    }

    @Pointcut("execution(* com.fil.week2.service.OnboardingService.onboard(..))")
    public void onboarding() {
    }

    @Around("onboarding()")
    public Object record(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            metrics.recordSuccess();
            return result;
        } catch (Throwable failure) {
            // Record and rethrow, never swallow: the caller's error handling must still see it.
            metrics.recordFailed();
            throw failure;
        }
    }
}
