FROM eclipse-temurin:21-jre

WORKDIR /app

COPY rest/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]