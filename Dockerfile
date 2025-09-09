# building
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY .env ./
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src
RUN ./mvnw dependency:go-offline
RUN ./mvnw clean package -DskipTests
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

COPY --from=build /app/target/app.jar app.jar
COPY --from=build /app/.env .
# ----

EXPOSE 8080
ENTRYPOINT ["app.jar", "java", "-jar"]