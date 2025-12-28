# Build stage
FROM gradle:8.7-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN gradle :products-service:bootJar --no-daemon

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built jar
COPY --from=build /app/products-service/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8082/api/products/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
