# =====================================================
# Eureka Server - Multi-stage Docker Build
# =====================================================
# Stage 1: Build the application
# Stage 2: Runtime image
# =====================================================

# =====================================================
# Stage 1: Build Stage
# =====================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# =====================================================
# Stage 2: Runtime Stage
# =====================================================
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the jar from build stage
COPY --from=build /app/target/*.jar eureka-server.jar

# Change ownership to non-root user
RUN chown spring:spring eureka-server.jar

# Switch to non-root user
USER spring:spring

# Expose Eureka Server port
EXPOSE 8761

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Environment variables for JVM tuning
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar eureka-server.jar"]
