# Order Condition System - Implementation Summary

## Overview

A fully configurable and customizable condition system for orders has been implemented. This system allows business rules to be defined and managed in the database without requiring code changes.

## Architecture

### Database Schema (7 Tables)

1. **condition_category** - Categories like VN (Véhicule Neuf), VO (Véhicule Occasion)
2. **order_condition** - Master conditions (DIAC, PARTICULIER, REPRISE VO, etc.)
3. **order_condition_rule** - Business rules that trigger conditions
4. **order_condition_criteria** - Individual criteria within rules
5. **order_assigned_condition** - Links orders to their active conditions
6. **condition_field_definition** - Available fields for rule building (UI support)
7. **condition_dependency** - Condition dependencies (REQUIRES, EXCLUDES, IMPLIES)

### Key Features

✅ **Fully Configurable** - All rules in database, no code changes needed
✅ **Flexible Operators** - 18 operators including EQUALS, IN, CONTAINS, GREATER_THAN, EXISTS, COUNT_*, etc.
✅ **Complex Logic** - Support AND/OR combinations with grouped criteria
✅ **Audit Trail** - Track which rule triggered each condition
✅ **Manual Override** - Manually assign/remove conditions
✅ **Dependency Management** - Conditions can require, exclude, or imply other conditions

## Components Created

### 1. Database Migrations

- **V11__create_order_conditions_tables.sql** - Creates all 7 tables with indexes
- **V12__seed_order_conditions.sql** - Seeds VN/VO conditions with rules

### 2. JPA Entities (7 entities)

- `ConditionCategory.java`
- `OrderCondition.java`
- `OrderConditionRule.java`
- `OrderConditionCriteria.java`
- `OrderAssignedCondition.java`
- `ConditionFieldDefinition.java`
- `ConditionDependency.java`

### 3. Repositories (7 repositories)

- `ConditionCategoryRepository.java`
- `OrderConditionRepository.java`
- `OrderConditionRuleRepository.java`
- `OrderConditionCriteriaRepository.java`
- `OrderAssignedConditionRepository.java`
- `ConditionFieldDefinitionRepository.java`
- `ConditionDependencyRepository.java`

### 4. Service Layer

- `ConditionEvaluationService.java` - Core evaluation engine with:
  - Rule evaluation logic
  - Operator implementations
  - Dependency resolution
  - Condition assignment/removal

### 5. DTOs (8 DTOs)

- `ConditionCategoryDTO.java`
- `OrderConditionDTO.java`
- `OrderConditionRuleDTO.java`
- `OrderConditionCriteriaDTO.java`
- `OrderAssignedConditionDTO.java`
- `ConditionFieldDefinitionDTO.java`
- `CreateConditionRequest.java`
- `CreateRuleRequest.java`

### 6. REST API Controller

- `ConditionController.java` - Complete REST API with endpoints for:
  - Condition CRUD operations
  - Rule creation and management
  - Order condition evaluation
  - Manual condition assignment
  - Field definition queries

## Supported Operators

| Operator | Description | Example |
|----------|-------------|---------|
| EQUALS | Exact match | `order.make = "RENAULT"` |
| NOT_EQUALS | Not equal | `order.make != "PA"` |
| IN | Value in array | `order.productType IN ["ND1", "ND2"]` |
| NOT_IN | Value not in array | `order.productType NOT IN ["DLA"]` |
| GREATER_THAN | Numeric comparison | `order.tradeinValue > 0` |
| LESS_THAN | Numeric comparison | `order.tradeinValue < 10000` |
| GREATER_THAN_OR_EQUAL | Numeric comparison | `order.tradeinValue >= 0` |
| LESS_THAN_OR_EQUAL | Numeric comparison | `order.tradeinValue <= 50000` |
| CONTAINS | String contains | `order.fuelType CONTAINS "Electrique"` |
| STARTS_WITH | String starts with | `order.productType STARTS_WITH "ND"` |
| ENDS_WITH | String ends with | `order.make ENDS_WITH "DACIA"` |
| EXISTS | Collection has items | `order.accessories EXISTS` |
| NOT_EXISTS | Collection is empty | `order.supplements NOT_EXISTS` |
| COUNT_EQUALS | Count equals | `COUNT(order.services) = 2` |
| COUNT_GREATER_THAN | Count greater than | `COUNT(order.aids) > 0` |
| COUNT_LESS_THAN | Count less than | `COUNT(order.options) < 5` |
| IS_NULL | Field is null | `order.financingInstitution IS NULL` |
| IS_NOT_NULL | Field is not null | `order.tradeinValue IS NOT NULL` |

