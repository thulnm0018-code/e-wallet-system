# ==========================================
# Stage 1: Build ứng dụng Spring Boot
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy file cấu hình Maven trước để tận dụng cache
COPY pom.xml .

# Tải dependency trước
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests=true

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy file JAR từ stage build
COPY --from=build /app/target/*.jar app.jar

# Port Spring Boot
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]