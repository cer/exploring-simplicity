FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache curl

WORKDIR /app

COPY build/libs/trip-booking-service-temporal-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]