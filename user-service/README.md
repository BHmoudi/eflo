# User Service

The User Service is a core microservice in the Eflo platform that manages users, business units, organizational hierarchies, and role-based access control. It integrates with Keycloak for authentication and authorization, providing comprehensive user and organization management capabilities.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Keycloak Integration](#keycloak-integration)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)
- [Development Guide](#development-guide)

## Overview

The User Service provides centralized management of:

- **User Management**: Create, update, and manage user accounts
- **Business Units**: Organize users into departments, branches, and teams
- **Organizational Hierarchy**: Define reporting structures and relationships
- **Role-Based Access Control**: Manage user roles and permissions
- **Keycloak Synchronization**: Bidirectional sync with Keycloak identity provider
- **Activity Logging**: Track user activities and audit trails

## Features

### User Management

- Create and manage user accounts with comprehensive profile information
- Email uniqueness validation
- Employee number management
- User activation/deactivation
- Soft delete functionality
- Password reset capabilities
- Login tracking and analytics
- User search with multiple criteria

### Business Unit Management

- Create hierarchical business unit structures
- Support for different business unit types (Department, Branch, Region, Team)
- Region-based organization
- Business unit activation/deactivation
- User assignments to business units
- Primary and secondary business unit assignments

### Organizational Hierarchy

- Define manager-employee relationships
- Multi-level hierarchy support
- Hierarchy level tracking
- Business unit-based hierarchies
- Dynamic hierarchy queries
- Subordinate counting and reporting

### Role Management

- User role assignment
- Role source tracking (Manual, Keycloak)
- Business unit-scoped roles
- Role activation/deactivation
- Multiple roles per user
- Role-based queries

### Keycloak Integration

- Automated user synchronization from Keycloak
- Role synchronization
- Bidirectional user updates
- Scheduled sync jobs
- Manual sync triggers
- User creation in Keycloak
- Password management through Keycloak

### Activity Logging

- Comprehensive audit trail
- Login tracking
- User activity logging
- IP address and user agent tracking
- Activity type categorization
- Performance analytics

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway                             │
│                   (Port 8080/8081)                           │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      │ HTTP/REST
                      ↓
┌─────────────────────────────────────────────────────────────┐
│                   User Service                               │
│                    (Port 8084)                               │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Controllers  │→ │   Services   │→ │ Repositories │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         ↓                  ↓                  ↓             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Security    │  │    Mappers   │  │   Entities   │     │
│  │  (JWT/OAuth) │  │  (MapStruct) │  │    (JPA)     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────┬───────────────────┬──────────────────┘
                      │                   │
           ┌──────────┴──────────┐       │
           ↓                     ↓       ↓
    ┌─────────────┐      ┌──────────────────┐
    │  Keycloak   │      │   PostgreSQL     │
    │   (8180)    │      │     (5438)       │
    │             │      │                  │
    │ - Auth      │      │ - Users          │
    │ - Roles     │      │ - Business Units │
    │ - Users     │      │ - Hierarchies    │
    └─────────────┘      │ - Roles          │
                         │ - Activity Logs  │
                         └──────────────────┘
```

### Components

1. **Controllers**: REST API endpoints for external communication
2. **Services**: Business logic implementation
3. **Repositories**: Data access layer using Spring Data JPA
4. **Entities**: JPA entities representing database tables
5. **DTOs**: Data Transfer Objects for API requests/responses
6. **Mappers**: MapStruct mappers for entity-DTO conversion
7. **Security**: JWT-based authentication and authorization
8. **Keycloak Integration**: Sync service and user management

## Technology Stack

- **Java 21**: Programming language
- **Spring Boot 3.2.1**: Application framework
- **Spring Data JPA**: Data persistence
- **Spring Security**: Authentication and authorization
- **OAuth2 Resource Server**: JWT token validation
- **PostgreSQL**: Primary database
- **Flyway**: Database migration
- **Keycloak Admin Client**: Keycloak integration
- **MapStruct**: Object mapping
- **Lombok**: Boilerplate code reduction
- **SpringDoc OpenAPI**: API documentation
- **Testcontainers**: Integration testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- PostgreSQL 15+ (or use Docker)
- Keycloak 23.0.0 (or use Docker)

### Local Development

1. **Clone the repository**

```bash
cd user-service
```

2. **Set up environment variables**

Create a `.env` file in the root directory:

```env
# Database
USER_DB_PASSWORD=userpass

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
```

3. **Start dependencies (PostgreSQL and Keycloak)**

```bash
# Using Docker Compose
docker-compose up -d postgres-user keycloak
```

4. **Run the application**

```bash
./mvnw spring-boot:run
```

The service will be available at `http://localhost:8084`

### Access Points

- **Service**: http://localhost:8084
- **Health Check**: http://localhost:8084/actuator/health
- **API Documentation**: http://localhost:8084/swagger-ui.html
- **Keycloak Admin**: http://localhost:8180

## Configuration

### Application Configuration

The service uses Spring profiles for different environments:

- `application.yml`: Default configuration
- `application-docker.yml`: Docker environment
- `application-test.yml`: Testing configuration

### Key Configuration Properties

```yaml
# Server Configuration
server:
  port: 8084

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5438/user_db
    username: user_user
    password: ${USER_DB_PASSWORD}

# Keycloak Configuration
keycloak:
  auth-server-url: http://localhost:8180
  realm: eflo
  admin:
    username: ${KEYCLOAK_ADMIN_USERNAME}
    password: ${KEYCLOAK_ADMIN_PASSWORD}
    client-id: admin-cli
  sync:
    enabled: true
    schedule: "0 0 2 * * ?" # Daily at 2 AM

# Security
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/eflo

# Eureka
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `USER_DB_PASSWORD` | PostgreSQL password | userpass |
| `KEYCLOAK_ADMIN_USERNAME` | Keycloak admin username | admin |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak admin password | admin |

## API Endpoints

### User Endpoints

| Method | Endpoint | Description | Roles Required |
|--------|----------|-------------|----------------|
| POST | `/api/v1/users` | Create new user | SUPER_ADMIN, ADMIN_LOCAL |
| GET | `/api/v1/users/{id}` | Get user by ID | Authenticated |
| GET | `/api/v1/users/email/{email}` | Get user by email | Authenticated |
| GET | `/api/v1/users/employee/{employeeNumber}` | Get user by employee number | Authenticated |
| GET | `/api/v1/users` | Get all users (paginated) | Authenticated |
| GET | `/api/v1/users/active` | Get active users | Authenticated |
| GET | `/api/v1/users/search?q={term}` | Search users | Authenticated |
| GET | `/api/v1/users/department/{department}` | Get users by department | Authenticated |
| PUT | `/api/v1/users/{id}` | Update user | SUPER_ADMIN, ADMIN_LOCAL, Self |
| POST | `/api/v1/users/{id}/deactivate` | Deactivate user | SUPER_ADMIN, ADMIN_LOCAL |
| POST | `/api/v1/users/{id}/reactivate` | Reactivate user | SUPER_ADMIN, ADMIN_LOCAL |
| DELETE | `/api/v1/users/{id}` | Delete user (soft) | SUPER_ADMIN |
| POST | `/api/v1/users/{id}/reset-password` | Reset password | SUPER_ADMIN, ADMIN_LOCAL |
| POST | `/api/v1/users/{id}/login` | Record login | Authenticated |
| GET | `/api/v1/users/count` | Get user count | SUPER_ADMIN, ADMIN_LOCAL |
| GET | `/api/v1/users/count/active` | Get active user count | SUPER_ADMIN, ADMIN_LOCAL |

### Business Unit Endpoints

| Method | Endpoint | Description | Roles Required |
|--------|----------|-------------|----------------|
| POST | `/api/v1/business-units` | Create business unit | SUPER_ADMIN, ADMIN_LOCAL |
| GET | `/api/v1/business-units/{id}` | Get business unit by ID | Authenticated |
| GET | `/api/v1/business-units/code/{code}` | Get business unit by code | Authenticated |
| GET | `/api/v1/business-units` | Get all business units | Authenticated |
| GET | `/api/v1/business-units/active` | Get active business units | Authenticated |
| GET | `/api/v1/business-units/type/{type}` | Get by type | Authenticated |
| GET | `/api/v1/business-units/region/{code}` | Get by region | Authenticated |
| GET | `/api/v1/business-units/search?q={term}` | Search business units | Authenticated |
| PUT | `/api/v1/business-units/{id}` | Update business unit | SUPER_ADMIN, ADMIN_LOCAL |
| POST | `/api/v1/business-units/{id}/deactivate` | Deactivate | SUPER_ADMIN, ADMIN_LOCAL |
| POST | `/api/v1/business-units/{id}/reactivate` | Reactivate | SUPER_ADMIN, ADMIN_LOCAL |
| DELETE | `/api/v1/business-units/{id}` | Delete | SUPER_ADMIN |

### Hierarchy Endpoints

| Method | Endpoint | Description | Roles Required |
|--------|----------|-------------|----------------|
| POST | `/api/v1/hierarchies` | Create hierarchy | SUPER_ADMIN, ADMIN_LOCAL |
| GET | `/api/v1/hierarchies/{id}` | Get hierarchy by ID | Authenticated |
| GET | `/api/v1/hierarchies/employee/{id}` | Get by employee | Authenticated |
| GET | `/api/v1/hierarchies/manager/{id}` | Get subordinates | Authenticated |
| GET | `/api/v1/hierarchies/business-unit/{id}` | Get by business unit | Authenticated |
| PUT | `/api/v1/hierarchies/{id}/level` | Update level | SUPER_ADMIN, ADMIN_LOCAL |
| PUT | `/api/v1/hierarchies/{id}/manager` | Change manager | SUPER_ADMIN, ADMIN_LOCAL |
| DELETE | `/api/v1/hierarchies/{id}` | Deactivate hierarchy | SUPER_ADMIN, ADMIN_LOCAL |

### Keycloak Sync Endpoints

| Method | Endpoint | Description | Roles Required |
|--------|----------|-------------|----------------|
| POST | `/api/v1/keycloak/sync/users` | Sync all users | SUPER_ADMIN |
| POST | `/api/v1/keycloak/sync/roles` | Sync all roles | SUPER_ADMIN |
| POST | `/api/v1/keycloak/sync/user/{id}` | Sync specific user | SUPER_ADMIN |
| GET | `/api/v1/keycloak/sync/status` | Get sync status | SUPER_ADMIN |

## Database Schema

### Main Tables

1. **users**: User accounts and profiles
2. **business_units**: Organizational units
3. **user_business_units**: User assignments to business units
4. **hierarchies**: Manager-employee relationships
5. **user_roles**: User role assignments
6. **user_activity_logs**: Audit trail

### Key Relationships

- Users can be assigned to multiple business units
- Users can have multiple roles
- Hierarchies link employees to managers within business units
- Activity logs track all user actions

## Keycloak Integration

### Features

1. **User Synchronization**
   - Automated daily sync from Keycloak
   - Manual sync on-demand
   - Bidirectional updates

2. **Role Management**
   - Sync roles from Keycloak
   - Track role source (Keycloak vs Manual)
   - Role activation/deactivation

3. **User Creation**
   - Create users in both systems simultaneously
   - Set temporary passwords
   - Enable/disable users

### Sync Schedule

- **Automatic**: Daily at 2:00 AM (configurable)
- **Manual**: POST `/api/v1/keycloak/sync/users`
- **Single User**: POST `/api/v1/keycloak/sync/user/{id}`

### Configuration

```yaml
keycloak:
  auth-server-url: http://localhost:8180
  realm: eflo
  admin:
    username: admin
    password: admin
    client-id: admin-cli
  sync:
    enabled: true
    schedule: "0 0 2 * * ?"
```

## Testing

### Unit Tests

Run unit tests:

```bash
./mvnw test
```

### Integration Tests

Run integration tests with Testcontainers:

```bash
./mvnw verify
```

Integration tests use Testcontainers to spin up PostgreSQL automatically.

### Test Coverage

- **Repository Tests**: 5 test classes covering all repositories
- **Service Tests**: 3 test classes for core services
- **Controller Tests**: 2 test classes for REST endpoints
- **Integration Tests**: End-to-end workflow testing

### Test Structure

```
src/test/java/
├── BaseIntegrationTest.java          # Base class for integration tests
├── BaseRepositoryTest.java           # Base class for repository tests
├── UserServiceIntegrationTest.java   # E2E integration tests
├── domain/
│   └── repository/
│       ├── UserRepositoryTest.java
│       ├── BusinessUnitRepositoryTest.java
│       ├── UserBusinessUnitRepositoryTest.java
│       ├── HierarchyRepositoryTest.java
│       └── UserRoleRepositoryTest.java
├── service/
│   ├── UserServiceTest.java
│   ├── KeycloakSyncServiceTest.java
│   └── HierarchyServiceTest.java
└── web/
    ├── UserControllerTest.java
    └── BusinessUnitControllerTest.java
```

## Docker Deployment

### Build Docker Image

```bash
# Using the build script
./build.sh

# Or manually
docker build -t eflo/user-service:1.0.0 .
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Start only user service and dependencies
docker-compose up -d postgres-user keycloak user-service

# View logs
docker-compose logs -f user-service

# Stop services
docker-compose down
```

### Environment Configuration

Create a `.env` file:

```env
# Database
USER_DB_PASSWORD=userpass

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_DB_PASSWORD=keycloakpass

# Other services
ORDER_DB_PASSWORD=orderpass
WORKFLOW_DB_PASSWORD=workflowpass
COMMISSION_DB_PASSWORD=commissionpass
DOCUMENT_DB_PASSWORD=documentpass
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
```

## Development Guide

### Project Structure

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/com/eflo/user/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── domain/
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── enums/           # Enumerations
│   │   │   │   └── repository/      # Spring Data repositories
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── integration/         # External integrations
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   ├── service/             # Business logic
│   │   │   ├── util/                # Utility classes
│   │   │   ├── web/                 # REST controllers
│   │   │   └── UserServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       ├── application-test.yml
│   │       └── db/migration/        # Flyway migrations
│   └── test/                        # Test files
├── Dockerfile
├── build.sh
├── pom.xml
└── README.md
```

### Adding New Features

1. **Create Entity**: Add JPA entity in `domain/entity/`
2. **Create Repository**: Add Spring Data repository in `domain/repository/`
3. **Create DTO**: Add request/response DTOs in `domain/dto/`
4. **Create Mapper**: Add MapStruct mapper in `mapper/`
5. **Create Service**: Add business logic in `service/`
6. **Create Controller**: Add REST endpoints in `web/`
7. **Add Tests**: Add unit and integration tests
8. **Add Migration**: Create Flyway migration in `resources/db/migration/`

### Code Style

- Use Lombok annotations to reduce boilerplate
- Follow RESTful API conventions
- Write comprehensive JavaDoc comments
- Maintain high test coverage
- Use meaningful variable and method names
- Follow Spring Boot best practices

### Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`

Naming convention: `V{version}__{description}.sql`

Example: `V1__initial_schema.sql`

### API Documentation

Access Swagger UI at: http://localhost:8084/swagger-ui.html

The API documentation is automatically generated using SpringDoc OpenAPI.

## Troubleshooting

### Common Issues

1. **Cannot connect to database**
   - Check PostgreSQL is running
   - Verify database credentials
   - Check port 5438 is not in use

2. **Keycloak connection failed**
   - Ensure Keycloak is running on port 8180
   - Verify Keycloak admin credentials
   - Check realm name is correct (eflo)

3. **Tests failing**
   - Ensure Docker is running (for Testcontainers)
   - Check Java version is 21+
   - Verify Maven dependencies are downloaded

4. **Port already in use**
   - Change port in `application.yml`
   - Kill process using port 8084: `lsof -ti:8084 | xargs kill -9`

### Logging

Adjust logging levels in `application.yml`:

```yaml
logging:
  level:
    com.eflo.user: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## Contributing

1. Follow the existing code style
2. Write tests for new features
3. Update documentation
4. Create meaningful commit messages
5. Submit pull requests for review

## License

Copyright 2024 Eflo Platform

---

For more information, visit the [Eflo Platform Documentation](https://docs.eflo.com)
