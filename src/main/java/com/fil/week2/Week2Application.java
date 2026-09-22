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
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Week2Application {

    public static void main(String[] args) {
        SpringApplication.run(Week2Application.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService executor() {
        return Executors.newFixedThreadPool(5);
    }

    @Scope("prototype")
    @Bean
    public StringBuilder builder() {
        return new StringBuilder();
    }
//    @Bean
//    public NotificationSender notificationSender() {}

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
