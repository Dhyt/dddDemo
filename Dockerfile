FROM eclipse-temurin:17-jdk-alpine
ARG SERVICE
COPY ${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
