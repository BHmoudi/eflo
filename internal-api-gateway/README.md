# Internal API Gateway

## Overview

The Internal API Gateway is the entry point for internal service-to-service communication within the Eflo microservices platform. It routes requests to appropriate microservices, handles authentication, and provides cross-cutting concerns like rate limiting and request logging.

## Functionality

### Core Features

- **Request Routing**: Routes requests to appropriate microservices based on path patterns
- **Service Discovery Integration**: Uses Eureka to dynamically discover service instances
- **Load Balancing**: Client-side load balancing across multiple service instances
- **Authentication & Authorization**: OAuth2/JWT token validation with Keycloak
- **Request/Response Logging**: Comprehensive logging for debugging and auditing
- **CORS Configuration**: Cross-origin request handling
- **Circuit Breaker**: Resilience patterns with Spring Cloud Circuit Breaker
- **Rate Limiting**: Request throttling per service/endpoint (if configured)

### Technical Details

- **Port**: 8080
- **Technology**: Spring Cloud Gateway
- **Security**: OAuth2 Resource Server with JWT
- **Service Discovery**: Eureka Client

## Configuration

### Application Properties

```yaml
server:
  port: 8080

spring:
  application:
    name: internal-api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```

### Environment Variables

None specific to this service (inherits from Config Server).

## Routing Configuration

### Service Routes

The gateway routes requests to the following services:

| Path Pattern | Target Service | Port |
|--------------|----------------|------|
| `/api/v1/orders/**` | order-service | 8089 |
| `/api/v1/workflows/**` | workflow-service | 8091 |
| `/api/v1/commissions/**` | commission-service | 8082 |
| `/api/v1/documents/**` | document-service | 8083 |
| `/api/v1/users/**` | user-service | 8084 |
| `/api/v1/business-units/**` | user-service | 8084 |
| `/api/v1/hierarchies/**` | user-service | 8084 |
| `/api/v1/roles/**` | user-service | 8084 |

### Route Configuration Example

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**
          filters:
            - StripPrefix=0

        - id: workflow-service
          uri: lb://workflow-service
          predicates:
            - Path=/api/v1/workflows/**
          filters:
            - StripPrefix=0
```

## Security

### OAuth2 JWT Validation

All requests are validated against Keycloak:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8180/realms/eflo
```

### Protected Endpoints

All routes require valid JWT tokens with appropriate roles:
- `SUPER_ADMIN` - Full access to all endpoints
- `ADMIN_LOCAL` - Administrative access to local resources
- `SALES_MANAGER` - Sales and order management
- `SALESPERSON` - Order creation and viewing
- `VIEWER` - Read-only access

### Public Endpoints

The following endpoints are accessible without authentication:
- `/actuator/health` - Health check
- `/actuator/info` - Service information

## API Endpoints

### Gateway Management

- **GET** `/actuator/health` - Gateway health status
- **GET** `/actuator/gateway/routes` - List all configured routes
- **GET** `/actuator/gateway/routes/{id}` - Get specific route details
- **POST** `/actuator/gateway/refresh` - Refresh routes
- **GET** `/actuator/metrics` - Gateway metrics

### Proxied Service Endpoints

All service endpoints are proxied through the gateway with the same paths.

## Development

### Build

```bash
cd internal-api-gateway
mvn clean package
```

### Run Locally

```bash
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d internal-api-gateway
```

## Testing

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### List All Routes

```bash
curl http://localhost:8080/actuator/gateway/routes
```

### Test Order Service Routing

```bash
# Get JWT token from Keycloak first
TOKEN="your-jwt-token"

# Call order service through gateway
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/orders
```

### Test Workflow Service Routing

```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/workflows
```

## Request Flow

```
Client Request
    ↓
Internal API Gateway (8080)
    ↓
JWT Validation (Keycloak)
    ↓
Service Discovery (Eureka)
    ↓
Load Balancing
    ↓
Target Microservice
    ↓
Response back through Gateway
```

## Filters

### Global Filters

Applied to all routes:
- **Authentication Filter**: Validates JWT tokens
- **Logging Filter**: Logs request/response details
- **Correlation ID Filter**: Adds correlation IDs for request tracing

### Route-Specific Filters

- **StripPrefix**: Removes path prefixes before forwarding
- **AddRequestHeader**: Adds custom headers to requests
- **RetryFilter**: Retries failed requests
- **CircuitBreakerFilter**: Implements circuit breaker pattern

## Circuit Breaker Configuration

```yaml
spring:
  cloud:
    circuitbreaker:
      resilience4j:
        enabled: true

resilience4j:
  circuitbreaker:
    instances:
      default:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30000
```

## CORS Configuration

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
            allowed-headers: "*"
            allow-credentials: true
```

## Monitoring

### Metrics

Available at `/actuator/metrics`:
- `gateway.requests` - Total requests through gateway
- `gateway.route.requests` - Requests per route
- `gateway.filter.requests` - Filter execution metrics
- `http.server.requests` - HTTP request metrics

### Logging

Configure logging levels:

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.security: DEBUG
    reactor.netty.http.client: DEBUG
```

## Troubleshooting

### 503 Service Unavailable
- Target service is not running or not registered with Eureka
- Check Eureka dashboard: `http://localhost:8761`
- Verify service health: `curl http://localhost:8089/actuator/health`

### 401 Unauthorized
- JWT token is missing or invalid
- Token has expired (default: 5 minutes)
- Check Keycloak issuer URI configuration
- Verify user has required roles

### Route not found (404)
- Check route configuration: `curl http://localhost:8080/actuator/gateway/routes`
- Verify path pattern matches request URL
- Refresh routes: `curl -X POST http://localhost:8080/actuator/gateway/refresh`

### Timeout errors
- Increase timeout configuration:
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 30s
```

## Dependencies

Requires the following services:
- **eureka-server** - Service discovery
- **config-server** - Configuration management
- **keycloak** - Authentication

## Internal vs External Gateway

**Internal Gateway (8080)**:
- Used for service-to-service communication
- Stricter security policies
- Lower timeout values
- Not exposed to public internet

**External Gateway (8081)**:
- Used for client applications (workflow-admin)
- Additional CORS configuration
- Higher timeout values
- May include rate limiting

## Additional Resources

- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [OAuth2 Resource Server Guide](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
