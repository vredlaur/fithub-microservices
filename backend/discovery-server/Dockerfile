FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY mvnw ./
COPY .mvn .mvn
COPY backend/discovery-server backend/discovery-server
RUN chmod +x mvnw && ./mvnw -q -f backend/discovery-server/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
