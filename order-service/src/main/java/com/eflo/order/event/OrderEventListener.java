package com.eflo.order.event;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.model.event.OrderEvent;
import com.eflo.order.service.ConditionEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka event listener for order events
 * Handles automatic condition assignment when orders are created/updated
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ConditionEvaluationService conditionEvaluationService;

    /**
     * Listen to order.created events and automatically evaluate/assign conditions
     */
    @KafkaListener(topics = "orders.created", groupId = "order-condition-service")
    public void handleOrderCreated(OrderEvent event) {
        log.info("Received order.created event for order ID: {}", event.getOrderId());

        try {
            // Automatically evaluate and assign conditions
            conditionEvaluationService.evaluateAndAssignConditions(event.getOrderId());
            log.info("Successfully auto-assigned conditions for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to auto-assign conditions for order {}: {}",
                     event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to order.updated events and re-evaluate conditions
     */
    @KafkaListener(topics = "orders.updated", groupId = "order-condition-service")
    public void handleOrderUpdated(OrderEvent event) {
        log.info("Received order.updated event for order ID: {}", event.getOrderId());

        try {
            // Re-evaluate conditions on order update
            conditionEvaluationService.evaluateAndAssignConditions(event.getOrderId());
            log.info("Successfully re-evaluated conditions for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to re-evaluate conditions for order {}: {}",
                     event.getOrderId(), e.getMessage(), e);
        }
    }
}
