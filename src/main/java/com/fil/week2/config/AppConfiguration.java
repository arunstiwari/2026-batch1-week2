package com.fil.week2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@ConfigurationProperties(prefix = "app")
public record AppConfiguration( Mail mail, Notification notification) {
    public static record Mail(String host, int port, String from) {}
    public static record Notification(int poolSize, Duration retryDelay, boolean enabled) {}

}
