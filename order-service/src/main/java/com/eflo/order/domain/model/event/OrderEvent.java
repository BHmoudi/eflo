package com.eflo.order.domain.model.event;

import com.eflo.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Long orderId;
    private String orderNumber;
    private Order.OrderType orderType;
    private Order.OrderStatus status;
    private Long customerId;
    private Long salespersonId;
    private Long businessUnitId;
    private Map<String, Object> eventData;
    private Long triggeredByUserId;
    private String source;

    public enum EventType {
        ORDER_CREATED,
        ORDER_UPDATED,
        ORDER_STATUS_CHANGED,
        ORDER_VALIDATED,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        ORDER_PRICE_CHANGED,
        ORDER_DATA_CHANGED
    }
}
