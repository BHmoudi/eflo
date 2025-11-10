# Commission Service

## Overview

The Commission Service manages sales commission calculations for the Eflo platform. It calculates, tracks, and manages commissions for salespeople based on vehicle orders, applying configurable commission rules and rates.

## Functionality

### Core Features

- **Commission Calculation**: Automatic calculation based on order values and commission rules
- **Commission Rules**: Configurable rules for different order types, vehicle models, and sales conditions
- **Tiered Rates**: Support for tiered commission structures (e.g., 0-50K: 3%, 50K-100K: 4%, 100K+: 5%)
- **Salesperson Tracking**: Link commissions to specific salespeople
- **Payment Tracking**: Track commission payments and outstanding amounts
- **Event-Driven**: Listens to order events for automatic commission creation
- **Reporting**: Commission reports by salesperson, period, business unit
- **Audit Trail**: Complete history of commission calculations and adjustments

### Technical Details

- **Port**: 8082
- **Database**: PostgreSQL (port 5435)
- **Technology**: Spring Boot 3.x, Spring Data JPA, Spring Cloud
- **API Documentation**: Swagger/OpenAPI at `http://localhost:8082/swagger-ui.html`

## Database Schema

### Main Entities

- **Commission**: Individual commission records linked to orders
- **CommissionRule**: Configuration for commission calculation logic
- **CommissionRate**: Tiered or flat rate configurations
- **CommissionPayment**: Payment records for paid commissions
- **CommissionAdjustment**: Manual adjustments to commissions

### Commission Status

- **PENDING**: Commission calculated but not yet approved
- **APPROVED**: Approved for payment
- **PAID**: Commission has been paid
- **CANCELLED**: Commission cancelled (e.g., order cancelled)
- **ADJUSTED**: Commission amount manually adjusted

## API Endpoints

### Commission Management

#### Get All Commissions
```http
GET /api/v1/commissions
GET /api/v1/commissions?status=PENDING
GET /api/v1/commissions?salespersonId=123
```

#### Get Commission by ID
```http
GET /api/v1/commissions/{id}
```

#### Get Commissions by Order
```http
GET /api/v1/commissions/order/{orderId}
```

#### Get Commissions by Salesperson
```http
GET /api/v1/commissions/salesperson/{salespersonId}
GET /api/v1/commissions/salesperson/{salespersonId}?from=2025-01-01&to=2025-12-31
```

#### Create Commission
```http
POST /api/v1/commissions
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "orderId": 123,
  "salespersonId": 456,
  "orderAmount": 50000.00,
  "commissionRate": 3.5,
  "commissionAmount": 1750.00,
  "status": "PENDING"
}
```

#### Update Commission
```http
PUT /api/v1/commissions/{id}
Content-Type: application/json

{
  "status": "APPROVED",
  "notes": "Approved by sales manager"
}
```

#### Approve Commission
```http
PATCH /api/v1/commissions/{id}/approve
```

#### Mark Commission as Paid
```http
POST /api/v1/commissions/{id}/pay
Content-Type: application/json

{
  "paymentDate": "2025-11-30",
  "paymentMethod": "BANK_TRANSFER",
  "paymentReference": "TXN-123456"
}
```

### Commission Rules

#### Create Commission Rule
```http
POST /api/v1/commissions/rules
Content-Type: application/json

{
  "name": "Standard VN Commission",
  "orderType": "VN",
  "baseRate": 3.0,
  "minOrderAmount": 0,
  "maxOrderAmount": null,
  "active": true
}
```

#### Get All Rules
```http
GET /api/v1/commissions/rules
GET /api/v1/commissions/rules?active=true
```

#### Get Applicable Rule for Order
```http
GET /api/v1/commissions/rules/applicable?orderType=VN&orderAmount=50000
```

### Commission Rates (Tiered)

#### Create Tiered Rate
```http
POST /api/v1/commissions/rates
Content-Type: application/json

{
  "ruleId": 1,
  "tier": 1,
  "minAmount": 0,
  "maxAmount": 50000,
  "rate": 3.0
}
```

#### Get Rates for Rule
```http
GET /api/v1/commissions/rates/rule/{ruleId}
```

### Reporting

#### Get Salesperson Commission Summary
```http
GET /api/v1/commissions/reports/salesperson/{salespersonId}/summary?year=2025&month=11
```

