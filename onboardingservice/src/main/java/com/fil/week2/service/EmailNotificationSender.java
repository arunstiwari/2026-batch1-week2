package com.fil.week2.service;

import com.fil.week2.config.AppConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Primary
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
        logger.info("host : {}", host);
        logger.info("[Email]Sending email to {}, subject {}, body {}", to, subject, body);
    }

    @PostConstruct
    public void init() {
        logger.info("[Email]Initializing email notification sender");
    }

    @PreDestroy
    public void destroy() {
        logger.info("[Email]Destroying email notification sender. So cleanup your SMTP connections");
    }
}
