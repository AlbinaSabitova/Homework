package com.example.notificationservice.controller;

import com.example.notificationservice.dto.EmailRequest;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationControllerIT {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
            .withPerMethodLifecycle(false);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
        registry.add("spring.mail.username", () -> "");
        registry.add("spring.mail.password", () -> "");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void cleanMailbox() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void sendEmail_viaRestApi_deliversMessage() throws Exception {
        EmailRequest request = new EmailRequest("test@mail.ru", "Test Subject", "Hello, World!");

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/notifications/send", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        await().atMost(Duration.ofSeconds(5))
                .until(() -> greenMail.getReceivedMessages().length > 0);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("test@mail.ru");
        assertThat(message.getSubject()).isEqualTo("Test Subject");

        String body = (String) message.getContent();
        assertThat(body).isEqualTo("Hello, World!");
    }

    @Test
    void sendEmail_invalidRequest_returns400() {
        EmailRequest request = new EmailRequest("not-an-email", "", "");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/notifications/send", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Ошибка валидации");
        assertThat(response.getBody()).contains("details");
    }
}