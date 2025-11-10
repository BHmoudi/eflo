package com.eflo.order.web;

import com.eflo.order.domain.entity.*;
import com.eflo.order.domain.model.dto.*;
import com.eflo.order.domain.repository.*;
import com.eflo.order.service.ConditionEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/conditions")
@RequiredArgsConstructor
@Tag(name = "Condition Management", description = "APIs for managing order conditions and rules")
public class ConditionController {

    private final ConditionCategoryRepository categoryRepository;
    private final OrderConditionRepository conditionRepository;
    private final OrderConditionRuleRepository ruleRepository;
    private final OrderConditionCriteriaRepository criteriaRepository;
    private final OrderAssignedConditionRepository assignedConditionRepository;
    private final ConditionFieldDefinitionRepository fieldDefinitionRepository;
    private final ConditionEvaluationService evaluationService;
    private final OrderRepository orderRepository;

    // ========== Condition Categories ==========

    @GetMapping("/categories")
    @Operation(summary = "Get all condition categories")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<ConditionCategoryDTO>> getAllCategories() {
        List<ConditionCategoryDTO> categories = categoryRepository.findAll().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<ConditionCategoryDTO> getCategoryById(@PathVariable Long id) {
        ConditionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return ResponseEntity.ok(toCategoryDTO(category));
    }

    // ========== Conditions ==========

    @GetMapping
    @Operation(summary = "Get all conditions")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderConditionDTO>> getAllConditions() {
        List<OrderConditionDTO> conditions = conditionRepository.findAll().stream()
                .map(this::toConditionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conditions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get condition by ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<OrderConditionDTO> getConditionById(@PathVariable Long id) {
        OrderCondition condition = conditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condition not found"));
        return ResponseEntity.ok(toConditionDTO(condition));
    }

    @GetMapping("/category/{categoryCode}")
    @Operation(summary = "Get conditions by category code")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderConditionDTO>> getConditionsByCategory(@PathVariable String categoryCode) {
        List<OrderConditionDTO> conditions = conditionRepository.findActiveByCategoryCode(categoryCode).stream()
                .map(this::toConditionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conditions);
    }

    @PostMapping
    @Operation(summary = "Create a new condition")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'ADMIN')")
    public ResponseEntity<OrderConditionDTO> createCondition(@Valid @RequestBody CreateConditionRequest request) {
        ConditionCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        OrderCondition condition = OrderCondition.builder()
                .code(request.getCode())
                .label(request.getLabel())
                .description(request.getDescription())
                .category(category)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .color(request.getColor())
                .icon(request.getIcon())
                .build();

        OrderCondition saved = conditionRepository.save(condition);
        return ResponseEntity.status(HttpStatus.CREATED).body(toConditionDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a condition")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'ADMIN')")
    public ResponseEntity<OrderConditionDTO> updateCondition(@PathVariable Long id,
                                                             @Valid @RequestBody CreateConditionRequest request) {
        OrderCondition condition = conditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condition not found"));

        if (request.getCategoryId() != null) {
            ConditionCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            condition.setCategory(category);
        }

        condition.setCode(request.getCode());
        condition.setLabel(request.getLabel());
        condition.setDescription(request.getDescription());
        condition.setPriority(request.getPriority() != null ? request.getPriority() : condition.getPriority());
        condition.setIsActive(request.getIsActive() != null ? request.getIsActive() : condition.getIsActive());
        condition.setColor(request.getColor());
        condition.setIcon(request.getIcon());

        OrderCondition updated = conditionRepository.save(condition);
        return ResponseEntity.ok(toConditionDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a condition")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteCondition(@PathVariable Long id) {
        conditionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Rules ==========

    @PostMapping("/rules")
    @Operation(summary = "Create a new rule for a condition")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'ADMIN')")
    public ResponseEntity<OrderConditionRuleDTO> createRule(@Valid @RequestBody CreateRuleRequest request) {
        OrderCondition condition = conditionRepository.findById(request.getConditionId())
                .orElseThrow(() -> new RuntimeException("Condition not found"));

        OrderConditionRule rule = OrderConditionRule.builder()
                .condition(condition)
                .name(request.getName())
                .description(request.getDescription())
                .logicalOperator(OrderConditionRule.LogicalOperator.valueOf(request.getLogicalOperator()))
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        OrderConditionRule savedRule = ruleRepository.save(rule);

        // Create criteria
        if (request.getCriteria() != null && !request.getCriteria().isEmpty()) {
            for (CreateRuleRequest.CriteriaRequest criteriaReq : request.getCriteria()) {
                OrderConditionCriteria criteria = OrderConditionCriteria.builder()
                        .rule(savedRule)
                        .fieldPath(criteriaReq.getFieldPath())
                        .operator(OrderConditionCriteria.CriteriaOperator.valueOf(criteriaReq.getOperator()))
                        .value(criteriaReq.getValue())
                        .criteriaGroup(criteriaReq.getCriteriaGroup() != null ? criteriaReq.getCriteriaGroup() : 1)
                        .sequence(criteriaReq.getSequence() != null ? criteriaReq.getSequence() : 0)
                        .build();
                criteriaRepository.save(criteria);
            }
        }

        // Reload with criteria
        OrderConditionRule reloaded = ruleRepository.findByIdWithCriteria(savedRule.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toRuleDTO(reloaded));
    }

    @GetMapping("/{conditionId}/rules")
    @Operation(summary = "Get all rules for a condition")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderConditionRuleDTO>> getRulesByCondition(@PathVariable Long conditionId) {
        List<OrderConditionRuleDTO> rules = ruleRepository.findActiveByConditionId(conditionId).stream()
                .map(this::toRuleDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    // ========== Order Conditions ==========

    @PostMapping("/orders/{orderId}/evaluate")
    @Operation(summary = "Evaluate and assign conditions to an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<List<OrderAssignedConditionDTO>> evaluateOrderConditions(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderAssignedCondition> assigned = evaluationService.evaluateAndAssignConditions(order);

        List<OrderAssignedConditionDTO> dtos = assigned.stream()
                .map(this::toAssignedConditionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get assigned conditions for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderAssignedConditionDTO>> getOrderConditions(@PathVariable Long orderId) {
        List<OrderAssignedConditionDTO> conditions = assignedConditionRepository.findByOrderId(orderId).stream()
                .map(this::toAssignedConditionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conditions);
    }

    @PostMapping("/orders/{orderId}/assign/{conditionId}")
    @Operation(summary = "Manually assign a condition to an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderAssignedConditionDTO> manuallyAssignCondition(
            @PathVariable Long orderId,
            @PathVariable Long conditionId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new RuntimeException("Condition not found"));

        OrderAssignedCondition assigned = evaluationService.assignCondition(order, condition, null, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAssignedConditionDTO(assigned));
    }

    @DeleteMapping("/orders/{orderId}/conditions/{conditionId}")
    @Operation(summary = "Remove a condition from an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<Void> removeConditionFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long conditionId) {

        OrderAssignedCondition assigned = assignedConditionRepository
                .findByOrderIdAndConditionId(orderId, conditionId)
                .orElseThrow(() -> new RuntimeException("Assigned condition not found"));

        assignedConditionRepository.delete(assigned);
        return ResponseEntity.noContent().build();
    }

    // ========== Field Definitions ==========

    @GetMapping("/fields")
    @Operation(summary = "Get all field definitions for rule building")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<ConditionFieldDefinitionDTO>> getAllFieldDefinitions() {
        List<ConditionFieldDefinitionDTO> fields = fieldDefinitionRepository.findAllActive().stream()
                .map(this::toFieldDefinitionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fields);
    }

    @GetMapping("/fields/entity/{entity}")
    @Operation(summary = "Get field definitions by entity")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<ConditionFieldDefinitionDTO>> getFieldDefinitionsByEntity(@PathVariable String entity) {
        List<ConditionFieldDefinitionDTO> fields = fieldDefinitionRepository.findActiveByEntity(entity).stream()
                .map(this::toFieldDefinitionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fields);
    }

    // ========== DTO Converters ==========

    private ConditionCategoryDTO toCategoryDTO(ConditionCategory category) {
        return ConditionCategoryDTO.builder()
                .id(category.getId())
                .code(category.getCode())
                .label(category.getLabel())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private OrderConditionDTO toConditionDTO(OrderCondition condition) {
        return OrderConditionDTO.builder()
                .id(condition.getId())
                .code(condition.getCode())
                .label(condition.getLabel())
                .description(condition.getDescription())
                .categoryId(condition.getCategory() != null ? condition.getCategory().getId() : null)
                .categoryCode(condition.getCategory() != null ? condition.getCategory().getCode() : null)
                .categoryLabel(condition.getCategory() != null ? condition.getCategory().getLabel() : null)
                .priority(condition.getPriority())
                .isActive(condition.getIsActive())
                .color(condition.getColor())
                .icon(condition.getIcon())
                .createdAt(condition.getCreatedAt())
                .updatedAt(condition.getUpdatedAt())
                .build();
    }

    private OrderConditionRuleDTO toRuleDTO(OrderConditionRule rule) {
        return OrderConditionRuleDTO.builder()
                .id(rule.getId())
                .conditionId(rule.getCondition().getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .logicalOperator(rule.getLogicalOperator().name())
                .priority(rule.getPriority())
                .isActive(rule.getIsActive())
                .criteria(rule.getCriteria().stream().map(this::toCriteriaDTO).collect(Collectors.toList()))
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private OrderConditionCriteriaDTO toCriteriaDTO(OrderConditionCriteria criteria) {
        return OrderConditionCriteriaDTO.builder()
                .id(criteria.getId())
                .ruleId(criteria.getRule().getId())
                .fieldPath(criteria.getFieldPath())
                .operator(criteria.getOperator().name())
                .value(criteria.getValue())
                .criteriaGroup(criteria.getCriteriaGroup())
                .sequence(criteria.getSequence())
                .createdAt(criteria.getCreatedAt())
                .updatedAt(criteria.getUpdatedAt())
                .build();
    }

    private OrderAssignedConditionDTO toAssignedConditionDTO(OrderAssignedCondition assigned) {
        return OrderAssignedConditionDTO.builder()
                .id(assigned.getId())
                .orderId(assigned.getOrder().getId())
                .conditionId(assigned.getCondition().getId())
                .conditionCode(assigned.getCondition().getCode())
                .conditionLabel(assigned.getCondition().getLabel())
                .conditionColor(assigned.getCondition().getColor())
                .conditionIcon(assigned.getCondition().getIcon())
                .ruleId(assigned.getRule() != null ? assigned.getRule().getId() : null)
                .ruleName(assigned.getRule() != null ? assigned.getRule().getName() : null)
                .assignedAt(assigned.getAssignedAt())
                .assignedBy(assigned.getAssignedBy())
                .isManual(assigned.getIsManual())
                .metadata(assigned.getMetadata())
                .createdAt(assigned.getCreatedAt())
                .updatedAt(assigned.getUpdatedAt())
                .build();
    }

    private ConditionFieldDefinitionDTO toFieldDefinitionDTO(ConditionFieldDefinition field) {
        return ConditionFieldDefinitionDTO.builder()
                .id(field.getId())
                .fieldPath(field.getFieldPath())
                .fieldLabel(field.getFieldLabel())
                .fieldType(field.getFieldType().name())
                .entity(field.getEntity())
                .availableOperators(field.getAvailableOperators())
                .valueSource(field.getValueSource().name())
                .lookupTable(field.getLookupTable())
                .lookupField(field.getLookupField())
                .description(field.getDescription())
                .isActive(field.getIsActive())
                .createdAt(field.getCreatedAt())
                .updatedAt(field.getUpdatedAt())
                .build();
    }
}
