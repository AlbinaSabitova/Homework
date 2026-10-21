package com.example.notificationservice.kafka;

import com.example.notificationservice.dto.UserEvent;
import com.example.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    private final EmailService emailService;

    public UserEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserEvent(UserEvent event) {
        log.info("Получено событие: {}", event);

        switch (event.operation()) {
            case CREATED -> emailService.sendUserCreatedEmail(event.email());
            case DELETED -> emailService.sendUserDeletedEmail(event.email());
            default -> log.warn("Неизвестная операция: {}", event.operation());
        }
    }
}