# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper & project files
COPY Java_Spring_AI_Project-main/pom.xml Java_Spring_AI_Project-main/mvnw Java_Spring_AI_Project-main/mvnw.cmd ./
COPY Java_Spring_AI_Project-main/.mvn .mvn
COPY Java_Spring_AI_Project-main/src src

# Make mvnw executable and package the app
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/spring-ai-demo-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -jar app.jar"]
