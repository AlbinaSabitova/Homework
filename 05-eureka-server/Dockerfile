# ЭТАП 1: СБОРКА (Используем обычный JDK, там есть все сертификаты)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Копируем всё необходимое для сборки
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# Запускаем сборку. В этом образе SSL работает сразу, без apk и keytool
RUN ./mvnw -B clean package -DskipTests

# ЭТАП 2: ЗАПУСК (Лёгкий образ для продакшена/проверки)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем готовый JAR из первого этапа
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]

