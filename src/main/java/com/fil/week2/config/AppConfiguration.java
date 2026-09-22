package com.fil.week2.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppConfiguration(@Valid  Mail mail, @Valid Notification notification) {
    public static record Mail(@NotBlank String host, @Min(1) @Max(65525) int port, String from) {}
    public static record Notification(@Min(1) @Max(20) int poolSize, Duration retryDelay, boolean enabled) {}

}
