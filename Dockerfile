FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /build
COPY ./pom.xml .
COPY ./src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
LABEL org.opencontainers.image.source="https://github.com/tony-waters/spring-boot-app"
LABEL org.opencontainers.image.description="Spring Boot JPA aggregate + CQRS demo"
LABEL org.opencontainers.image.licenses="MIT"
COPY --from=build /build/target/spring-boot-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]