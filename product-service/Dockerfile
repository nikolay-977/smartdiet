FROM amazoncorretto:17-alpine
COPY build/libs/product-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]