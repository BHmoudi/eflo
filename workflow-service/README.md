# Workflow Service

## Overview

The Workflow Service is the orchestration engine for the Eflo platform. It manages business process workflows, state machines, task assignments, approvals, and escalations. It integrates with n8n for external automation and provides comprehensive workflow lifecycle management.

## Functionality

### Core Features

- **Workflow Process Management**: Define, version, and activate workflow templates
- **State Machine Engine**: Manage workflow states and transitions
- **Task Management**: Create, assign, and track workflow tasks
- **Approval Chains**: Multi-level approval workflows with delegation support
- **Automatic Escalation**: Time-based task escalation (24h, 48h, 72h, 168h thresholds)
- **Deadline Monitoring**: Hourly scheduled checks for task deadlines
- **n8n Integration**: Webhook integration for external automation (AI document analysis, WhatsApp notifications)
- **Event Listening**: Kafka integration to trigger workflows from order events
- **Bilingual Support**: French and English workflow content
- **JSONB Configuration**: Flexible runtime behavior through JSON configuration

### Technical Details

- **Port**: 8091
- **Database**: PostgreSQL (port 5434)
- **Technology**: Spring Boot 3.x, Spring State Machine, Spring Data JPA
- **External Integrations**: n8n webhooks, Kafka event streaming
- **API Documentation**: Swagger/OpenAPI at `http://localhost:8091/swagger-ui.html`

## Database Schema

### Main Entities

- **WorkflowProcess**: Template definitions for business processes (versioned, activatable)
- **WorkflowState**: States within processes (START, NORMAL, END types)
- **WorkflowTransition**: Allowed state progressions with conditions
- **WorkflowInstance**: Runtime execution tracking for specific orders
- **WorkflowInstanceTask**: Individual task instances with escalation support
- **ApprovalChain**: Multi-level approval configuration
- **ApprovalChainStep**: Individual approval steps
- **TaskAssignment**: User/role task assignments

### State Types

- **START**: Initial state when workflow instance is created
- **NORMAL**: Intermediate processing states
- **END**: Terminal state indicating completion

## API Endpoints

### Workflow Process Management

#### Create Workflow Process
```http
POST /api/v1/workflows/processes
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "name": "Vehicle Order Approval",
  "code": "VO_APPROVAL",
  "description": "Standard approval workflow for vehicle orders",
  "version": "1.0",
  "active": true,
  "configuration": {
    "autoStart": true,
    "maxDuration": 168
  }
}
```

#### Get All Workflow Processes
```http
GET /api/v1/workflows/processes
GET /api/v1/workflows/processes?active=true
```

#### Get Process by ID
```http
GET /api/v1/workflows/processes/{id}
```

#### Update Workflow Process
```http
PUT /api/v1/workflows/processes/{id}
```

#### Activate/Deactivate Process
```http
PATCH /api/v1/workflows/processes/{id}/activate
PATCH /api/v1/workflows/processes/{id}/deactivate
```

### Workflow State Management

#### Create State
```http
POST /api/v1/workflows/processes/{processId}/states
Content-Type: application/json

{
  "name": "Manager Approval",
  "code": "MANAGER_APPROVAL",
  "type": "NORMAL",
  "configuration": {
    "requiresApproval": true,
    "escalationThreshold": 48,
    "notifyOnEntry": true
  }
}
```

#### Get Process States
```http
GET /api/v1/workflows/processes/{processId}/states
```

### Workflow Transition Management

#### Create Transition
```http
POST /api/v1/workflows/transitions
Content-Type: application/json

{
  "fromStateId": 1,
  "toStateId": 2,
  "event": "APPROVE",
  "condition": "order.totalAmount < 50000",
  "order": 1
}
```

#### Get Transitions
```http
GET /api/v1/workflows/transitions/state/{stateId}
```

### Workflow Instance Management

#### Create Workflow Instance
```http
POST /api/v1/workflows/instances
Content-Type: application/json

{
  "processId": 1,
  "orderId": 123,
  "businessUnitId": 5,
  "assignedUserId": 10,
  "variables": {
    "orderAmount": 45000,
    "customerType": "VIP"
  }
}
```

#### Get Instance by ID
```http
GET /api/v1/workflows/instances/{id}
```

#### Get Instances by Order
```http
GET /api/v1/workflows/instances/order/{orderId}
```

#### Transition Instance
```http
POST /api/v1/workflows/instances/{id}/transition
Content-Type: application/json

{
  "event": "APPROVE",
  "comment": "Approved by manager",
  "variables": {
    "approvalDate": "2025-11-10T10:30:00"
  }
}
```

### Task Management

