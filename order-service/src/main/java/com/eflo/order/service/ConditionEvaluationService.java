package com.eflo.order.service;

import com.eflo.order.domain.entity.*;
import com.eflo.order.domain.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionEvaluationService {

    private final OrderConditionRepository conditionRepository;
    private final OrderConditionRuleRepository ruleRepository;
    private final OrderAssignedConditionRepository assignedConditionRepository;
    private final ConditionDependencyRepository dependencyRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    /**
     * Evaluate and assign conditions to an order by ID (fetches from DB)
     * Used by Kafka listeners and API endpoints
     */
    @Transactional
    public List<OrderAssignedCondition> evaluateAndAssignConditions(Long orderId) {
        Order order = findOrderById(orderId);
        if (order == null) {
            log.error("Order not found for condition evaluation: {}", orderId);
            return Collections.emptyList();
        }
        return evaluateAndAssignConditions(order);
    }

    /**
     * Evaluate and assign conditions to an order based on its type (VN, VO, etc.)
     * Internal method - use overloaded method with orderId for external calls
     */
    @Transactional
    public List<OrderAssignedCondition> evaluateAndAssignConditions(Order order) {
        log.info("Evaluating conditions for order: {}", order.getOrderNumber());

        // Get category code from order type
        String categoryCode = order.getOrderType().name(); // VN, VO, EVO

        // Remove existing auto-assigned conditions
        removeAutoAssignedConditions(order.getId());

        // Get all active conditions for this category, ordered by priority
        List<OrderCondition> conditions = conditionRepository.findActiveByCategoryCode(categoryCode);

        List<OrderAssignedCondition> assignedConditions = new ArrayList<>();

        for (OrderCondition condition : conditions) {
            if (evaluateCondition(order, condition)) {
                OrderAssignedCondition assigned = assignCondition(order, condition, null, false);
                assignedConditions.add(assigned);
                log.debug("Condition {} applied to order {}", condition.getCode(), order.getOrderNumber());
            }
        }

        // Apply dependency rules
        applyDependencies(order, assignedConditions);

        log.info("Assigned {} conditions to order {}", assignedConditions.size(), order.getOrderNumber());
        return assignedConditions;
    }

    /**
     * Asynchronously evaluate and assign conditions to an order
     * This method runs in a separate thread and doesn't block the order creation
     * Note: @Transactional is on the inner method, not here, to avoid transaction propagation issues
     */
    @Async("conditionEvaluationExecutor")
    public void evaluateAndAssignConditionsAsync(Long orderId) {
        log.info("Async condition evaluation started for order ID: {}", orderId);
        long startTime = System.currentTimeMillis();

        try {
            // Fetch order from database (in new transaction)
            Order order = findOrderById(orderId);
            if (order == null) {
                log.error("Order not found for async condition evaluation: {}", orderId);
                return;
            }

            // Evaluate and assign conditions
            log.debug("Fetched order {}, starting condition evaluation", order.getOrderNumber());
            List<OrderAssignedCondition> assigned = evaluateAndAssignConditions(order);

            long elapsedMs = System.currentTimeMillis() - startTime;
            log.info("Async condition evaluation completed for order {} in {}ms. Assigned {} conditions",
                     order.getOrderNumber(), elapsedMs, assigned.size());

        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.error("Error during async condition evaluation for order {} after {}ms: {}",
                      orderId, elapsedMs, e.getMessage(), e);
        }
    }

    /**
     * Evaluate a single condition against an order
     */
    public boolean evaluateCondition(Order order, OrderCondition condition) {
        List<OrderConditionRule> rules = ruleRepository.findActiveByConditionId(condition.getId());

        // If no rules, condition is not automatically assigned
        if (rules.isEmpty()) {
            return false;
        }

        // Evaluate each rule - if any rule passes, condition is activated
        for (OrderConditionRule rule : rules) {
            if (evaluateRule(order, rule)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluate a single rule against an order
     */
    private boolean evaluateRule(Order order, OrderConditionRule rule) {
        List<OrderConditionCriteria> criteria = rule.getCriteria();

        if (criteria.isEmpty()) {
            return false;
        }

        // Group criteria by criteria_group
        Map<Integer, List<OrderConditionCriteria>> groupedCriteria = criteria.stream()
                .collect(Collectors.groupingBy(OrderConditionCriteria::getCriteriaGroup));

        List<Boolean> groupResults = new ArrayList<>();

        // Evaluate each group
        for (List<OrderConditionCriteria> group : groupedCriteria.values()) {
            boolean groupResult = evaluateCriteriaGroup(order, group, rule.getLogicalOperator());
            groupResults.add(groupResult);
        }

        // Combine group results with OR logic (any group can satisfy the rule)
        return groupResults.stream().anyMatch(result -> result);
    }

    /**
     * Evaluate a group of criteria with the specified logical operator
     */
    private boolean evaluateCriteriaGroup(Order order, List<OrderConditionCriteria> criteria,
                                          OrderConditionRule.LogicalOperator operator) {
        List<Boolean> results = new ArrayList<>();

        for (OrderConditionCriteria criterion : criteria) {
            boolean result = evaluateCriterion(order, criterion);
            results.add(result);
        }

        // Apply logical operator
        if (operator == OrderConditionRule.LogicalOperator.AND) {
            return results.stream().allMatch(result -> result);
        } else { // OR
            return results.stream().anyMatch(result -> result);
        }
    }

    /**
     * Evaluate a single criterion against an order
     */
    private boolean evaluateCriterion(Order order, OrderConditionCriteria criterion) {
        try {
            Object fieldValue = getFieldValue(order, criterion.getFieldPath());
            Object criterionValue = criterion.getValue();

            return evaluateOperator(fieldValue, criterion.getOperator(), criterionValue);
        } catch (Exception e) {
            log.error("Error evaluating criterion: {}", criterion.getFieldPath(), e);
            return false;
        }
    }

    /**
     * Get field value from order using reflection or direct access
     */
    private Object getFieldValue(Order order, String fieldPath) {
        // Handle nested paths like "order.tradeinValue"
        String[] parts = fieldPath.split("\\.");

        if (parts.length < 2) {
            return null;
        }

        String entity = parts[0]; // "order"
        String field = parts[1];  // "tradeinValue"

        if (!"order".equals(entity)) {
            return null;
        }

        // Map field names to Order getter methods
        return switch (field) {
            case "tradeinValue" -> order.getTradeinValue();
            case "productType" -> order.getProductType();
            case "make" -> order.getMake();
            case "fuelType" -> order.getFuelType();
            case "financingInstitution" -> order.getFinancingInstitution();
            case "accessoriesTotal" -> order.getAccessoriesTotal();
            case "accessories" -> order.getAccessories();
            case "contractServices" -> order.getContractServices();
            case "commercialActions" -> order.getCommercialActions();
            case "orderType" -> order.getOrderType() != null ? order.getOrderType().name() : null;
            case "supplements" -> order.getSupplements();
            case "aids" -> order.getAids();
            case "customerType" -> order.getCustomerType();
            case "options" -> order.getOptions();
            default -> null;
        };
    }

    /**
     * Evaluate operator against values
     */
    private boolean evaluateOperator(Object fieldValue, OrderConditionCriteria.CriteriaOperator operator,
                                      Object criterionValue) {
        return switch (operator) {
            case EQUALS -> equals(fieldValue, criterionValue);
            case NOT_EQUALS -> !equals(fieldValue, criterionValue);
            case IN -> in(fieldValue, criterionValue);
            case NOT_IN -> !in(fieldValue, criterionValue);
            case GREATER_THAN -> greaterThan(fieldValue, criterionValue);
            case LESS_THAN -> lessThan(fieldValue, criterionValue);
            case GREATER_THAN_OR_EQUAL -> greaterThanOrEqual(fieldValue, criterionValue);
            case LESS_THAN_OR_EQUAL -> lessThanOrEqual(fieldValue, criterionValue);
            case CONTAINS -> contains(fieldValue, criterionValue);
            case STARTS_WITH -> startsWith(fieldValue, criterionValue);
            case ENDS_WITH -> endsWith(fieldValue, criterionValue);
            case EXISTS -> exists(fieldValue);
            case NOT_EXISTS -> !exists(fieldValue);
            case COUNT_EQUALS -> countEquals(fieldValue, criterionValue);
            case COUNT_GREATER_THAN -> countGreaterThan(fieldValue, criterionValue);
            case COUNT_LESS_THAN -> countLessThan(fieldValue, criterionValue);
            case IS_NULL -> fieldValue == null;
            case IS_NOT_NULL -> fieldValue != null;
        };
    }

    // Operator implementations

    private boolean equals(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        String criterionStr = extractStringValue(criterionValue);
        return fieldValue.toString().equals(criterionStr);
    }

    private boolean in(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        try {
            JsonNode arrayNode = objectMapper.readTree(criterionValue.toString());
            if (arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    if (fieldValue.toString().equals(node.asText())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing IN criterion value", e);
        }
        return false;
    }

    private boolean greaterThan(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        try {
            BigDecimal fieldNum = new BigDecimal(fieldValue.toString());
            BigDecimal criterionNum = new BigDecimal(extractStringValue(criterionValue));
            return fieldNum.compareTo(criterionNum) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean lessThan(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        try {
            BigDecimal fieldNum = new BigDecimal(fieldValue.toString());
            BigDecimal criterionNum = new BigDecimal(extractStringValue(criterionValue));
            return fieldNum.compareTo(criterionNum) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean greaterThanOrEqual(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        try {
            BigDecimal fieldNum = new BigDecimal(fieldValue.toString());
            BigDecimal criterionNum = new BigDecimal(extractStringValue(criterionValue));
            return fieldNum.compareTo(criterionNum) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean lessThanOrEqual(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        try {
            BigDecimal fieldNum = new BigDecimal(fieldValue.toString());
            BigDecimal criterionNum = new BigDecimal(extractStringValue(criterionValue));
            return fieldNum.compareTo(criterionNum) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean contains(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        String criterionStr = extractStringValue(criterionValue);
        return fieldValue.toString().contains(criterionStr);
    }

    private boolean startsWith(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        String criterionStr = extractStringValue(criterionValue);
        return fieldValue.toString().startsWith(criterionStr);
    }

    private boolean endsWith(Object fieldValue, Object criterionValue) {
        if (fieldValue == null || criterionValue == null) return false;
        String criterionStr = extractStringValue(criterionValue);
        return fieldValue.toString().endsWith(criterionStr);
    }

    private boolean exists(Object fieldValue) {
        if (fieldValue == null) return false;
        if (fieldValue instanceof Collection) {
            return !((Collection<?>) fieldValue).isEmpty();
        }
        return true;
    }

    private boolean countEquals(Object fieldValue, Object criterionValue) {
        if (!(fieldValue instanceof Collection)) return false;
        try {
            int count = ((Collection<?>) fieldValue).size();
            int expectedCount = Integer.parseInt(extractStringValue(criterionValue));
            return count == expectedCount;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean countGreaterThan(Object fieldValue, Object criterionValue) {
        if (!(fieldValue instanceof Collection)) return false;
        try {
            int count = ((Collection<?>) fieldValue).size();
            int threshold = Integer.parseInt(extractStringValue(criterionValue));
            return count > threshold;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean countLessThan(Object fieldValue, Object criterionValue) {
        if (!(fieldValue instanceof Collection)) return false;
        try {
            int count = ((Collection<?>) fieldValue).size();
            int threshold = Integer.parseInt(extractStringValue(criterionValue));
            return count < threshold;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractStringValue(Object value) {
        if (value == null) return "";
        String str = value.toString();
        // Remove quotes if present
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    /**
     * Assign a condition to an order
     */
    @Transactional
    public OrderAssignedCondition assignCondition(Order order, OrderCondition condition,
                                                   OrderConditionRule triggeredRule, boolean isManual) {
        // Check if already assigned
        Optional<OrderAssignedCondition> existing = assignedConditionRepository
                .findByOrderIdAndConditionId(order.getId(), condition.getId());

        if (existing.isPresent()) {
            return existing.get();
        }

        OrderAssignedCondition assigned = OrderAssignedCondition.builder()
                .order(order)
                .condition(condition)
                .rule(triggeredRule)
                .assignedAt(LocalDateTime.now())
                .assignedBy(isManual ? "USER" : "SYSTEM")
                .isManual(isManual)
                .build();

        return assignedConditionRepository.save(assigned);
    }

    /**
     * Remove auto-assigned conditions for an order
     */
    @Transactional
    public void removeAutoAssignedConditions(Long orderId) {
        List<OrderAssignedCondition> autoAssigned = assignedConditionRepository
                .findByOrderIdAndIsManual(orderId, false);
        assignedConditionRepository.deleteAll(autoAssigned);
    }

    /**
     * Apply dependency rules
     */
    private void applyDependencies(Order order, List<OrderAssignedCondition> assignedConditions) {
        Set<Long> assignedConditionIds = assignedConditions.stream()
                .map(ac -> ac.getCondition().getId())
                .collect(Collectors.toSet());

        for (OrderAssignedCondition assigned : new ArrayList<>(assignedConditions)) {
            List<ConditionDependency> dependencies = dependencyRepository
                    .findByConditionId(assigned.getCondition().getId());

            for (ConditionDependency dependency : dependencies) {
                switch (dependency.getDependencyType()) {
                    case REQUIRES:
                        // If required condition is not present, remove this condition
                        if (!assignedConditionIds.contains(dependency.getDependsOnCondition().getId())) {
                            assignedConditionRepository.delete(assigned);
                            assignedConditions.remove(assigned);
                        }
                        break;
                    case EXCLUDES:
                        // If excluded condition is present, remove this condition
                        if (assignedConditionIds.contains(dependency.getDependsOnCondition().getId())) {
                            assignedConditionRepository.delete(assigned);
                            assignedConditions.remove(assigned);
                        }
                        break;
                    case IMPLIES:
                        // If this condition is present, add the implied condition
                        if (!assignedConditionIds.contains(dependency.getDependsOnCondition().getId())) {
                            OrderAssignedCondition implied = assignCondition(
                                    order, dependency.getDependsOnCondition(), null, false);
                            assignedConditions.add(implied);
                            assignedConditionIds.add(dependency.getDependsOnCondition().getId());
                        }
                        break;
                }
            }
        }
    }

    /**
     * Helper method to find order by ID
     */
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * Get assigned conditions for an order
     */
    public List<OrderAssignedCondition> getAssignedConditions(Long orderId) {
        return assignedConditionRepository.findByOrderId(orderId);
    }
}
