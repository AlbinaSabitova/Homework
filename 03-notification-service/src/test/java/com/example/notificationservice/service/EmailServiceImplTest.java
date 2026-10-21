package com.example.notificationservice.service;

import com.example.notificationservice.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, "noreply@example.com");
    }

    @Test
    @DisplayName("sendUserCreatedEmail: отправляет письмо с текстом про создание")
    void sendUserCreatedEmail_sendsCorrectMessage() {
        emailService.sendUserCreatedEmail("alice@mail.ru");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("noreply@example.com");
        assertThat(message.getTo()).containsExactly("alice@mail.ru");
        assertThat(message.getSubject()).contains("создан");
        assertThat(message.getText()).contains("успешно создан");
    }

    @Test
    @DisplayName("sendUserDeletedEmail: отправляет письмо с текстом про удаление")
    void sendUserDeletedEmail_sendsCorrectMessage() {
        emailService.sendUserDeletedEmail("bob@mail.ru");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("bob@mail.ru");
        assertThat(message.getSubject()).contains("удалён");
        assertThat(message.getText()).contains("был удалён");
    }
}