#### Create Task
```http
POST /api/v1/workflows/tasks
Content-Type: application/json

{
  "instanceId": 1,
  "name": "Review Order",
  "description": "Review and approve vehicle order",
  "assignedUserId": 10,
  "assignedRoleId": 2,
  "dueDate": "2025-11-15T17:00:00",
  "priority": "HIGH"
}
```

#### Get Task by ID
```http
GET /api/v1/workflows/tasks/{id}
```

#### Get User Tasks
```http
GET /api/v1/workflows/tasks/user/{userId}
GET /api/v1/workflows/tasks/user/{userId}?status=PENDING
```

#### Complete Task
```http
POST /api/v1/workflows/tasks/{id}/complete
Content-Type: application/json

{
  "outcome": "APPROVED",
  "comment": "Order looks good, approved",
  "variables": {
    "approvalLevel": "MANAGER"
  }
}
```

#### Escalate Task
```http
POST /api/v1/workflows/tasks/{id}/escalate
Content-Type: application/json

{
  "escalationLevel": 1,
  "escalatedToUserId": 15,
  "reason": "Original assignee unavailable"
}
```

### Approval Chain Management

#### Create Approval Chain
```http
POST /api/v1/workflows/approval-chains
Content-Type: application/json

{
  "name": "High Value Order Approval",
  "workflowProcessId": 1,
  "steps": [
    {
      "stepOrder": 1,
      "approverRoleId": 2,
      "requiredApprovals": 1,
      "autoApprove": false
    },
    {
      "stepOrder": 2,
      "approverRoleId": 1,
      "requiredApprovals": 2,
      "autoApprove": false
    }
  ]
}
```

#### Get Approval Chains
```http
GET /api/v1/workflows/approval-chains
GET /api/v1/workflows/approval-chains/process/{processId}
```

## Configuration

### Application Properties

```yaml
server:
  port: 8091

spring:
  application:
    name: workflow-service
  datasource:
    url: jdbc:postgresql://localhost:5434/workflow_db
    username: workflowuser
    password: ${WORKFLOW_DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: workflow-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

### Environment Variables

- `WORKFLOW_DB_PASSWORD`: Database password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker URLs (default: localhost:9092)
- `KEYCLOAK_ISSUER_URI`: Keycloak issuer URI
- `N8N_WEBHOOK_URL`: n8n webhook base URL

## Workflow Lifecycle

### 1. Process Definition

1. Create WorkflowProcess (template)
2. Define WorkflowStates (START, NORMAL, END)
3. Create WorkflowTransitions (allowed paths between states)
4. Configure ApprovalChains (if needed)
5. Activate the process

### 2. Instance Execution

```
Order Created Event (Kafka)
    ↓
Create WorkflowInstance
    ↓
Set to START state
    ↓
Create Initial Task(s)
    ↓
User completes task
    ↓
Transition to next state
    ↓
Create next task(s)
    ↓
Repeat until END state
```

### 3. Task Lifecycle

```
PENDING → IN_PROGRESS → COMPLETED
           ↓
       ESCALATED → IN_PROGRESS → COMPLETED
           ↓
       OVERDUE (if deadline passed)
```

## Escalation System

### Automatic Escalation

Tasks are automatically escalated based on configurable thresholds:

- **Level 1**: 24 hours after creation
- **Level 2**: 48 hours after creation
- **Level 3**: 72 hours after creation (3 days)
- **Level 4**: 168 hours after creation (7 days)

### Escalation Configuration

```java
@Configuration
public class EscalationConfig {

    private static final Map<Integer, Long> ESCALATION_THRESHOLDS = Map.of(
        1, 24L,   // 24 hours
        2, 48L,   // 48 hours
        3, 72L,   // 3 days
        4, 168L   // 7 days
    );
}
```

### Deadline Monitoring

A scheduled job runs hourly to check for tasks past their deadline:

```java
@Scheduled(cron = "0 0 * * * *")  // Every hour
public void checkDeadlines() {
    // Find overdue tasks
    // Escalate as needed
    // Send notifications
}
```

## n8n Integration

The workflow service integrates with n8n for external automation:

### Webhook URLs

- **Document Analysis**: `POST http://localhost:5678/webhook/analyze-document`
- **WhatsApp Notification**: `POST http://localhost:5678/webhook/send-whatsapp`
- **Task Validation**: `POST http://localhost:5678/webhook/validate-task`

### Webhook Payload Example

```json
{
  "workflowInstanceId": 123,
  "taskId": 456,
  "orderId": 789,
  "action": "ANALYZE_DOCUMENT",
  "data": {
    "documentUrl": "https://minio:9000/documents/invoice.pdf",
    "documentType": "INVOICE"
  }
}
```

### Response Handling

n8n workflows can send results back to workflow service:

