# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Eflo is a microservices-based platform for managing vehicle orders, workflows, commissions, documents, and user management. The system uses Spring Boot microservices with a Next.js admin dashboard, integrated with Keycloak for authentication and n8n for workflow automation.

## Architecture

### Microservices Stack

The platform consists of:

**Infrastructure Services:**
- **eureka-server** (8761) - Service discovery
- **config-server** (8888) - Centralized configuration with native profile
- **keycloak** (8180) - Identity and access management
- **kafka + zookeeper** (9092, 2181) - Event streaming
- **n8n** (5678) - Workflow automation with AI document analysis and WhatsApp integration

**API Gateways:**
- **internal-api-gateway** (8080) - Internal service communication
- **external-api-gateway** (8081) - External client access

**Business Services:**
- **order-service** (8089) - Order management for VN/VO/EVO vehicle orders with pricing engine
- **workflow-service** (8091) - Workflow orchestration with state machines, tasks, and approvals
- **commission-service** (8082) - Commission calculations
- **document-service** (8083) - Document management with MinIO storage
- **user-service** (8084) - User, business unit, hierarchy, and role management with Keycloak sync

**Frontend:**
- **workflow-admin** (3001) - Next.js 14 admin dashboard with React Flow visualization

**Storage:**
- PostgreSQL databases for each service (ports 5433-5439)
- MinIO (9000, 9001) - S3-compatible object storage
- Kafka for event streaming

### Key Architectural Patterns

1. **Event-Driven Architecture**: Services publish domain events to Kafka (e.g., `orders.created`, `orders.status-changed`)
2. **Service Discovery**: All services register with Eureka for dynamic service location
3. **API Gateway Pattern**: Internal/external gateways route and secure requests
4. **Centralized Configuration**: Config server provides environment-specific configs
5. **OAuth2/JWT Security**: Keycloak provides authentication with JWT tokens validated by resource servers
6. **Workflow Orchestration**: Workflow service manages state machines with automatic escalation and deadline monitoring
7. **External Automation**: n8n integration enables AI document analysis, WhatsApp notifications, and task validation

### Workflow Service Architecture

The workflow service is the orchestration engine:
- **WorkflowProcess**: Template definitions for business processes (versioned, activatable)
- **WorkflowState**: States within processes (START, NORMAL, END types)
- **WorkflowTransition**: Allowed state progressions with conditions
- **WorkflowInstance**: Runtime execution tracking for specific orders
- **WorkflowInstanceTask**: Individual task instances with escalation support
- **Approval Chains**: Multi-level approval workflows with delegation

Key features:
- Automatic escalation after configurable thresholds (24h, 48h, 72h, 168h)
- Deadline monitoring with hourly checks
- n8n webhook integration for external automation
- JSONB configuration for flexible runtime behavior
- Bilingual support (French/English)

### Order Service Architecture

Handles complete order lifecycle:
- **Order Types**: VN (New), VO (Used), EVO (Evolution)
- **Pricing Engine**: Automatic calculation of totals, VAT, discounts, margins
- **Status Flow**: DRAFT → PENDING → CONFIRMED → IN_PRODUCTION → READY_FOR_DELIVERY → DELIVERED → INVOICED
- **Deliveries**: Scheduling and tracking with status management
- **Configurable Mapping System**: JSON-based field mappings for dynamic order types (see `order-service/src/main/resources/mappings/`)

### User Service Architecture

Manages organizational structure:
- **Users**: Profile management with employee numbers, Keycloak sync
- **Business Units**: Hierarchical organization (Department, Branch, Region, Team)
- **Hierarchies**: Manager-employee relationships with level tracking
- **Roles**: RBAC with Keycloak synchronization (scheduled daily at 2 AM)
- **Activity Logs**: Comprehensive audit trail

## Development Commands

### Java Services (Maven)

All Spring Boot services use Maven with Java 21:

```bash
# Build service
mvn clean package

# Run tests
mvn test

# Run integration tests
mvn verify

# Run service locally
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Skip tests during build
mvn clean package -DskipTests

# Run Flyway migrations
mvn flyway:migrate
```

### Next.js Workflow Admin

```bash
cd workflow-admin

# Install dependencies
npm install

# Run development server (port 3001)
npm run dev

# Build for production
npm run build

# Start production server
npm start

# Type checking
npm run type-check

# Lint
npm run lint
```

### Docker Operations

```bash
# Start all services
docker-compose up -d

# Start specific service with dependencies
docker-compose up -d order-service

# View logs
docker-compose logs -f [service-name]

# Rebuild service
docker-compose up -d --build [service-name]

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Build service image
docker build -t eflo/[service-name]:1.0.0 ./[service-name]
```

### Testing Services

