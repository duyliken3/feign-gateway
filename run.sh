#!/bin/bash

echo "Starting Feign Gateway Service..."
echo "Building the project..."

mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful. Starting the application..."
    mvn spring-boot:run
else
    echo "Build failed. Please check the errors above."
    exit 1
fi
