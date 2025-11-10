package com.eflo.workflow.kafka;

import com.eflo.workflow.domain.entity.WorkflowInstance;
import com.eflo.workflow.service.InstanceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Order Event Listener
 *
 * Consumes order-related events from Kafka to trigger workflow actions.
 * Automatically creates workflow instances when orders are created.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final InstanceManagementService instanceManagementService;

    /**
     * Handle order events from Kafka
     */
    @KafkaListener(topics = "order.events", groupId = "workflow-service-group")
    public void handleOrderEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("Received order event: {} - {}", eventType, event);

            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(event);
                    break;
                case "ORDER_UPDATED":
                    handleOrderUpdated(event);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(event);
                    break;
                default:
                    log.debug("Unhandled order event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing order event: {}", event, e);
            // Don't throw - let Kafka retry mechanism handle it
        }
    }

    /**
     * Handle ORDER_CREATED event
     * Creates workflow instance for the order based on order type
     */
    private void handleOrderCreated(Map<String, Object> event) {
        try {
            // Extract order data from event
            Long orderId = extractLong(event.get("orderId"));
            String orderType = (String) event.get("orderType");
            String orderNumber = (String) event.get("orderNumber");
            String createdBy = (String) event.get("createdBy");

            if (orderId == null || orderType == null) {
                log.error("Invalid ORDER_CREATED event - missing orderId or orderType: {}", event);
                return;
            }

            log.info("Processing ORDER_CREATED: orderId={}, orderType={}, orderNumber={}",
                    orderId, orderType, orderNumber);

            // Create workflow instance automatically
            WorkflowInstance instance = instanceManagementService.createInstanceFromOrderEvent(
                    orderId,
                    orderType,
                    orderNumber != null ? orderNumber : String.valueOf(orderId),
                    createdBy
            );

            if (instance != null) {
                log.info("Workflow instance created successfully: instanceId={}, orderId={}",
                        instance.getId(), orderId);
            } else {
                log.warn("No workflow instance created for order: orderId={}, orderType={}",
                        orderId, orderType);
            }

        } catch (Exception e) {
            log.error("Failed to process ORDER_CREATED event", e);
            throw e; // Re-throw to trigger Kafka retry
        }
    }

    /**
     * Handle ORDER_UPDATED event
     * Updates workflow context with new order data
     */
    private void handleOrderUpdated(Map<String, Object> event) {
        log.info("Processing ORDER_UPDATED event: {}", event);
        // TODO: Update workflow instance context data when order is updated
        // This can be implemented later based on specific requirements
    }

    /**
     * Handle ORDER_CANCELLED event
     * Cancels associated workflow instances
     */
    private void handleOrderCancelled(Map<String, Object> event) {
        try {
            Long orderId = extractLong(event.get("orderId"));
            String cancelledBy = (String) event.get("cancelledBy");
            String reason = (String) event.get("reason");

            if (orderId == null) {
                log.error("Invalid ORDER_CANCELLED event - missing orderId: {}", event);
                return;
            }

            log.info("Processing ORDER_CANCELLED: orderId={}", orderId);

            // Get instances for this order
            var instances = instanceManagementService.getInstanceByOrderId(orderId);

            // Cancel all non-terminal instances
            for (var instanceResponse : instances) {
                try {
                    instanceManagementService.cancelInstance(
                            instanceResponse.getId(),
                            reason != null ? reason : "Order cancelled",
                            cancelledBy != null ? cancelledBy : "SYSTEM"
                    );
                    log.info("Cancelled workflow instance: instanceId={}", instanceResponse.getId());
                } catch (Exception e) {
                    log.error("Failed to cancel instance: {}", instanceResponse.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process ORDER_CANCELLED event", e);
        }
    }

    /**
     * Helper method to safely extract Long from Object
     */
    private Long extractLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse Long from string: {}", value);
                return null;
            }
        }
        return null;
    }
}
