FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/taskflow-api-0.0.1-SNAPSHOT.jar app.jar

# Run with an unprivileged numeric user; the app only needs to read the JAR.
USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
