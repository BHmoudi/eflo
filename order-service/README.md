# Order Service

Order Management Service for Eflo Microservices - Handles VN (New), VO (Used), and EVO (Evolution) vehicle orders.

## Overview

The Order Service manages the complete order lifecycle for vehicle sales, including:
- Order creation, updates, and status management
- Pricing calculation with margins and discounts
- Options, accessories, services, aids, and supplements management
- Delivery scheduling and tracking
- Event streaming via Kafka
- Integration with Workflow, Commission, and Document services

## Features

- **Order Types**: VN (New Vehicle), VO (Used Vehicle), EVO (Evolution)
- **Pricing Engine**: Automatic calculation of totals, VAT, discounts, and margins
- **Status Management**: Complete order lifecycle from DRAFT to INVOICED
- **Delivery Management**: Schedule, track, and complete deliveries
- **Event Streaming**: Kafka-based event publishing for order events
- **Security**: JWT-based authentication with Keycloak
- **Audit Trail**: Complete history of order changes

## Tech Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **PostgreSQL 15** (database)
- **Apache Kafka** (event streaming)
- **Spring Cloud Netflix Eureka** (service discovery)
- **Keycloak** (authentication/authorization)
- **Redis** (caching)
- **Flyway** (database migrations)
- **Docker & Docker Compose**

## Database Schema

### Main Tables
- `orders` - Main order table
- `order_options` - Factory and dealer options
- `order_accessories` - Aftermarket accessories
- `order_contract_services` - Extended warranties and maintenance contracts
- `order_aids` - Financial aids and subsidies
- `order_supplements` - Additional charges and fees
- `order_deliveries` - Delivery tracking
- `order_history` - Complete audit trail

## API Endpoints

### Order Management

```
POST   /api/v1/orders                          - Create new order
GET    /api/v1/orders/{id}                     - Get order by ID
GET    /api/v1/orders/number/{orderNumber}    - Get order by number
GET    /api/v1/orders/customer/{customerId}   - Get customer orders
GET    /api/v1/orders/salesperson/{id}        - Get salesperson orders
GET    /api/v1/orders/business-unit/{id}      - Get business unit orders
GET    /api/v1/orders/status/{status}         - Get orders by status
GET    /api/v1/orders/active                  - Get all active orders
PUT    /api/v1/orders/{id}                    - Update order
PATCH  /api/v1/orders/{id}/status/{status}   - Change order status
POST   /api/v1/orders/{id}/validate           - Validate order
POST   /api/v1/orders/{id}/recalculate        - Recalculate pricing
POST   /api/v1/orders/{id}/discount           - Apply discount
DELETE /api/v1/orders/{id}                    - Delete order (soft)
```

### Delivery Management

```
POST   /api/v1/deliveries/order/{orderId}/schedule  - Schedule delivery
PUT    /api/v1/deliveries/{deliveryId}              - Update delivery
POST   /api/v1/deliveries/{deliveryId}/complete     - Complete delivery
POST   /api/v1/deliveries/{deliveryId}/cancel       - Cancel delivery
GET    /api/v1/deliveries/order/{orderId}           - Get order deliveries
GET    /api/v1/deliveries/date/{date}               - Get deliveries by date
GET    /api/v1/deliveries/status/{status}           - Get deliveries by status
```

## Order Status Flow

```
DRAFT → PENDING → CONFIRMED → IN_PRODUCTION → READY_FOR_DELIVERY → DELIVERED → INVOICED
                     ↓                                     ↓
                ON_HOLD                              ON_HOLD
                     ↓                                     ↓
                CANCELLED                            CANCELLED
```

## Pricing Calculation

The pricing engine calculates:

1. **Subtotal** = Base Price + Options + Accessories + Services + Supplements
2. **Discount** = Discount Amount OR (Subtotal × Discount Percentage)
3. **Total Before Tax** = Subtotal - Discount - Aids
4. **VAT Amount** = Total Before Tax × VAT Rate
5. **Total Amount** = Total Before Tax + VAT Amount