Response:
```json
{
  "salespersonId": 456,
  "totalOrders": 15,
  "totalOrderAmount": 750000.00,
  "totalCommissionAmount": 26250.00,
  "pendingCommission": 5000.00,
  "paidCommission": 21250.00
}
```

#### Get Business Unit Commission Report
```http
GET /api/v1/commissions/reports/business-unit/{businessUnitId}?from=2025-01-01&to=2025-12-31
```

## Configuration

### Application Properties

```yaml
server:
  port: 8082

spring:
  application:
    name: commission-service
  datasource:
    url: jdbc:postgresql://localhost:5435/commission_db
    username: commissionuser
    password: ${COMMISSION_DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: commission-service
```

### Environment Variables

- `COMMISSION_DB_PASSWORD`: Database password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker URLs
- `KEYCLOAK_ISSUER_URI`: Keycloak issuer URI

## Commission Calculation Logic

### Basic Calculation

```
Commission Amount = Order Amount × Commission Rate / 100
```

### Tiered Calculation

For tiered rates, the calculation applies different rates to different portions:

```
Order Amount: $75,000

Tier 1: $0 - $50,000 @ 3% = $1,500
Tier 2: $50,000 - $75,000 @ 4% = $1,000
Total Commission: $2,500
```

### Rule Matching

Commission rules are matched based on:
1. Order type (VN, VO, EVO)
2. Order amount range
3. Vehicle model (optional)
4. Business unit (optional)
5. Customer type (optional)

Priority order:
1. Most specific rule (multiple criteria matched)
2. Order type specific rule
3. Default rule

## Event Handling

### Kafka Event Listeners

#### Order Delivered Event

```java
@KafkaListener(topics = "orders.delivered")
public void handleOrderDelivered(OrderDeliveredEvent event) {
    // Find applicable commission rule
    // Calculate commission amount
    // Create commission record
    // Notify salesperson
}
```

#### Order Cancelled Event

```java
@KafkaListener(topics = "orders.cancelled")
public void handleOrderCancelled(OrderCancelledEvent event) {
    // Find associated commission
    // Mark as CANCELLED
    // Reverse payment if already paid
}
```

## Development

### Build

```bash
cd commission-service
mvn clean package
```

### Run Tests

```bash
mvn test
mvn verify
```

### Run Locally

```bash
export COMMISSION_DB_PASSWORD=commissionpass
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d commission-service
```

### Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:

```
V1__initial_schema.sql
V2__add_commission_rules.sql
V3__add_tiered_rates.sql
V4__add_payment_tracking.sql
```

## Testing

### Health Check

```bash
curl http://localhost:8082/actuator/health
```

### Test Commission Calculation

```bash
TOKEN="your-jwt-token"

# Create test commission
curl -X POST http://localhost:8082/api/v1/commissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 123,
    "salespersonId": 456,
    "orderAmount": 50000.00,
    "commissionRate": 3.5
  }'
```

### Get Salesperson Commissions

```bash
curl http://localhost:8082/api/v1/commissions/salesperson/456 \
  -H "Authorization: Bearer $TOKEN"
```

## Security

### Required Roles

- **Create Commission**: `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`
- **Approve Commission**: `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`
- **Mark as Paid**: `ADMIN_LOCAL`, `SUPER_ADMIN`
- **View Own Commissions**: `SALESPERSON` (own commissions only)
- **View All Commissions**: `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`

## Integration Points

### Order Service
- Listens to order delivery events
- Fetches order details for commission calculation

### User Service
- Validates salesperson IDs
- Retrieves business unit hierarchy for reporting

## Monitoring

### Metrics

Available at `/actuator/metrics`:
- `commissions.created.count`
- `commissions.approved.count`
- `commissions.paid.count`
- `commissions.total.amount`

### Logging

```yaml
logging:
  level:
    com.eflo.commission: DEBUG
    org.springframework.kafka: INFO
```

## Troubleshooting

### Commission not created automatically
- Verify Kafka listener is active
- Check order delivered event is being published
- Ensure applicable commission rule exists
- Review service logs for errors

### Incorrect commission amount
- Verify commission rule configuration
- Check tiered rate setup
- Review order amount calculation
- Test commission calculation logic

## Additional Resources

- API Documentation: `http://localhost:8082/swagger-ui.html`
- Database Schema: `src/main/resources/db/migration/`
