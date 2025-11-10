package com.eflo.order.service;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.entity.OrderDelivery;
import com.eflo.order.domain.repository.OrderDeliveryRepository;
import com.eflo.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final OrderDeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final EventPublisherService eventPublisherService;

    /**
     * Schedule a delivery for an order
     */
    @Transactional
    public OrderDelivery scheduleDelivery(Long orderId, OrderDelivery.DeliveryType deliveryType,
                                         LocalDate scheduledDate, LocalTime timeStart, LocalTime timeEnd,
                                         String address, String city, String state, String postalCode,
                                         String country, String contactName, String contactPhone,
                                         String contactEmail, String instructions, Long userId) {
        log.info("Scheduling delivery for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderDelivery delivery = OrderDelivery.builder()
                .order(order)
                .deliveryType(deliveryType)
                .scheduledDate(scheduledDate)
                .scheduledTimeStart(timeStart)
                .scheduledTimeEnd(timeEnd)
                .deliveryAddressLine1(address)
                .deliveryCity(city)
                .deliveryState(state)
                .deliveryPostalCode(postalCode)
                .deliveryCountry(country)
                .deliveryContactName(contactName)
                .deliveryContactPhone(contactPhone)
                .deliveryContactEmail(contactEmail)
                .deliveryInstructions(instructions)
                .deliveryStatus(OrderDelivery.DeliveryStatus.SCHEDULED)
                .createdByUserId(userId)
                .build();

        delivery = deliveryRepository.save(delivery);

        // Update order expected delivery date
        order.setExpectedDeliveryDate(scheduledDate);
        orderRepository.save(order);

        log.info("Delivery scheduled successfully for order: {}", order.getOrderNumber());
        return delivery;
    }

    /**
     * Update delivery details
     */
    @Transactional
    public OrderDelivery updateDelivery(Long deliveryId, LocalDate scheduledDate,
                                       LocalTime timeStart, LocalTime timeEnd,
                                       String instructions, Long userId) {
        log.info("Updating delivery: {}", deliveryId);

        OrderDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        if (scheduledDate != null) {
            delivery.setScheduledDate(scheduledDate);
            delivery.getOrder().setExpectedDeliveryDate(scheduledDate);
        }
        if (timeStart != null) {
            delivery.setScheduledTimeStart(timeStart);
        }
        if (timeEnd != null) {
            delivery.setScheduledTimeEnd(timeEnd);
        }
        if (instructions != null) {
            delivery.setDeliveryInstructions(instructions);
        }

        delivery = deliveryRepository.save(delivery);

        log.info("Delivery {} updated successfully", deliveryId);
        return delivery;
    }

    /**
     * Complete a delivery
     */
    @Transactional
    public OrderDelivery completeDelivery(Long deliveryId, Long userId, String notes,
                                         boolean signatureCaptured, String signatureData) {
        log.info("Completing delivery: {}", deliveryId);

        OrderDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setDeliveryStatus(OrderDelivery.DeliveryStatus.DELIVERED);
        delivery.setActualDeliveryDate(LocalDateTime.now());
        delivery.setDeliveredByUserId(userId);
        delivery.setDeliveryNotes(notes);
        delivery.setSignatureCaptured(signatureCaptured);
        delivery.setSignatureData(signatureData);

        delivery = deliveryRepository.save(delivery);

        // Update order status and actual delivery date
        Order order = delivery.getOrder();
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setActualDeliveryDate(LocalDate.now());
        orderRepository.save(order);

        // Publish event
        eventPublisherService.publishOrderDelivered(order, userId);

        log.info("Delivery completed for order: {}", order.getOrderNumber());
        return delivery;
    }

    /**
     * Cancel a delivery
     */
    @Transactional
    public OrderDelivery cancelDelivery(Long deliveryId, String reason, Long userId) {
        log.info("Cancelling delivery: {}", deliveryId);

        OrderDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setDeliveryStatus(OrderDelivery.DeliveryStatus.CANCELLED);
        delivery.setDeliveryNotes(reason);

        delivery = deliveryRepository.save(delivery);

        log.info("Delivery {} cancelled", deliveryId);
        return delivery;
    }

    /**
     * Get deliveries for an order
     */
    @Transactional(readOnly = true)
    public List<OrderDelivery> getDeliveriesForOrder(Long orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }

    /**
     * Get deliveries for a specific date
     */
    @Transactional(readOnly = true)
    public List<OrderDelivery> getDeliveriesByDate(LocalDate date) {
        return deliveryRepository.findByScheduledDate(date);
    }

    /**
     * Get deliveries by status
     */
    @Transactional(readOnly = true)
    public List<OrderDelivery> getDeliveriesByStatus(OrderDelivery.DeliveryStatus status) {
        return deliveryRepository.findByDeliveryStatus(status);
    }
}