### Margin Calculation

- **Total Cost** = Base Cost + Options Cost + Accessories Cost + Services Cost
- **Gross Margin** = Total Amount - Total Cost
- **Net Margin** = Total Before Tax - Total Cost
- **Margin Percentage** = (Net Margin / Total Amount) × 100

## Events Published

The service publishes the following Kafka events:

- `orders.created` - Order created
- `orders.updated` - Order updated
- `orders.status-changed` - Order status changed
- `orders.validated` - Order validated
- `orders.delivered` - Order delivered
- `orders.cancelled` - Order cancelled
- `orders.price-changed` - Order pricing changed
- `orders.data-changed` - Order data changed

## Security & Roles

### Roles
- `ROLE_SALES_MANAGER` - Full access to all order operations
- `ROLE_SALESPERSON` - Create and manage own orders
- `ROLE_VIEWER` - Read-only access

### JWT Configuration
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/eflo
```

## Setup & Running

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### Local Development

1. **Clone the repository**
```bash
git clone <repository-url>
cd order-service
```

2. **Set environment variables**
```bash
export ORDER_DB_PASSWORD=orderpass
```

3. **Run with Docker Compose**
```bash
docker-compose up -d
```

4. **Run locally (without Docker)**
```bash
# Start PostgreSQL, Kafka, Eureka separately
mvn spring-boot:run
```

### Build & Run

```bash
# Build
mvn clean package

# Run
java -jar target/order-service-1.0.0-SNAPSHOT.jar

# Run with Docker profile
java -jar -Dspring.profiles.active=docker target/order-service-1.0.0-SNAPSHOT.jar
```

## Configuration

### Application Profiles

- **default** - Local development (localhost databases)
- **docker** - Docker environment

### Key Configuration

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db

  kafka:
    bootstrap-servers: localhost:9092

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## API Documentation

Swagger UI is available at:
- **Local**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/v3/api-docs

## Health Check

```bash
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`.

Migrations run automatically on startup. To manually run:
```bash
mvn flyway:migrate
```

## Example API Calls

### Create Order
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "orderType": "VN",
    "customerId": 1,
    "businessUnitId": 1,
    "salespersonId": 1,
    "basePrice": 25000.00,
    "vatRate": 20.00,
    "make": "Toyota",
    "model": "Camry",
    "year": 2024
  }'
```

### Get Order
```bash
curl http://localhost:8081/api/v1/orders/1 \
  -H "Authorization: Bearer <token>"
```

### Recalculate Pricing
```bash
curl -X POST http://localhost:8081/api/v1/orders/1/recalculate \
  -H "Authorization: Bearer <token>"
```

### Schedule Delivery
```bash
curl -X POST "http://localhost:8081/api/v1/deliveries/order/1/schedule?deliveryType=HOME_DELIVERY&scheduledDate=2024-03-15" \
  -H "Authorization: Bearer <token>"
```

## Monitoring

### Actuator Endpoints
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

## Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres-order

# Check logs
docker logs postgres-order
```

### Kafka Connection Issues
```bash
# Check Kafka is running
docker ps | grep kafka

# Test connectivity
docker exec -it kafka-order kafka-topics --list --bootstrap-server localhost:9092
```

### Service Discovery Issues
```bash
# Check Eureka registration
curl http://localhost:8761/eureka/apps/ORDER-SERVICE
```

## Development

### Project Structure
```
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/eflo/order/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── domain/          # Entities, DTOs, repositories
│   │   │   ├── service/         # Business logic
│   │   │   ├── web/             # REST controllers
│   │   │   └── mapper/          # Entity-DTO mappers
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # Flyway migrations
│   └── test/
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Contributing

1. Create feature branch
2. Make changes
3. Run tests: `mvn test`
4. Submit pull request

## License

Copyright © 2024 Eflo. All rights reserved.

## Support

For issues or questions, contact: dev@eflo.com
