package com.fil.week2.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class ApplicationBeansTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void verifyingOnboardingServiceBeanIsSingleton() {
        OnboardingService onboardingService = applicationContext.getBean(OnboardingService.class);
        OnboardingService onboardingService1 = applicationContext.getBean(OnboardingService.class);
        Assertions.assertThat(onboardingService).isSameAs(onboardingService1);
        System.out.println("onboardingService hashcode: "+onboardingService.hashCode());
        System.out.println("onboardingService1 hashcode: "+onboardingService1.hashCode());
    }

    @Test
    void verifyingStringBuilderIsPrototype() {
        StringBuilder builder = applicationContext.getBean(StringBuilder.class);
        StringBuilder builder1 = applicationContext.getBean(StringBuilder.class);
        Assertions.assertThat(builder).isNotSameAs(builder1);
        System.out.println("builder hashcode: "+builder.hashCode());
        System.out.println("builder1 hashcode: "+builder1.hashCode());
    }
}
