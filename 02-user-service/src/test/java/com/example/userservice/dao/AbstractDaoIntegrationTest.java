package com.example.userservice.dao;

import com.example.userservice.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractDaoIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    protected static SessionFactory sessionFactory;

    @BeforeAll
    static void startContainerAndFactory() {
        POSTGRES.start();

        sessionFactory = new Configuration()
                .configure("hibernate-test.cfg.xml")
                .addAnnotatedClass(User.class)
                .setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl())
                .setProperty("hibernate.connection.username", POSTGRES.getUsername())
                .setProperty("hibernate.connection.password", POSTGRES.getPassword())
                .buildSessionFactory();
    }

    @AfterAll
    static void stopFactoryAndContainer() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        POSTGRES.stop();
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE")
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }
}
