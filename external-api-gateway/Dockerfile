# ======================================================
# External API Gateway - Multi-stage Docker Build
# ======================================================

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the jar from build stage
COPY --from=build /app/target/*.jar external-gateway.jar

# Change ownership to non-root user
RUN chown spring:spring external-gateway.jar

# Switch to non-root user
USER spring:spring

# Expose External Gateway port
EXPOSE 8081

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Environment variables for JVM tuning
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar external-gateway.jar"]
