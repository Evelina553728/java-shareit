FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/shareit-server-1.0-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]