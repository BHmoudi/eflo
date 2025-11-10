package com.eflo.order.domain.model.dto;

import com.eflo.order.domain.entity.OrderDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliveryDTO {
    private Long id;
    private Long orderId;
    private OrderDelivery.DeliveryType deliveryType;
    private LocalDate scheduledDate;
    private LocalTime scheduledTimeStart;
    private LocalTime scheduledTimeEnd;
    private LocalDateTime actualDeliveryDate;
    private String deliveryAddressLine1;
    private String deliveryAddressLine2;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPostalCode;
    private String deliveryCountry;
    private String deliveryContactName;
    private String deliveryContactPhone;
    private String deliveryContactEmail;
    private String deliveryInstructions;
    private OrderDelivery.DeliveryStatus deliveryStatus;
    private Long deliveredByUserId;
    private String deliveryNotes;
    private Boolean signatureCaptured;
    private String signatureData;
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private LocalDateTime updatedAt;
}

