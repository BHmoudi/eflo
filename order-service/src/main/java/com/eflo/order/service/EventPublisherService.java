package com.eflo.order.service;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.model.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC_PREFIX = "orders";

    public void publishOrderCreated(Order order, Long userId) {
        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_CREATED.name(),
                order,
                userId,
                Map.of("orderCreated", true)
        );
        publishEvent(TOPIC_PREFIX + ".created", event);
    }

    public void publishOrderUpdated(Order order, Long userId) {
        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_UPDATED.name(),
                order,
                userId,
                Map.of("orderUpdated", true)
        );
        publishEvent(TOPIC_PREFIX + ".updated", event);
    }

    public void publishOrderStatusChanged(Order order, Order.OrderStatus previousStatus,
                                         Order.OrderStatus newStatus, Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("previousStatus", previousStatus.name());
        data.put("newStatus", newStatus.name());

        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_STATUS_CHANGED.name(),
                order,
                userId,
                data
        );
        publishEvent(TOPIC_PREFIX + ".status-changed", event);
    }

    public void publishOrderValidated(Order order, Long userId) {
        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_VALIDATED.name(),
                order,
                userId,
                Map.of("validated", true)
        );
        publishEvent(TOPIC_PREFIX + ".validated", event);
    }

    public void publishOrderDelivered(Order order, Long userId) {
        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_DELIVERED.name(),
                order,
                userId,
                Map.of("delivered", true, "deliveryDate", order.getActualDeliveryDate())
        );
        publishEvent(TOPIC_PREFIX + ".delivered", event);
    }

    public void publishOrderCancelled(Order order, Long userId, String reason) {
        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_CANCELLED.name(),
                order,
                userId,
                Map.of("cancelled", true, "reason", reason)
        );
        publishEvent(TOPIC_PREFIX + ".cancelled", event);
    }

    public void publishOrderPriceChanged(Order order, Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalAmount", order.getTotalAmount());
        data.put("marginPercentage", order.getMarginPercentage());

        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_PRICE_CHANGED.name(),
                order,
                userId,
                data
        );
        publishEvent(TOPIC_PREFIX + ".price-changed", event);
    }

    public void publishOrderDataChanged(Order order, Long userId, String changeType) {
        OrderEvent event = buildEvent(
                OrderEvent.EventType.ORDER_DATA_CHANGED.name(),
                order,
                userId,
                Map.of("changeType", changeType)
        );
        publishEvent(TOPIC_PREFIX + ".data-changed", event);
    }

    private OrderEvent buildEvent(String eventType, Order order, Long userId, Map<String, Object> eventData) {
        return OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .customerId(order.getCustomerId())
                .salespersonId(order.getSalespersonId())
                .businessUnitId(order.getBusinessUnitId())
                .eventData(eventData)
                .triggeredByUserId(userId)
                .source("order-service")
                .build();
    }

    private void publishEvent(String topic, OrderEvent event) {
        try {
            kafkaTemplate.send(topic, event.getOrderNumber(), event);
            log.info("Published event {} to topic {}: {}", event.getEventType(), topic, event.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish event {} to topic {}: {}", event.getEventType(), topic, e.getMessage(), e);
        }
    }
}
