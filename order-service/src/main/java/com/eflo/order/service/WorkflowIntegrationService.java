package com.eflo.order.service;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.model.dto.WorkflowInstanceDTO;
import com.eflo.order.domain.model.request.StartInstanceRequest;
import com.eflo.order.domain.model.request.TransitionRequest;
import com.eflo.order.domain.repository.OrderRepository;
import com.eflo.order.integration.WorkflowServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowIntegrationService {

    private final WorkflowServiceClient workflowServiceClient;
    private final OrderRepository orderRepository;

    /**
     * Auto-start workflow when order created
     * Maps order types to workflow codes:
     * - VN → VN_COMPLETE
     * - VO → VO_STANDARD
     * - EVO → EVO_STANDARD
     */
    @Transactional
    public void initializeWorkflowForOrder(Order order) {
        log.info("=== Starting workflow initialization for order: {} (ID: {}, Type: {}) ===",
                order.getOrderNumber(), order.getId(), order.getOrderType());

        try {
            // Map order type to workflow code
            String workflowCode = mapOrderTypeToWorkflowCode(order.getOrderType());
            log.info("Step 1: Mapped order type {} to workflow code: {}", order.getOrderType(), workflowCode);

            // Get process by code to obtain processId
            log.info("Step 2: Fetching workflow process by code: {}", workflowCode);
            Map<String, Object> process = workflowServiceClient.getProcessByCode(workflowCode);

            if (process == null || !process.containsKey("id")) {
                log.error("Step 2 FAILED: Process response is null or missing 'id' field. Response: {}", process);
                throw new RuntimeException("Invalid process response from workflow service");
            }

            Long processId = ((Number) process.get("id")).longValue();
            log.info("Step 2 SUCCESS: Found workflow process {} with ID: {}", workflowCode, processId);

            // Prepare context data with order details
            Map<String, Object> contextData = buildOrderMetadata(order);
            log.info("Step 3: Built order metadata with {} fields", contextData.size());

            // Create start instance request with correct fields
            StartInstanceRequest request = StartInstanceRequest.builder()
                    .processId(processId)
                    .orderId(order.getId())
                    .instanceName("Order " + order.getOrderNumber())
                    .priority("NORMAL")
                    .contextData(contextData)
                    .build();

            log.info("Step 4: Created StartInstanceRequest - ProcessId: {}, OrderId: {}, Name: {}",
                    request.getProcessId(), request.getOrderId(), request.getInstanceName());

            // Step 5a: Create workflow instance
            log.info("Step 5a: Creating workflow instance...");
            WorkflowInstanceDTO workflowInstance = workflowServiceClient.createInstance(request);

            if (workflowInstance == null || workflowInstance.getId() == null) {
                log.error("Step 5a FAILED: Workflow instance response is null or missing ID. Response: {}", workflowInstance);
                throw new RuntimeException("Invalid workflow instance response from workflow service");
            }

            log.info("Step 5a SUCCESS: Workflow instance created - ID: {}", workflowInstance.getId());

            // Step 5b: Start the workflow instance (initializes state and creates tasks)
            log.info("Step 5b: Starting workflow instance {}...", workflowInstance.getId());
            workflowInstance = workflowServiceClient.startInstance(workflowInstance.getId());

            log.info("Step 5b SUCCESS: Workflow instance started - State: {}", workflowInstance.getCurrentState());

            // Store workflow instance ID and current state in order
            log.info("Step 6: Updating order with workflow instance ID and state...");
            order.setWorkflowInstanceId(workflowInstance.getId());
            order.setWorkflowCurrentState(workflowInstance.getCurrentState());
            Order savedOrder = orderRepository.save(order);

            log.info("Step 6 SUCCESS: Order updated - WorkflowInstanceId: {}, WorkflowCurrentState: {}",
                    savedOrder.getWorkflowInstanceId(), savedOrder.getWorkflowCurrentState());

            log.info("=== Workflow initialization completed successfully for order {} ===",
                    order.getOrderNumber());

        } catch (feign.FeignException e) {
            log.error("=== Workflow initialization FAILED - Feign/HTTP error for order {} ===",
                    order.getOrderNumber());
            log.error("HTTP Status: {}, Reason: {}", e.status(), e.getMessage());
            log.error("Response body: {}", e.contentUTF8());
            log.error("Full exception:", e);
            // Don't fail the order creation if workflow initialization fails
            // This makes the system more resilient
        } catch (Exception e) {
            log.error("=== Workflow initialization FAILED - Unexpected error for order {} ===",
                    order.getOrderNumber());
            log.error("Error type: {}, Message: {}", e.getClass().getName(), e.getMessage());
            log.error("Full exception:", e);
            // Don't fail the order creation if workflow initialization fails
            // This makes the system more resilient
        }
    }

    /**
     * Sync order status with workflow state
     */
    @Transactional
    public void syncOrderWithWorkflowState(Long orderId, String workflowState) {
        try {
            log.info("Syncing order {} with workflow state: {}", orderId, workflowState);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            // Update workflow current state
            order.setWorkflowCurrentState(workflowState);

            // Optionally map workflow state to order status
            // This is a simple mapping - you might want more sophisticated logic
            Order.OrderStatus newStatus = mapWorkflowStateToOrderStatus(workflowState);
            if (newStatus != null && newStatus != order.getStatus()) {
                log.info("Updating order status from {} to {} based on workflow state",
                        order.getStatus(), newStatus);
                order.setStatus(newStatus);
            }

            orderRepository.save(order);

            log.info("Order {} synced with workflow state: {}", orderId, workflowState);

        } catch (Exception e) {
            log.error("Failed to sync order {} with workflow state: {}",
                    orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync order with workflow state", e);
        }
    }

    /**
     * Execute a workflow transition for an order
     */
    @Transactional
    public WorkflowInstanceDTO executeWorkflowTransition(Long orderId, TransitionRequest request) {
        try {
            log.info("Executing workflow transition for order {}: {}", orderId, request.getTransitionCode());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            if (order.getWorkflowInstanceId() == null) {
                throw new RuntimeException("Order has no associated workflow instance");
            }

            // Execute transition on workflow service
            WorkflowInstanceDTO workflowInstance = workflowServiceClient.executeTransition(
                    order.getWorkflowInstanceId(), request);

            // Update order's workflow state
            order.setWorkflowCurrentState(workflowInstance.getCurrentState());
            orderRepository.save(order);

            log.info("Workflow transition executed successfully for order {} - New state: {}",
                    order.getOrderNumber(), workflowInstance.getCurrentState());

            return workflowInstance;

        } catch (Exception e) {
            log.error("Failed to execute workflow transition for order {}: {}",
                    orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to execute workflow transition", e);
        }
    }

    /**
     * Complete workflow when order delivered
     */
    @Transactional
    public void completeWorkflowForOrder(Long orderId) {
        try {
            log.info("Completing workflow for order: {}", orderId);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            if (order.getWorkflowInstanceId() == null) {
                log.warn("Order {} has no associated workflow instance", orderId);
                return;
            }

            // Execute completion transition
            TransitionRequest request = TransitionRequest.builder()
                    .transitionCode("COMPLETE")
                    .metadata(new HashMap<>())
                    .build();

            WorkflowInstanceDTO workflowInstance = workflowServiceClient.executeTransition(
                    order.getWorkflowInstanceId(), request);

            // Update order's workflow state
            order.setWorkflowCurrentState(workflowInstance.getCurrentState());
            orderRepository.save(order);

            log.info("Workflow completed for order {} - Final state: {}",
                    order.getOrderNumber(), workflowInstance.getCurrentState());

        } catch (Exception e) {
            log.error("Failed to complete workflow for order {}: {}",
                    orderId, e.getMessage(), e);
            // Don't fail the order completion if workflow fails
        }
    }

    /**
     * Get workflow instance for an order
     */
    public WorkflowInstanceDTO getWorkflowForOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            if (order.getWorkflowInstanceId() == null) {
                throw new RuntimeException("Order has no associated workflow instance");
            }

            return workflowServiceClient.getInstance(order.getWorkflowInstanceId());

        } catch (Exception e) {
            log.error("Failed to get workflow for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to get workflow instance", e);
        }
    }

    /**
     * Map order type to workflow code
     */
    private String mapOrderTypeToWorkflowCode(Order.OrderType orderType) {
        return switch (orderType) {
            case VN -> "VN_COMPLETE";
            case VO -> "VO_STANDARD";
            case EVO -> "EVO_STANDARD";
        };
    }

    /**
     * Build metadata from order for workflow
     */
    private Map<String, Object> buildOrderMetadata(Order order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderNumber", order.getOrderNumber());
        metadata.put("orderType", order.getOrderType().name());
        metadata.put("customerId", order.getCustomerId());
        metadata.put("businessUnitId", order.getBusinessUnitId());
        metadata.put("salespersonId", order.getSalespersonId());
        metadata.put("vehicleId", order.getVehicleId());
        metadata.put("vin", order.getVin());
        metadata.put("make", order.getMake());
        metadata.put("model", order.getModel());
        metadata.put("year", order.getYear());
        metadata.put("totalAmount", order.getTotalAmount());
        metadata.put("status", order.getStatus().name());
        return metadata;
    }

    /**
     * Map workflow state to order status
     * This is a simple mapping - customize based on your workflow states
     */
    private Order.OrderStatus mapWorkflowStateToOrderStatus(String workflowState) {
        if (workflowState == null) {
            return null;
        }

        return switch (workflowState.toUpperCase()) {
            case "DRAFT", "CREATED" -> Order.OrderStatus.DRAFT;
            case "PENDING_APPROVAL", "AWAITING_VALIDATION" -> Order.OrderStatus.PENDING;
            case "APPROVED", "CONFIRMED" -> Order.OrderStatus.CONFIRMED;
            case "IN_PRODUCTION", "MANUFACTURING" -> Order.OrderStatus.IN_PRODUCTION;
            case "READY", "READY_FOR_DELIVERY" -> Order.OrderStatus.READY_FOR_DELIVERY;
            case "DELIVERED", "COMPLETED" -> Order.OrderStatus.DELIVERED;
            case "INVOICED" -> Order.OrderStatus.INVOICED;
            case "CANCELLED", "REJECTED" -> Order.OrderStatus.CANCELLED;
            case "ON_HOLD", "PAUSED" -> Order.OrderStatus.ON_HOLD;
            default -> null; // Don't change status if state is unknown
        };
    }
}
