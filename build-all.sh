#!/bin/bash

echo "Building all services..."

# Сборка всех модулей
./gradlew clean build -x test

echo "All services built successfully!"

docker-compose up -d