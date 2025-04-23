FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN ./mvnw clean install -DskipTests
ENTRYPOINT ["./mvnw", "spring-boot:run"]