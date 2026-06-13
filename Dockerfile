# ============================================================
# Multi-stage build: Node (admin frontend) + Maven (backend)
# ============================================================

# --- Stage 1: Build Vue admin frontend ---
FROM node:14-alpine AS frontend-build
WORKDIR /app/admin
COPY gaoxiaojiaoshidianzimingpian/src/main/resources/admin/admin/package*.json ./
RUN npm install --registry=https://registry.npmmirror.com
COPY gaoxiaojiaoshidianzimingpian/src/main/resources/admin/admin/ ./
RUN npm run build

# --- Stage 2: Build Spring Boot JAR ---
FROM maven:3.6-jdk-8 AS backend-build
WORKDIR /app
COPY gaoxiaojiaoshidianzimingpian/pom.xml ./
# Download dependencies first (cacheable layer)
RUN mvn dependency:go-offline -B
COPY gaoxiaojiaoshidianzimingpian/src ./src
# Copy the freshly built admin dist into resources before Maven packages
COPY --from=frontend-build /app/admin/dist ./src/main/resources/admin/admin/dist
RUN mvn package -DskipTests -B

# --- Stage 3: Runtime ---
FROM openjdk:8-jre-slim
LABEL maintainer="gaoxiaojiaoshidianzimingpian"
WORKDIR /app

COPY --from=backend-build /app/target/gaoxiaojiaoshidianzimingpian-0.0.1-SNAPSHOT.jar app.jar

# Default upload directory — mount a volume here for persistence
ENV UPLOAD_BASE_PATH=/data/upload
RUN mkdir -p /data/upload/upload

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar", \
    "--spring.profiles.active=prod"]
