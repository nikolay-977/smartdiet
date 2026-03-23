FROM amazoncorretto:17-alpine
COPY build/libs/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]