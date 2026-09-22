package com.fil.week2;

import com.fil.week2.service.ConsoleNotificationSender;
import com.fil.week2.service.EmailNotificationSender;
import com.fil.week2.service.NotificationSender;
import com.fil.week2.service.OnboardingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Week2Application {

    public static void main(String[] args) {
        SpringApplication.run(Week2Application.class, args);
    }

//    @Bean
//    public OnboardingService onboardingService(NotificationSender notificationSender, ConsoleNotificationSender consoleNotificationSender) {
//        return new OnboardingService(consoleNotificationSender);
//    }

//    @Primary
//    @Bean
//    public NotificationSender consoleNotificationSender1() {
//        return new  ConsoleNotificationSender();
//    }

//  @Bean
//    public NotificationSender emailNotificationSender() {
//        return new EmailNotificationSender();
//    }

}
