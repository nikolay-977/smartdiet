FROM amazoncorretto:17-alpine
COPY build/libs/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app.jar"]