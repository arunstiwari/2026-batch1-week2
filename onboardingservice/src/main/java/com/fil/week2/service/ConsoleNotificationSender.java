package com.fil.week2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

//@Primary
@Service
public class ConsoleNotificationSender implements NotificationSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleNotificationSender.class);
    @Override
    public void send(String to, String subject, String body) {
        LOGGER.info("[console] Sending notification to {}, subject {}, body {}", to, subject, body);

    }
}
