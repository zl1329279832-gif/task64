# ============================================================
# Multi-stage Dockerfile for 高校教师电子名片系统
# ============================================================

# ---- Stage 1: Build (Maven + JDK 8) ----
FROM maven:3.6-jdk-8 AS builder

WORKDIR /build
COPY gaoxiaojiaoshidianzimingpian/pom.xml ./pom.xml
# Download dependencies first (better layer caching)
RUN mvn dependency:go-offline -B

COPY gaoxiaojiaoshidianzimingpian/src ./src
RUN mvn clean package -DskipTests -B \
    && mv target/gaoxiaojiaoshidianzimingpian-*.jar target/app.jar

# ---- Stage 2: Runtime (JRE 8 slim) ----
FROM openjdk:8-jre-slim

LABEL maintainer="教务处"
LABEL description="高校教师电子名片系统"

# Non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser \
    && mkdir -p /data/uploads /app \
    && chown -R appuser:appuser /data/uploads /app

WORKDIR /app

COPY --from=builder /build/target/app.jar /app/app.jar

# Default to prod profile
ENV SPRING_PROFILES_ACTIVE=prod
ENV UPLOAD_BASE_PATH=/data/uploads

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/gaoxiaojiaoshidianzimingpian/actuator/health || \
        curl -sf http://localhost:8080/gaoxiaojiaoshidianzimingpian/ || exit 1

EXPOSE 8080

VOLUME ["/data/uploads"]

USER appuser

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]
