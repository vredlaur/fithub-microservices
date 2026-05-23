FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY mvnw ./
COPY .mvn .mvn
COPY backend/api-gateway backend/api-gateway
RUN chmod +x mvnw && ./mvnw -q -f backend/api-gateway/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
