# Multi-stage build for efficient image size
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /workspace/target/spring-ai-demo-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "export GOOGLE_API_KEY=$(echo -n \"$GOOGLE_API_KEY\" | tr -d '\\r\\n\\t '); export GEMINI_API_KEY=$(echo -n \"${GEMINI_API_KEY:-$GOOGLE_API_KEY}\" | tr -d '\\r\\n\\t '); export GOOGLE_PROJECT_ID=$(echo -n \"$GOOGLE_PROJECT_ID\" | tr -d '\\r\\n\\t '); exec java -Dserver.port=${PORT:-8081} -jar app.jar"]