## Seeded Conditions

### VN (Véhicule Neuf) Conditions

1. **REPRISE_VO_VN** - Trade-in Used Vehicle
2. **DIAC_VN** - DIAC Financing
3. **PARTICULIER_VN** - Individual Customer
4. **ENTREPRISE_VN** - Business Customer
5. **ACCESSOIRES_VN** - Accessories
6. **CONTRAT_SERVICE_VN** - Service Contract
7. **VD_AGENT_VN** - Agent Seller
8. **INTERMEDIAIRE_VN** - Intermediary
9. **LOUEUR_VN** - Renter
10. **TRANSPORT_VN** - Transport Charges
11. **SEC_GRAV_VN** - Security Engraving
12. **DIAC_LOC_VN** - DIAC Leasing
13. **ER_VN** - Engagement/Remuneration
14. **DACIA_VN** - Dacia Brand
15. **RENAULT_VN** - Renault Brand
16. **ALPINE_VN** - Alpine Brand
17. **ZE_VN** - Zero Emission (Electric)
18. **HE_VN** - Hybrid Electric
19. **VS_VN** - Véhicule de Sortie
20. **REPVOPRIMCONV_VN** - Trade-in with Conversion Prime
21. **BONUS_VN** - Super Bonus
22. **AUTO_SEPHERE_VN** - CGI Financier
23. **CEE_VN** - Certificats d'Économies d'Énergie

### VO (Véhicule Occasion) Conditions

1. **REPRISE_VO** - Trade-in Used Vehicle
2. **DIAC_VO** - DIAC Financing
3. **PARTICULIER_VO** - Individual Customer
4. **ENTREPRISE_VO** - Business Customer
5. **ZE_VO** - Zero Emission (Electric)
6. **HE_VO** - Hybrid Electric
7. **EN_VO** - Entretien (Maintenance)
8. **EX_VO** - Warranty Extension
9. **LOUEUR_VO** - Renter
10. **GRAVAGE_VO** - Security Engraving
11. **ER_VO** - Engagement/Remuneration
12. **SOLOVI_VO** - Specific Origin Stock
13. **AUTO_SEPHERE_VO** - CGI Financier
14. **REPVOPRIMCONV_VO** - Trade-in with Conversion Prime
15. **BONUS_VO** - Super Bonus

## API Endpoints

### Condition Management

```http
GET    /api/v1/conditions                    # Get all conditions
GET    /api/v1/conditions/{id}               # Get condition by ID
GET    /api/v1/conditions/category/{code}    # Get conditions by category
POST   /api/v1/conditions                    # Create new condition
PUT    /api/v1/conditions/{id}               # Update condition
DELETE /api/v1/conditions/{id}               # Delete condition
```

### Rule Management

```http
POST   /api/v1/conditions/rules              # Create new rule with criteria
GET    /api/v1/conditions/{id}/rules         # Get rules for condition
```

### Order Conditions

```http
POST   /api/v1/conditions/orders/{orderId}/evaluate           # Evaluate conditions
GET    /api/v1/conditions/orders/{orderId}                    # Get assigned conditions
POST   /api/v1/conditions/orders/{orderId}/assign/{conditionId}  # Manually assign
DELETE /api/v1/conditions/orders/{orderId}/conditions/{conditionId}  # Remove condition
```

### Field Definitions

```http
GET    /api/v1/conditions/fields             # Get all field definitions
GET    /api/v1/conditions/fields/entity/{entity}  # Get fields by entity
```

### Categories

```http
GET    /api/v1/conditions/categories         # Get all categories
GET    /api/v1/conditions/categories/{id}    # Get category by ID
```