```http
POST /api/v1/workflows/webhooks/n8n-callback
Content-Type: application/json

{
  "taskId": 456,
  "result": "APPROVED",
  "data": {
    "aiAnalysis": "Document validated successfully",
    "confidence": 0.95
  }
}
```

## Event Handling

### Kafka Event Listeners

The service listens to order events to trigger workflows:

#### Order Created Event

```java
@KafkaListener(topics = "orders.created")
public void handleOrderCreated(OrderCreatedEvent event) {
    // Find appropriate workflow process
    // Create workflow instance
    // Start execution
}
```

#### Order Status Changed Event

```java
@KafkaListener(topics = "orders.status-changed")
public void handleStatusChanged(OrderStatusChangedEvent event) {
    // Find active workflow instance
    // Trigger appropriate transition
    // Update task statuses
}
```

## Development

### Build

```bash
cd workflow-service
mvn clean package
```

### Run Tests

```bash
mvn test
mvn verify  # includes integration tests
```

### Run Locally

```bash
# Set environment variables
export WORKFLOW_DB_PASSWORD=workflowpass

# Run service
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d workflow-service
```

### Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`:

```
V1__initial_schema.sql
V2__add_approval_chains.sql
V3__add_escalation_config.sql
V4__add_n8n_integration.sql
```

## Testing

### Health Check

```bash
curl http://localhost:8091/actuator/health
```

### Create Test Workflow

```bash
TOKEN="your-jwt-token"

# Create process
curl -X POST http://localhost:8091/api/v1/workflows/processes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Workflow",
    "code": "TEST_WF",
    "version": "1.0",
    "active": true
  }'

# Create start state
curl -X POST http://localhost:8091/api/v1/workflows/processes/1/states \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Start",
    "code": "START",
    "type": "START"
  }'
```

## Security

### Required Roles

- **Create Workflow**: `ADMIN_LOCAL`, `SUPER_ADMIN`
- **Manage Instances**: `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`
- **Complete Tasks**: `SALESPERSON`, `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`
- **View Workflows**: `VIEWER` (all roles)

### Endpoint Protection

```java
@PreAuthorize("hasAnyRole('ADMIN_LOCAL', 'SUPER_ADMIN')")
@PostMapping("/processes")
public ResponseEntity<WorkflowProcessDto> createProcess(@RequestBody WorkflowProcessDto dto) {
    // ...
}
```

## Integration Points

### Order Service
- Listens to order events (`orders.created`, `orders.status-changed`)
- Triggers workflow instances based on order data
- Updates order status based on workflow completion

### User Service
- Validates task assignments to users
- Checks user roles for approvals
- Retrieves manager hierarchy for escalations

### Document Service
- Integrates with n8n for document processing
- Links workflow instances to documents

### n8n
- Sends webhooks for external automation
- Receives callbacks with results
- Handles AI document analysis and WhatsApp notifications

## Monitoring

### Metrics

Available at `/actuator/metrics`:
- `workflows.instances.active` - Active workflow instances
- `workflows.tasks.pending` - Pending tasks
- `workflows.tasks.overdue` - Overdue tasks
- `workflows.escalations.count` - Total escalations

### Logging

Configure logging levels:

```yaml
logging:
  level:
    com.eflo.workflow: DEBUG
    org.springframework.statemachine: INFO
    org.springframework.kafka: INFO
```

## Troubleshooting

### Workflow instance stuck
- Check current state: `GET /api/v1/workflows/instances/{id}`
- Verify available transitions from current state
- Check condition evaluation for transitions
- Review task statuses

### Tasks not escalating
- Verify deadline monitoring job is running (check logs)
- Check escalation configuration in database
- Ensure escalation thresholds are configured
- Review task creation timestamps

### n8n webhooks failing
- Verify n8n is running: `curl http://localhost:5678`
- Check webhook URLs in configuration
- Review n8n workflow logs
- Verify network connectivity

### Events not triggering workflows
- Check Kafka connectivity: `docker logs workflow-service`
- Verify event listener registration
- Ensure appropriate workflow process is active
- Review order event payload structure

## Performance Considerations

### Database Optimization
- Indexes on instanceId, status, dueDate
- Pagination for task queries
- Lazy loading for related entities

### Caching
- Cache active workflow processes
- Cache state machine configurations
- Use Redis for distributed caching (optional)

### Async Processing
- Async event handling with Kafka
- Background jobs for deadline monitoring
- Non-blocking n8n webhook calls

## Additional Resources

- API Documentation: `http://localhost:8091/swagger-ui.html`
- Spring State Machine: https://spring.io/projects/spring-statemachine
- n8n Documentation: https://docs.n8n.io/
- Kafka Streams: https://kafka.apache.org/documentation/streams/
