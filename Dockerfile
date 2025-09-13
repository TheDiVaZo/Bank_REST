# building
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY .env ./
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package
# ----

# test launcher
FROM eclipse-temurin:17-jdk-jammy AS test
WORKDIR /app

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
COPY --from=build /app .
# ----

# prod launcher
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/.env .
# ----

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]