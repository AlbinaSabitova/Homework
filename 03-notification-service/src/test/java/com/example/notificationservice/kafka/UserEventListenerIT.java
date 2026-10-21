package com.example.notificationservice.kafka;

import com.example.notificationservice.dto.UserEvent;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class UserEventListenerIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
            .withPerMethodLifecycle(false);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
        registry.add("spring.mail.username", () -> "");
        registry.add("spring.mail.password", () -> "");
    }

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @BeforeEach
    void cleanMailbox() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void userCreatedEvent_sendsCreationEmail() throws Exception {
        kafkaTemplate.send("user-events", "alice@mail.ru",
                new UserEvent(UserEvent.Operation.CREATED, "alice@mail.ru"));

        await().atMost(Duration.ofSeconds(10))
                .until(() -> greenMail.getReceivedMessages().length > 0);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString()).isEqualTo("alice@mail.ru");
        assertThat(messages[0].getSubject()).contains("создан");
        String body = (String) messages[0].getContent();
        assertThat(body).contains("успешно создан");
    }

    @Test
    void userDeletedEvent_sendsDeletionEmail() throws Exception {
        kafkaTemplate.send("user-events", "bob@mail.ru",
                new UserEvent(UserEvent.Operation.DELETED, "bob@mail.ru"));

        await().atMost(Duration.ofSeconds(10))
                .until(() -> greenMail.getReceivedMessages().length > 0);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages[0].getAllRecipients()[0].toString()).isEqualTo("bob@mail.ru");
        assertThat(messages[0].getSubject()).contains("удалён");
        String body = (String) messages[0].getContent();
        assertThat(body).contains("был удалён");
    }
}
