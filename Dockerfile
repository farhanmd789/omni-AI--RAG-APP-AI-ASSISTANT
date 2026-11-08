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

ENTRYPOINT ["sh", "-c", "export GOOGLE_API_KEY=$(echo -n \"$GOOGLE_API_KEY\" | tr -d '\\r\\n\\t '); export GEMINI_API_KEY=$(echo -n \"${GEMINI_API_KEY:-$GOOGLE_API_KEY}\" | tr -d '\\r\\n\\t '); export GOOGLE_PROJECT_ID=$(echo -n \"$GOOGLE_PROJECT_ID\" | tr -d '\\r\\n\\t '); exec java -Dserver.port=${PORT:-8081} -jar app.jar"]
