FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/server-1.0-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-Dserver.address=0.0.0.0", "-Dserver.port=9090", "-jar", "app.jar"]