## Example Rule Configuration

### DIAC Condition (Simple)

```json
{
  "conditionId": 2,
  "name": "DIAC Product Type Rule",
  "logicalOperator": "AND",
  "priority": 0,
  "isActive": true,
  "criteria": [
    {
      "fieldPath": "order.productType",
      "operator": "IN",
      "value": ["ND1", "ND2", "ND3", "CB", "CBE", "CD", "CC", "LPC", "LPD"],
      "sequence": 0
    }
  ]
}
```

### Accessories Condition (Complex - OR logic)

```json
{
  "conditionId": 5,
  "name": "Accessories Rule",
  "logicalOperator": "OR",
  "priority": 0,
  "isActive": true,
  "criteria": [
    {
      "fieldPath": "order.accessoriesTotal",
      "operator": "GREATER_THAN",
      "value": 0,
      "criteriaGroup": 1,
      "sequence": 0
    },
    {
      "fieldPath": "order.accessories",
      "operator": "EXISTS",
      "value": null,
      "criteriaGroup": 2,
      "sequence": 1
    }
  ]
}
```

### Electric Vehicle Condition (String Contains)

```json
{
  "conditionId": 17,
  "name": "Electric Vehicle Rule",
  "logicalOperator": "AND",
  "priority": 0,
  "isActive": true,
  "criteria": [
    {
      "fieldPath": "order.fuelType",
      "operator": "CONTAINS",
      "value": "Electrique",
      "sequence": 0
    }
  ]
}
```

## How It Works

### 1. Condition Evaluation Flow

```
Order Created/Updated
    ↓
evaluateAndAssignConditions(order)
    ↓
Get conditions for order category (VN/VO)
    ↓
For each condition:
    ↓
    Get active rules
    ↓
    For each rule:
        ↓
        Evaluate criteria (AND/OR logic)
        ↓
        If TRUE → Assign condition
    ↓
Apply dependency rules (REQUIRES/EXCLUDES/IMPLIES)
    ↓
Save assigned conditions
```

### 2. Criterion Evaluation

1. Extract field value from order using field path
2. Apply operator logic (EQUALS, IN, GREATER_THAN, etc.)
3. Return boolean result
4. Combine results using rule's logical operator

### 3. Dependency Resolution

- **REQUIRES**: Condition A requires Condition B (if B not present, remove A)
- **EXCLUDES**: Condition A excludes Condition B (if B present, remove A)
- **IMPLIES**: Condition A implies Condition B (if A present, auto-add B)

## Usage Examples

### Evaluate conditions for an order

```bash
POST /api/v1/conditions/orders/123/evaluate
```

Response:
```json
[
  {
    "id": 1,
    "orderId": 123,
    "conditionCode": "DIAC_VN",
    "conditionLabel": "DIAC",
    "conditionColor": "#2196F3",
    "assignedBy": "SYSTEM",
    "isManual": false,
    "assignedAt": "2025-10-05T10:30:00"
  },
  {
    "id": 2,
    "orderId": 123,
    "conditionCode": "PARTICULIER_VN",
    "conditionLabel": "PARTICULIER",
    "conditionColor": "#FF9800",
    "assignedBy": "SYSTEM",
    "isManual": false,
    "assignedAt": "2025-10-05T10:30:00"
  }
]
```

### Create a new condition

```bash
POST /api/v1/conditions
Content-Type: application/json

{
  "code": "NEW_CONDITION",
  "label": "New Condition",
  "description": "Description of new condition",
  "categoryId": 1,
  "priority": 10,
  "isActive": true,
  "color": "#00FF00"
}
```

### Create a new rule

```bash
POST /api/v1/conditions/rules
Content-Type: application/json

{
  "conditionId": 1,
  "name": "My Custom Rule",
  "logicalOperator": "AND",
  "criteria": [
    {
      "fieldPath": "order.totalAmount",
      "operator": "GREATER_THAN",
      "value": 50000
    },
    {
      "fieldPath": "order.make",
      "operator": "EQUALS",
      "value": "RENAULT"
    }
  ]
}
```

## Integration with Order Service

