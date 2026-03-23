FROM amazoncorretto:17-alpine
COPY build/libs/recommendation-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]