02-user-service
REST API for users, Kafka producer, Swagger + HATEOAS

03-notification-service
Kafka consumer, sends emails

04-config-server
Spring Cloud Config Server

05-eureka-server
Eureka Service Discovery

06-api-gateway
Spring Cloud Gateway + Circuit Breaker

1. Infrastructure (Postgres, Kafka, MailHog)
docker-compose up -d

2. Start services in this order:
cd 04-config-server      && mvn spring-boot:run   # :8888
cd 05-eureka-server      && mvn spring-boot:run   # :8761
cd 02-user-service       && mvn spring-boot:run   # :8082
cd 03-notification-service && mvn spring-boot:run # :8081
cd 06-api-gateway        && mvn spring-boot:run   # :8080

- API Gateway: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Eureka Dashboard: http://localhost:8761
- MailHog: http://localhost:8025

Test the API (via Gateway)

Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Albina","email":"albinasabitova@mail.ru","age":30}'

Get all users
curl http://localhost:8080/api/users

Send email directly
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"to":"test@mail.ru","subject":"Test","text":"Hello"}'
