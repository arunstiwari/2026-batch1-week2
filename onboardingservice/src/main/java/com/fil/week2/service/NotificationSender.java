package com.fil.week2.service;

public interface NotificationSender {
    void send(String to, String subject, String body);

}
