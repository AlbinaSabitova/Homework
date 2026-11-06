\# Homework — Microservices System



A set of Spring Boot microservices demonstrating service discovery, centralized configuration, asynchronous messaging and container orchestration with Docker Compose.



\## Architecture



| Service | Description | Port |

| --- | --- | --- |

| \*\*user-service\*\* | REST API for users, Kafka producer, Swagger + HATEOAS | 8082 |

| \*\*notification-service\*\* | Kafka consumer, sends emails | 8081 |

| \*\*config-server\*\* | Spring Cloud Config Server (centralized configuration) | 8888 |

| \*\*eureka-server\*\* | Eureka Service Discovery | 8761 |

| \*\*api-gateway\*\* | Spring Cloud Gateway + Circuit Breaker | 8080 |

| \*\*PostgreSQL\*\* | Database for user-service | 5432 |

| \*\*Kafka\*\* | Async messaging between services | 9092 |

| \*\*MailHog\*\* | Local SMTP server for email testing | 8025 |



\## Tech Stack



\- Java 17, Spring Boot 3.3, Spring Cloud 2023.0.5

\- Spring Cloud Config, Eureka, Gateway, Resilience4j

\- PostgreSQL, Kafka, MailHog

\- Docker \& Docker Compose (healthchecks, service\_healthy dependencies)



\## Quick Start



All services start with a single command:



```bash

docker-compose up -d

```



Startup order is handled automatically: `config-server` runs first and exposes a healthcheck; the other services start only after it becomes healthy (`depends\_on` with `service\_healthy`), so they fetch their configuration on the first attempt.



\## Useful URLs



| URL | What's there |

| --- | --- |

| http://localhost:8080/swagger-ui.html | API Gateway |

| http://localhost:8761 | Eureka Dashboard |

| http://localhost:8025 | MailHog (email inbox) |



\## Testing the API



\### Create user (through the gateway)



```bash

curl -X POST http://localhost:8080/api/users \\

&#x20; -H "Content-Type: application/json" \\

&#x20; -d '{"name":"Albina","email":"albinasabitova@mail.ru","age":30}'

```



Expected response: \*\*201 Created\*\*



\### Get all users



```bash

curl http://localhost:8080/api/users

```



\### Send email directly



```bash

curl -X POST http://localhost:8080/api/notifications/send \\

&#x20; -H "Content-Type: application/json" \\

&#x20; -d '{"to":"test@mail.ru","subject":"Test","text":"Hello"}'

```



\### Verify email delivery



After creating a user or sending a test message, open MailHog at http://localhost:8025 — the notification-service message will appear there.



\## Services registered in Eureka



After a successful startup, check the Eureka Dashboard (http://localhost:8761): `user-service`, `notification-service` and `api-gateway` should be listed as registered instances.



