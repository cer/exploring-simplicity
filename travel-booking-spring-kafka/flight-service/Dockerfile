FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache curl
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]