```bash
# Check service health
curl http://localhost:[port]/actuator/health

# View Eureka registry
curl http://localhost:8761/eureka/apps

# Test Kafka connectivity
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

## Environment Configuration

All services require a `.env` file in the root directory:

```env
# Database passwords
KEYCLOAK_DB_PASSWORD=keycloakpass
ORDER_DB_PASSWORD=orderpass
WORKFLOW_DB_PASSWORD=workflowpass
COMMISSION_DB_PASSWORD=commissionpass
DOCUMENT_DB_PASSWORD=documentpass
USER_DB_PASSWORD=userpass
N8N_DB_PASSWORD=n8npass

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=n8nadmin
N8N_AI_API_KEY=your-openai-key
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_WHATSAPP_NUMBER=whatsapp:+1234567890
```

## Service Startup Order

For correct initialization:

1. Infrastructure: `eureka-server`, `zookeeper`, `kafka`, `postgres-*`, `keycloak`, `minio`
2. Config: `config-server`
3. Gateways: `internal-api-gateway`, `external-api-gateway`
4. Business Services: `order-service`, `workflow-service`, `commission-service`, `document-service`, `user-service`
5. Automation: `n8n`
6. Frontend: `workflow-admin`

The `docker-compose.yml` handles this with `depends_on` and `healthcheck` configurations.

## Common Development Patterns

### Adding a New Microservice

1. Create service directory with Spring Boot structure
2. Add `pom.xml` with Spring Cloud, Eureka, Kafka, OAuth2 dependencies
3. Configure `application.yml` with port, database, Eureka, security settings
4. Add Dockerfile (multi-stage build with Maven)
5. Add service to `docker-compose.yml` with database, health checks
6. Register with Eureka using `spring.application.name`
7. Create Flyway migrations in `src/main/resources/db/migration/`
8. Add Kafka event publishing/listening as needed

### Project Structure (Spring Boot Services)

```
[service-name]/
├── src/main/java/com/eflo/[service]/
│   ├── config/          # Security, Kafka, OpenFeign configs
│   ├── domain/
│   │   ├── entity/      # JPA entities
│   │   ├── dto/         # Request/response DTOs
│   │   ├── enums/       # Status enums
│   │   └── repository/  # Spring Data repositories
│   ├── service/         # Business logic
│   ├── web/             # REST controllers
│   ├── mapper/          # MapStruct entity-DTO mappers
│   ├── exception/       # Custom exceptions
│   └── [Service]Application.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   └── db/migration/    # Flyway migrations (V1__*.sql)
├── Dockerfile
└── pom.xml
```

### Security and Authentication

All services use OAuth2 JWT tokens from Keycloak:
- Issuer URI: `http://localhost:8180/realms/eflo` (or `http://keycloak:8180/realms/eflo` in Docker)
- Token validation happens at API gateways and individual services
- Roles include: `SUPER_ADMIN`, `ADMIN_LOCAL`, `SALES_MANAGER`, `SALESPERSON`, `VIEWER`
- Use `@PreAuthorize("hasRole('ROLE_NAME')")` on endpoints

### Database Migrations

All services use Flyway:
- Migrations in `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__initial_schema.sql`)
- Run automatically on startup
- Never modify existing migrations; create new ones

### Event Publishing (Kafka)

Services publish domain events for cross-service communication:
- Order Service: `orders.created`, `orders.updated`, `orders.status-changed`, `orders.delivered`
- Workflow Service listens to order events to trigger workflows
- Document Service listens for document-related events
- Use consistent event payload structures with timestamps and correlation IDs

## Important Notes

### Order Service Condition System

The order service includes a sophisticated condition evaluation system for business rules:
- Conditions stored in database with rules and category assignments
- Dynamic field evaluation against order entities
- See `order-service/CONDITION-SYSTEM-IMPLEMENTATION.md` for details

### Configurable Order Mapping

Order service supports dynamic field mappings via JSON configuration files in `order-service/src/main/resources/mappings/`:
- `move-order-mapping.json` - For move/transportation orders
- `generic-order-mapping.json` - Standard vehicle orders
- Enables different order types without code changes

### User Service Keycloak Sync

User service automatically syncs with Keycloak:
- Scheduled sync: Daily at 2:00 AM (configurable via cron)
- Manual sync: `POST /api/v1/keycloak/sync/users`
- Bidirectional: Changes in either system can propagate
- Sync includes users and roles

### Workflow Admin Frontend

Built with Next.js 14 App Router:
- Uses React Flow for visual workflow design
- TanStack Query for server state management
- Zustand for client state
- PostgreSQL direct connection for certain operations (via pg library)
- Tailwind CSS 4.0 for styling
- TypeScript for type safety

### n8n Integration

n8n workflows are pre-configured for:
- AI document analysis (using OpenAI GPT-4)
- WhatsApp notifications (via Twilio)
- Task validation
- File reception and processing
- Located in `n8n/workflows/`

## API Documentation

Each service exposes Swagger UI:
- Order Service: http://localhost:8089/swagger-ui.html
- Workflow Service: http://localhost:8091/swagger-ui.html
- User Service: http://localhost:8084/swagger-ui.html
- Commission Service: http://localhost:8082/swagger-ui.html
- Document Service: http://localhost:8083/swagger-ui.html

## Troubleshooting

### Service won't start
- Check dependencies are running: `docker ps`
- Verify Eureka registration: `curl http://localhost:8761/eureka/apps`
- Check service logs: `docker-compose logs -f [service-name]`
- Ensure environment variables are set

### Database connection issues
- Verify PostgreSQL is running on correct port
- Check credentials in `.env` match `application.yml`
- Ensure database exists (created by docker-compose)

### Kafka connection issues
- Wait for Kafka to be fully ready (can take 60s)
- Check Zookeeper is healthy
- Verify `KAFKA_ADVERTISED_LISTENERS` configuration

### Keycloak authentication failures
- Ensure Keycloak is running and realm 'eflo' exists
- Verify issuer URI matches Keycloak URL
- Check JWT token is valid and not expired
- Confirm user has required roles

### Port conflicts
- Default ports: 8761, 8888, 8180, 8080-8084, 8089, 8091, 9000-9001, 5678, 3001, 5433-5439, 9092, 2181
- Change ports in both `docker-compose.yml` and service `application.yml`
