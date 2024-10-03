FROM gradle:8.3.0-jdk20-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src


RUN gradle build -x test --no-daemon

FROM openjdk:20-jdk-slim

RUN apt-get update && apt-get install curl -y

EXPOSE 8090

HEALTHCHECK --interval=5m --timeout=3s CMD curl -f http://localhost:8090/actuator/health/ || exit 1

RUN mkdir /app

COPY --from=build /home/gradle/src/app/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions","-jar","/app/spring-boot-application.jar"]