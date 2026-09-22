package com.fil.week2.service;

import com.fil.week2.config.AppConfiguration;
import com.fil.week2.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@SpringBootTest
class OnboardingServiceTest {

    // The email bean from the context (it is a @Service, so Spring created it
    // with the AppConfiguration bound from application.properties).
    @Autowired
    EmailNotificationSender emailNotificationSender;

//    @Test
//    void onboardsWithEmailSenderFromContext() {
//        // Constructor injection done by hand: this is the injection point,
//        // so we decide which implementation goes in.
//        OnboardingService service = new OnboardingService(emailNotificationSender);
//
//        service.onboard(new Customer("ABC", "name", "demo@abc.com"));
//    }

//    @Test
//    void onboardsWithEmailSenderWithoutSpring() {
//        // No container at all - fastest, and nothing can override the choice.
//        AppConfiguration config = new AppConfiguration(
//                new AppConfiguration.Mail("localhost", 25,"welcome@demo.com"),
//                new AppConfiguration.Notification(2, java.time.Duration.ofSeconds(2), false));
//
//        OnboardingService service = new OnboardingService(new EmailNotificationSender(config));
//
//        service.onboard(new Customer("ABC", "name", "demo@abc.com"));
//    }


    @Autowired
    OnboardingService onboardingService;

    @Autowired
    AppConfiguration appConfiguration;
    @Test
    void onboardingServiceTest() {
        onboardingService.onboard(new Customer(Long.valueOf(123), "Demo User", "demo@example.com"));
        System.out.println("Pool Size: "+appConfiguration.notification().poolSize());
    }


}
