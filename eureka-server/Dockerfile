FROM amazoncorretto:17-alpine
COPY build/libs/eureka-server-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app.jar"]