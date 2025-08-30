# Stage 1: Build Stage using a slim Gradle JDK
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

# Build the project while excluding tests to speed up the build process
RUN gradle build -x test --no-daemon

# Stage 2: Use a lightweight JRE image for the runtime
FROM eclipse-temurin:21-jre-alpine

# Expose the application port
EXPOSE 8090

# Create an application directory
RUN mkdir /app

# Copy the jar file from the build stage
COPY --from=build /home/gradle/src/app/build/libs/*.jar /app/spring-boot-application.jar

# Define the entry point for the application
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Duser.timezone=UTC", "-jar", "/app/spring-boot-application.jar"]
