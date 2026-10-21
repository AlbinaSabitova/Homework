package com.example.notificationservice.service;

public interface EmailService {
    void sendUserCreatedEmail(String email);
    void sendUserDeletedEmail(String email);
    void sendCustomEmail(String email, String subject, String text);
}