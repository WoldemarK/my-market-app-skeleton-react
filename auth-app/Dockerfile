FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /auth-app
COPY ../pom.xml .
RUN mvn dependency:go-offline
COPY ../src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /auth-app
COPY --from=build /auth-app/target/*.jar auth-app.jar
RUN ls -la /auth-app/
RUN mkdir -p /uploads

EXPOSE 8080

CMD ["sh", "-c", "java -jar auth-app.jar"]