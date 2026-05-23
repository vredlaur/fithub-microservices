FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/booking-service backend/booking-service
RUN mvn -q -f backend/booking-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/booking-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
