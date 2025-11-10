# External API Gateway

## Overview

The External API Gateway is the public-facing entry point for client applications (like workflow-admin) to access the Eflo microservices platform. It provides secure routing, authentication, and cross-cutting concerns optimized for external clients.

## Functionality

### Core Features

- **Request Routing**: Routes client requests to appropriate microservices
- **Service Discovery Integration**: Uses Eureka to dynamically discover service instances
- **Load Balancing**: Client-side load balancing across multiple service instances
- **Authentication & Authorization**: OAuth2/JWT token validation with Keycloak
- **CORS Support**: Comprehensive CORS configuration for browser-based clients
- **Rate Limiting**: Protection against abuse and DDoS attacks
- **Request/Response Logging**: Audit trail for external requests
- **Circuit Breaker**: Resilience patterns for fault tolerance
- **API Documentation**: Centralized Swagger/OpenAPI documentation

### Technical Details

- **Port**: 8081
- **Technology**: Spring Cloud Gateway
- **Security**: OAuth2 Resource Server with JWT
- **Service Discovery**: Eureka Client
- **Target Clients**: workflow-admin (Next.js), mobile apps, third-party integrations

## Configuration

### Application Properties

```yaml
server:
  port: 8081

spring:
  application:
    name: external-api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins:
              - "http://localhost:3001"
              - "https://your-production-domain.com"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600
```

### Environment Variables

None specific to this service (inherits from Config Server).

## Routing Configuration

### Service Routes

The gateway routes requests to the following services:

| Path Pattern | Target Service | Port | Description |
|--------------|----------------|------|-------------|
| `/api/v1/orders/**` | order-service | 8089 | Order management |
| `/api/v1/workflows/**` | workflow-service | 8091 | Workflow processes |
| `/api/v1/commissions/**` | commission-service | 8082 | Commission calculations |
| `/api/v1/documents/**` | document-service | 8083 | Document operations |
| `/api/v1/users/**` | user-service | 8084 | User management |
| `/api/v1/business-units/**` | user-service | 8084 | Organizational structure |
| `/api/v1/hierarchies/**` | user-service | 8084 | Reporting hierarchies |
| `/api/v1/roles/**` | user-service | 8084 | Role management |

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
            - name: CircuitBreaker
              args:
                name: orderServiceCircuitBreaker
                fallbackUri: forward:/fallback/orders

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
          issuer-uri: http://localhost:8180/realms/eflo
```

### Role-Based Access Control

- `SUPER_ADMIN` - Full access to all endpoints
- `ADMIN_LOCAL` - Administrative access to local resources
- `SALES_MANAGER` - Sales and order management
- `SALESPERSON` - Order creation and viewing
- `VIEWER` - Read-only access

### Public Endpoints

The following endpoints are accessible without authentication:
- `/actuator/health` - Health check
- `/actuator/info` - Service information
- `/swagger-ui.html` - API documentation (if enabled)

### Rate Limiting

Protect against abuse:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10  # requests per second
                  burstCapacity: 20
```

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
cd external-api-gateway
mvn clean package
```

### Run Locally

```bash
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d external-api-gateway
```

## Testing

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### Test with workflow-admin

The Next.js admin dashboard uses this gateway:

```typescript
// workflow-admin API configuration
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081';
```

### Test Order Service Routing

```bash
# Get JWT token from Keycloak first
TOKEN="your-jwt-token"

# Call order service through gateway
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8081/api/v1/orders
```

### Test CORS

```bash
curl -X OPTIONS http://localhost:8081/api/v1/orders \
  -H "Origin: http://localhost:3001" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v
```

## Request Flow

```
Client (workflow-admin)
    ↓
External API Gateway (8081)
    ↓
CORS Validation
    ↓
JWT Validation (Keycloak)
    ↓
Rate Limiting Check
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
- **CORS Filter**: Handles CORS preflight and headers
- **Authentication Filter**: Validates JWT tokens
- **Logging Filter**: Logs request/response details
- **Correlation ID Filter**: Adds correlation IDs for request tracing
- **Rate Limiting Filter**: Throttles excessive requests

### Route-Specific Filters

- **StripPrefix**: Removes path prefixes before forwarding
- **AddRequestHeader**: Adds custom headers to requests
- **RetryFilter**: Retries failed requests
- **CircuitBreakerFilter**: Implements circuit breaker pattern
- **RequestRateLimiter**: Per-route rate limiting

## Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      orderServiceCircuitBreaker:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30000
        permitted-number-of-calls-in-half-open-state: 3
      workflowServiceCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50
```

## CORS Configuration

### Development

```yaml
allowed-origins:
  - "http://localhost:3001"  # workflow-admin dev server
  - "http://localhost:3000"  # alternative dev port
```

### Production

```yaml
allowed-origins:
  - "https://admin.eflo.com"
  - "https://app.eflo.com"
```

## Timeout Configuration

External gateway has higher timeouts for client requests:

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 10000  # 10 seconds
        response-timeout: 60s   # 60 seconds
```

## Monitoring

### Metrics

Available at `/actuator/metrics`:
- `gateway.requests` - Total requests through gateway
- `gateway.route.requests` - Requests per route
- `http.server.requests` - HTTP request metrics
- `resilience4j.circuitbreaker.calls` - Circuit breaker metrics

### Request Tracing

Each request receives a correlation ID:

```
X-Correlation-ID: 123e4567-e89b-12d3-a456-426614174000
```

### Logging

Configure logging levels:

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.security: INFO
    org.springframework.web.cors: DEBUG
```

## Troubleshooting

### CORS errors in browser
- Check allowed origins include your client URL
- Verify CORS configuration: `curl -X OPTIONS ...`
- Ensure `allow-credentials: true` if sending cookies/auth headers
- Check browser console for specific CORS error

### 503 Service Unavailable
- Target service is not running or not registered with Eureka
- Check Eureka dashboard: `http://localhost:8761`
- Verify service health: `curl http://localhost:8089/actuator/health`

### 401 Unauthorized
- JWT token is missing or invalid
- Token has expired
- Check Keycloak issuer URI configuration
- Verify user has required roles

### 429 Too Many Requests
- Rate limiting is active
- Check rate limit configuration
- Wait before retrying
- Contact administrator if limit is too restrictive

### Connection timeout
- Increase timeout configuration
- Check network connectivity to target service
- Verify target service is responsive

## Dependencies

Requires the following services:
- **eureka-server** - Service discovery
- **config-server** - Configuration management
- **keycloak** - Authentication

## Internal vs External Gateway

**External Gateway (8081)**:
- Public-facing for client applications
- Comprehensive CORS configuration
- Higher timeout values
- Rate limiting enabled
- Additional security layers

**Internal Gateway (8080)**:
- Service-to-service communication only
- No CORS configuration
- Lower timeout values
- Stricter security policies

## Integration with workflow-admin

The Next.js admin dashboard connects to this gateway:

```typescript
// workflow-admin/.env.local
NEXT_PUBLIC_API_URL=http://localhost:8081
```

All API calls from the frontend go through this gateway:
- User authentication
- Order management
- Workflow operations
- Document uploads/downloads
- User and role management

## Additional Resources

- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [OAuth2 Resource Server Guide](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [CORS Configuration Guide](https://spring.io/guides/gs/rest-service-cors/)