### Update Order entity

The `Order` entity now includes:

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<OrderAssignedCondition> assignedConditions = new ArrayList<>();
```

### Auto-evaluation trigger points

You can trigger condition evaluation:
1. When order is created
2. When order is updated
3. Manually via API endpoint
4. As part of order workflow transitions

### Example integration in OrderService

```java
@Transactional
public OrderDTO createOrder(CreateOrderRequest request, Long userId) {
    Order order = // ... create order
    order = orderRepository.save(order);

    // Auto-evaluate conditions
    conditionEvaluationService.evaluateAndAssignConditions(order);

    return orderMapper.toDTO(order);
}
```

## Benefits

1. **No Code Changes** - Add/modify conditions via API or database
2. **Business User Friendly** - UI can be built on top of this API
3. **Auditable** - Track when and how conditions were assigned
4. **Flexible** - Support any combination of rules and operators
5. **Testable** - Each rule can be tested independently
6. **Scalable** - Easy to add new condition types and categories
7. **Migration Ready** - Can import existing dossier conditions

## Next Steps

1. **UI Development** - Build admin UI for condition management
2. **Field Mapping** - Map more Order fields to field definitions
3. **Custom Fields** - Add support for custom order fields
4. **Rule Templates** - Create reusable rule templates
5. **Bulk Operations** - Add bulk condition evaluation endpoints
6. **Analytics** - Add condition usage analytics
7. **Import Tool** - Tool to import existing dossier conditions

## Files Created

### Database Migrations
- `src/main/resources/db/migration/V11__create_order_conditions_tables.sql`
- `src/main/resources/db/migration/V12__seed_order_conditions.sql`

### Entities
- `src/main/java/com/eflo/order/domain/entity/ConditionCategory.java`
- `src/main/java/com/eflo/order/domain/entity/OrderCondition.java`
- `src/main/java/com/eflo/order/domain/entity/OrderConditionRule.java`
- `src/main/java/com/eflo/order/domain/entity/OrderConditionCriteria.java`
- `src/main/java/com/eflo/order/domain/entity/OrderAssignedCondition.java`
- `src/main/java/com/eflo/order/domain/entity/ConditionFieldDefinition.java`
- `src/main/java/com/eflo/order/domain/entity/ConditionDependency.java`

### Repositories
- `src/main/java/com/eflo/order/domain/repository/ConditionCategoryRepository.java`
- `src/main/java/com/eflo/order/domain/repository/OrderConditionRepository.java`
- `src/main/java/com/eflo/order/domain/repository/OrderConditionRuleRepository.java`
- `src/main/java/com/eflo/order/domain/repository/OrderConditionCriteriaRepository.java`
- `src/main/java/com/eflo/order/domain/repository/OrderAssignedConditionRepository.java`
- `src/main/java/com/eflo/order/domain/repository/ConditionFieldDefinitionRepository.java`
- `src/main/java/com/eflo/order/domain/repository/ConditionDependencyRepository.java`

### Services
- `src/main/java/com/eflo/order/service/ConditionEvaluationService.java`

### DTOs
- `src/main/java/com/eflo/order/domain/model/dto/ConditionCategoryDTO.java`
- `src/main/java/com/eflo/order/domain/model/dto/OrderConditionDTO.java`
- `src/main/java/com/eflo/order/domain/model/dto/OrderConditionRuleDTO.java`
- `src/main/java/com/eflo/order/domain/model/dto/OrderConditionCriteriaDTO.java`
- `src/main/java/com/eflo/order/domain/model/dto/OrderAssignedConditionDTO.java`
- `src/main/java/com/eflo/order/domain/model/dto/ConditionFieldDefinitionDTO.java`
- `src/main/java/com/eflo/order/domain/model/dto/CreateConditionRequest.java`
- `src/main/java/com/eflo/order/domain/model/dto/CreateRuleRequest.java`

### Controllers
- `src/main/java/com/eflo/order/web/ConditionController.java`

### Documentation
- `database/design/order-conditions-schema.md`
- `CONDITION-SYSTEM-IMPLEMENTATION.md` (this file)
