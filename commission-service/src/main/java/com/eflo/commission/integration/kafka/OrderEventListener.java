package com.eflo.commission.integration.kafka;

import com.eflo.commission.domain.model.event.OrderCompletedEvent;
import com.eflo.commission.domain.model.request.CalculateCommissionRequest;
import com.eflo.commission.service.CommissionCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final CommissionCalculationService commissionCalculationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.order-completed:order.completed}",
            groupId = "${spring.kafka.consumer.group-id:commission-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCompletedEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("Received order completed event from topic: {}", topic);

        try {
            // Parse the event
            OrderCompletedEvent event = objectMapper.readValue(payload, OrderCompletedEvent.class);

            log.info("Processing order completed event for order: {} (ID: {})",
                    event.getOrderNumber(), event.getOrderId());

            // Create commission calculation request
            CalculateCommissionRequest request = CalculateCommissionRequest.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .orderType(event.getOrderType())
                    .businessUnitId(event.getBusinessUnitId())
                    .businessUnitName(event.getBusinessUnitName())
                    .salespersonId(event.getSalespersonId())
                    .salespersonName(event.getSalespersonName())
                    .managerId(event.getManagerId())
                    .managerName(event.getManagerName())
                    .orderTotalRevenue(event.getOrderTotalRevenue())
                    .orderNetMargin(event.getOrderNetMargin())
                    .vehicleMargin(event.getVehicleMargin() != null
                            ? event.getVehicleMargin() : BigDecimal.ZERO)
                    .accessoryMargin(event.getAccessoryMargin() != null
                            ? event.getAccessoryMargin() : BigDecimal.ZERO)
                    .serviceMargin(event.getServiceMargin() != null
                            ? event.getServiceMargin() : BigDecimal.ZERO)
                    .additionalData(event.getMetadata())
                    .build();

            // Calculate commission
            commissionCalculationService.calculateCommission(request);

            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            log.info("Successfully processed order completed event for order: {}",
                    event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing order completed event", e);
            // In production, you might want to implement retry logic or dead letter queue
            // For now, we'll acknowledge to prevent infinite retries
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        }
    }
}
