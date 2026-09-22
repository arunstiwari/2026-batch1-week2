package com.fil.week2.service;

import com.fil.week2.config.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationSender implements NotificationSender {
    private static Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);
    private int port;
    private String host;

    public EmailNotificationSender(AppConfiguration appConfiguration) {
        AppConfiguration.Mail mail = appConfiguration.mail();
        this.port = mail.port();
        this.host = mail.host();
    }

    @Override
    public void send(String to, String subject, String body) {
        logger.info("[Email]Sending email to {}, subject {}, body {}", to, subject, body);
    }
}
