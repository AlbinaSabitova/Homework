package com.example.notificationservice.service.impl;

import com.example.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendUserCreatedEmail(String email) {
        send(email,
                "Аккаунт создан",
                "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
    }

    @Override
    public void sendUserDeletedEmail(String email) {
        send(email,
                "Аккаунт удалён",
                "Здравствуйте! Ваш аккаунт был удалён.");
    }

    @Override
    public void sendCustomEmail(String email, String subject, String text) {
        send(email, subject, text);
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Письмо отправлено на {}: [{}] {}", to, subject, text);
    }
}