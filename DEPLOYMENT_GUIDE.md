# Eflo Microservices Platform - Deployment & Testing Guide

**Version**: 1.0.0
**Last Updated**: November 15, 2025
**Status**: Production Ready (after bug fixes applied)

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Local Development Deployment](#local-development-deployment)
4. [Docker Deployment](#docker-deployment)
5. [Production Deployment](#production-deployment)
6. [Testing Guide](#testing-guide)
7. [Troubleshooting](#troubleshooting)
8. [Monitoring & Health Checks](#monitoring--health-checks)

---

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java JDK | 21+ | Running Spring Boot services |
| Maven | 3.9+ | Building Java services |
| Node.js | 18+ LTS | Running Next.js frontend |
| npm | 9+ | Frontend package management |
| Docker | 24+ | Container runtime |
| Docker Compose | 2.20+ | Multi-container orchestration |
| PostgreSQL | 15+ | Database (if running locally) |
| Kafka | 3.5+ | Event streaming (if running locally) |

### Minimum Hardware Requirements

**Development Environment**:
- CPU: 4 cores
- RAM: 16 GB
- Disk: 50 GB free space

**Production Environment**:
- CPU: 8+ cores
- RAM: 32+ GB
- Disk: 200+ GB SSD
- Network: 1 Gbps

---

## Environment Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd eflo
```

### 2. Create Environment File

```bash
# Copy example environment file
cp .env.example .env

# Edit with your values
nano .env
```

**Minimum required variables**:
```env
# Database Passwords
KEYCLOAK_DB_PASSWORD=your_secure_password
ORDER_DB_PASSWORD=your_secure_password
WORKFLOW_DB_PASSWORD=your_secure_password
COMMISSION_DB_PASSWORD=your_secure_password
DOCUMENT_DB_PASSWORD=your_secure_password
USER_DB_PASSWORD=your_secure_password
N8N_DB_PASSWORD=your_secure_password

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your_secure_admin_password

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_secure_minio_password

# n8n (Optional)
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=your_secure_n8n_password
```

**⚠️ Security Note**: Use strong, unique passwords for each service in production!

### 3. Verify Java Version

```bash
java -version
# Should show: openjdk version "21.x.x" or higher

mvn -version
# Should show: Apache Maven 3.9.x
```

If you need to install Java 21:

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# macOS (using Homebrew)
brew install openjdk@21

# Verify installation
java -version
```

---

## Local Development Deployment

### Option 1: Run Services Individually

#### Step 1: Start Infrastructure Services

```bash
# Start PostgreSQL databases
docker-compose up -d postgres-keycloak postgres-order postgres-workflow \
  postgres-commission postgres-document postgres-user postgres-n8n

# Start Kafka & Zookeeper
docker-compose up -d zookeeper kafka

# Start MinIO
docker-compose up -d minio

# Wait for services to be healthy (30-60 seconds)
docker-compose ps
```

#### Step 2: Start Keycloak

```bash
docker-compose up -d keycloak

# Wait for Keycloak to initialize (can take 2-3 minutes)
# Check health
curl http://localhost:8180/health/ready
```

#### Step 3: Start Core Services

**Terminal 1 - Eureka Server**:
```bash
cd eureka-server
mvn spring-boot:run
```

**Terminal 2 - Config Server**:
```bash
cd config-server
mvn spring-boot:run -Dspring-boot.run.profiles=native
```

**Terminal 3 - API Gateways**:
```bash
cd internal-api-gateway
mvn spring-boot:run

# In another terminal
cd external-api-gateway
mvn spring-boot:run
```

#### Step 4: Start Business Services

```bash
# Order Service
cd order-service
mvn spring-boot:run

# Workflow Service
cd workflow-service
mvn spring-boot:run

# User Service
cd user-service
mvn spring-boot:run

# Commission Service
cd commission-service
mvn spring-boot:run

# Document Service
cd document-service
mvn spring-boot:run
```

#### Step 5: Start Frontend

```bash
cd workflow-admin
npm install
npm run dev
```

**Access the application**:
- Frontend: http://localhost:3001
- Eureka Dashboard: http://localhost:8761
- Keycloak Admin: http://localhost:8180/admin (admin/admin)
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

---

## Docker Deployment

### Full Stack Deployment

#### Step 1: Build All Services

```bash
# Build all service images (takes 10-15 minutes first time)
docker-compose build

# Or build individual services
docker-compose build order-service
docker-compose build workflow-service
```

#### Step 2: Start Infrastructure

```bash
# Start infrastructure services first
docker-compose up -d eureka-server zookeeper kafka postgres-keycloak \
  postgres-order postgres-workflow postgres-commission postgres-document \
  postgres-user minio keycloak

# Check health status
docker-compose ps

# Wait for all services to show (healthy)
# This can take 2-3 minutes
```

#### Step 3: Start Configuration & Gateways

```bash
# Start config server
docker-compose up -d config-server

# Wait 30 seconds for config server to be ready

# Start API gateways
docker-compose up -d internal-api-gateway external-api-gateway
```

#### Step 4: Start Business Services

```bash
# Start all business services
docker-compose up -d order-service workflow-service commission-service \
  document-service user-service

# Monitor startup logs
docker-compose logs -f order-service workflow-service
```

#### Step 5: Start Optional Services

```bash
# Start document generation service
docker-compose up -d document-generation-service

# Start n8n (if needed)
docker-compose up -d n8n
```

#### Step 6: Verify All Services

```bash
# Check all containers are running
docker-compose ps

# View Eureka dashboard
open http://localhost:8761

# All services should appear within 2 minutes
```

### Quick Start (One Command)

```bash
# Start everything at once (not recommended for first time)
docker-compose up -d

# Monitor logs
docker-compose logs -f
```

---

## Production Deployment

### Pre-Deployment Checklist

- [ ] All environment variables configured in `.env`
- [ ] Strong passwords set for all services
- [ ] SSL/TLS certificates obtained
- [ ] Database backups configured
- [ ] Monitoring & alerting set up
- [ ] Log aggregation configured
- [ ] Firewall rules configured
- [ ] Resource limits set in docker-compose
- [ ] Health checks verified
- [ ] Disaster recovery plan documented

### Production Environment Variables

Create a `.env.production` file:

```env
# Use production URLs
KEYCLOAK_AUTH_SERVER_URL=https://auth.yourdomain.com
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://discovery.yourdomain.com/eureka/

# Use strong passwords (32+ characters)
KEYCLOAK_DB_PASSWORD=<use password manager>
ORDER_DB_PASSWORD=<use password manager>
# ... etc

# Enable production settings
SPRING_PROFILES_ACTIVE=production
NODE_ENV=production

# Disable dev features
N8N_AI_ENABLED=false (unless API key configured)
```

### Production Deployment Steps

#### 1. Infrastructure Setup

```bash
# Use production docker-compose file
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

#### 2. Database Initialization

```bash
# Verify all databases are created
docker-compose exec postgres-order psql -U order_user -d order_db -c "\dt"

# Flyway migrations run automatically on service startup
# Check migration status in logs:
docker-compose logs order-service | grep Flyway
```

#### 3. SSL/TLS Configuration

Update `docker-compose.prod.yml` to mount certificates:

```yaml
services:
  internal-api-gateway:
    volumes:
      - ./ssl/cert.pem:/app/cert.pem:ro
      - ./ssl/key.pem:/app/key.pem:ro
    environment:
      - SERVER_SSL_ENABLED=true
      - SERVER_SSL_CERTIFICATE=/app/cert.pem
      - SERVER_SSL_KEY=/app/key.pem
```

#### 4. Reverse Proxy (Nginx)

```nginx
upstream eflo_frontend {
    server localhost:3001;
}

upstream eflo_api {
    server localhost:8080;
}

server {
    listen 443 ssl http2;
    server_name app.yourdomain.com;

    ssl_certificate /etc/ssl/certs/yourdomain.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.key;

    location / {
        proxy_pass http://eflo_frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/ {
        proxy_pass http://eflo_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## Testing Guide

### Unit Tests

```bash
# Run all unit tests
cd order-service
mvn test

# Run specific test class
mvn test -Dtest=OrderServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
mvn verify

# Run with Testcontainers (requires Docker)
mvn verify -Pintegration-tests
```

### End-to-End Testing

#### 1. Service Health Checks

```bash
# Check all services are healthy
./scripts/health-check.sh

# Or manually:
curl http://localhost:8089/actuator/health  # Order Service
curl http://localhost:8091/actuator/health  # Workflow Service
curl http://localhost:8084/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Commission Service
curl http://localhost:8083/actuator/health  # Document Service
```

#### 2. API Testing with Postman

```bash
# Import Postman collection
# File: Eflo-Microservices-Complete.postman_collection.json

# Set environment variables in Postman:
# - gateway_url: http://localhost:8080
# - keycloak_url: http://localhost:8180
# - realm: eflo
```

**Test Sequence**:
1. Get Keycloak token
2. Create order
3. Update order status
4. Create workflow instance
5. Assign task
6. Complete task

#### 3. Frontend Testing

```bash
cd workflow-admin

# Type checking
npm run type-check

# Linting
npm run lint

# Build production bundle
npm run build

# Start production server
npm start
```

### Load Testing

```bash
# Install k6 (if not already installed)
brew install k6  # macOS
# or
sudo apt install k6  # Ubuntu

# Run load test
k6 run scripts/load-test.js
```

---

## Troubleshooting

### Common Issues

#### 1. Service Won't Start

**Symptoms**: Container exits immediately or shows errors in logs

**Solution**:
```bash
# Check logs
docker-compose logs <service-name>

# Common causes:
# - Database not ready: Wait longer or check database health
# - Port already in use: Check with `lsof -i :<port>` and kill process
# - Missing environment variable: Verify .env file
# - Eureka not reachable: Ensure eureka-server is running first
```

#### 2. Database Connection Errors

**Symptoms**: `org.postgresql.util.PSQLException: Connection refused`

**Solution**:
```bash
# Verify database is running
docker-compose ps postgres-order

# Check database logs
docker-compose logs postgres-order

# Test connection manually
docker-compose exec postgres-order psql -U order_user -d order_db

# Verify password in .env matches docker-compose.yml
```

#### 3. Kafka Connection Issues

**Symptoms**: `TimeoutException: Topic metadata not available`

**Solution**:
```bash
# Wait for Kafka to be fully ready (can take 60s)
docker-compose logs kafka

# Verify Kafka is healthy
docker-compose exec kafka kafka-broker-api-versions \
  --bootstrap-server localhost:9092

# Check Zookeeper
docker-compose logs zookeeper
```

#### 4. Keycloak Authentication Failures

**Symptoms**: `401 Unauthorized` on API calls

**Solution**:
```bash
# Verify Keycloak is running
curl http://localhost:8180/health/ready

# Check realm exists
curl http://localhost:8180/realms/eflo

# Get new token via Postman or:
curl -X POST http://localhost:8180/realms/eflo/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=eflo-client&username=admin&password=admin"
```

#### 5. Service Not Registering with Eureka

**Symptoms**: Service not visible in Eureka dashboard

**Solution**:
```bash
# Check Eureka URL in service logs
docker-compose logs order-service | grep eureka

# Verify Eureka is accessible from service container
docker-compose exec order-service curl http://eureka-server:8761/eureka/apps

# Check Eureka credentials match
# Services should use: eureka/eurekapass (default)
```

### Performance Issues

#### Slow Startup

```bash
# Increase JVM memory
docker-compose up -d order-service \
  -e JAVA_OPTS="-Xms512m -Xmx1024m"
```

#### High Memory Usage

```bash
# Check memory usage
docker stats

# Set resource limits in docker-compose.yml
services:
  order-service:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

---

## Monitoring & Health Checks

### Health Check Endpoints

All services expose actuator health endpoints:

```bash
# Individual service health
curl http://localhost:8089/actuator/health

# Detailed health with components
curl http://localhost:8089/actuator/health/details
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8089/actuator/prometheus

# Application info
curl http://localhost:8089/actuator/info

# Environment variables
curl http://localhost:8089/actuator/env
```

### Log Aggregation

**Development**:
```bash
# View all logs
docker-compose logs -f

# View specific service
docker-compose logs -f order-service workflow-service

# Search logs
docker-compose logs | grep ERROR
```

**Production**: Consider using:
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Splunk
- Datadog
- CloudWatch (AWS)

### Service Discovery

**Eureka Dashboard**: http://localhost:8761

Expected services:
- ORDER-SERVICE
- WORKFLOW-SERVICE
- USER-SERVICE
- COMMISSION-SERVICE
- DOCUMENT-SERVICE
- DOCUMENT-GENERATION-SERVICE
- INTERNAL-API-GATEWAY
- EXTERNAL-API-GATEWAY
- CONFIG-SERVER

---

## Backup & Recovery

### Database Backups

```bash
# Backup order database
docker-compose exec postgres-order pg_dump \
  -U order_user order_db > backup_order_$(date +%Y%m%d).sql

# Backup all databases
./scripts/backup-databases.sh
```

### Restore from Backup

```bash
# Restore order database
cat backup_order_20251115.sql | \
  docker-compose exec -T postgres-order psql -U order_user order_db
```

---

## Scaling

### Horizontal Scaling

```bash
# Scale order service to 3 instances
docker-compose up -d --scale order-service=3

# Verify all instances registered with Eureka
curl http://localhost:8761/eureka/apps/ORDER-SERVICE
```

### Vertical Scaling

Update `docker-compose.yml`:

```yaml
services:
  order-service:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
```

---

## Security Checklist

- [ ] All default passwords changed
- [ ] SSL/TLS enabled for all external endpoints
- [ ] Firewall configured (only expose ports 80, 443)
- [ ] Database encryption at rest enabled
- [ ] Kafka encryption in transit enabled
- [ ] Regular security updates applied
- [ ] Keycloak admin console not publicly accessible
- [ ] MinIO console not publicly accessible
- [ ] Actuator endpoints secured or disabled in production
- [ ] Rate limiting configured on API gateways
- [ ] CORS properly configured
- [ ] JWT token expiration set appropriately

---

## Support & Maintenance

### Regular Maintenance Tasks

**Daily**:
- Check service health via Eureka
- Review error logs
- Monitor disk space

**Weekly**:
- Database backups verification
- Security updates check
- Performance metrics review

**Monthly**:
- Full system backup
- Dependency updates
- Capacity planning review

### Getting Help

1. Check service logs: `docker-compose logs <service-name>`
2. Review Eureka dashboard: http://localhost:8761
3. Check BUG_FIXES.md for known issues
4. Review service-specific README files

---

## Quick Reference

### Service Ports

| Service | Port | URL |
|---------|------|-----|
| Workflow Admin | 3001 | http://localhost:3001 |
| Internal Gateway | 8080 | http://localhost:8080 |
| External Gateway | 8079 | http://localhost:8079 |
| Eureka | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| Keycloak | 8180 | http://localhost:8180 |
| Order Service | 8089 | http://localhost:8089 |
| Workflow Service | 8091 | http://localhost:8091 |
| User Service | 8084 | http://localhost:8084 |
| Commission Service | 8082 | http://localhost:8082 |
| Document Service | 8083 | http://localhost:8083 |
| Doc Generation | 8085 | http://localhost:8085 |
| MinIO API | 9000 | http://localhost:9000 |
| MinIO Console | 9001 | http://localhost:9001 |
| n8n | 5678 | http://localhost:5678 |

### Useful Commands

```bash
# View all running services
docker-compose ps

# Restart specific service
docker-compose restart order-service

# View logs for last hour
docker-compose logs --since 1h -f

# Clean up everything
docker-compose down -v

# Rebuild and restart service
docker-compose up -d --build order-service
```

---

**Document Version**: 1.0
**Last Reviewed**: November 15, 2025
**Next Review**: December 15, 2025
