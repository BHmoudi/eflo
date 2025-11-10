# Eureka Server

## Overview

Eureka Server is the service discovery component of the Eflo microservices platform. It provides a registry where all microservices register themselves and discover other services dynamically.

## Functionality

### Core Features

- **Service Registration**: All microservices register with Eureka on startup
- **Service Discovery**: Services can discover and communicate with each other without hard-coded URLs
- **Health Monitoring**: Tracks the health status of registered services
- **Load Balancing**: Enables client-side load balancing through service instance tracking
- **Self-Preservation Mode**: Protects against network partition scenarios

### Technical Details

- **Port**: 8761
- **Technology**: Spring Cloud Netflix Eureka
- **Dashboard**: Available at `http://localhost:8761`

## Configuration

### Application Properties

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: true
```

### Environment Variables

No specific environment variables required.

## API Endpoints

### Eureka Dashboard
- **URL**: `http://localhost:8761`
- **Description**: Web UI showing all registered services and their instances

### REST API
- **GET** `/eureka/apps` - List all registered applications
- **GET** `/eureka/apps/{appName}` - Get specific application details
- **POST** `/eureka/apps/{appName}` - Register a new instance
- **DELETE** `/eureka/apps/{appName}/{instanceId}` - Deregister an instance

## Registered Services

The following services register with Eureka:

1. **config-server** (8888)
2. **internal-api-gateway** (8080)
3. **external-api-gateway** (8081)
4. **order-service** (8089)
5. **workflow-service** (8091)
6. **commission-service** (8082)
7. **document-service** (8083)
8. **user-service** (8084)

## Development

### Build

```bash
cd eureka-server
mvn clean package
```

### Run Locally

```bash
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d eureka-server
```

## Health Check

```bash
curl http://localhost:8761/actuator/health
```

## Troubleshooting

### Services not appearing in registry
- Check if service has `eureka.client.enabled=true`
- Verify network connectivity between service and Eureka
- Check service logs for registration errors
- Ensure `spring.application.name` is set in service configuration

### Self-preservation mode activated
- This is normal during development when services are frequently restarted
- Can be disabled with `eureka.server.enable-self-preservation=false` (not recommended for production)

## Dependencies

This service should start before all other microservices to ensure proper registration.

## Additional Resources

- [Spring Cloud Netflix Eureka Documentation](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)
