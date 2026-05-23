FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY mvnw ./
COPY .mvn .mvn
COPY backend/auth-service backend/auth-service
RUN chmod +x mvnw && ./mvnw -q -f backend/auth-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
