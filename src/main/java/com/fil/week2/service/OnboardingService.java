package com.fil.week2.service;

import com.fil.week2.model.Customer;
import org.springframework.stereotype.Service;

@Service
public class OnboardingService {
    private NotificationSender notificationSender;

    public OnboardingService(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public String onboard(Customer customer){
        notificationSender.send("to","subject","body");
        return "Notification Sent";
    }
}
