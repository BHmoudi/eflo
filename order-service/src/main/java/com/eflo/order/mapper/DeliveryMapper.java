package com.eflo.order.mapper;

import com.eflo.order.domain.entity.OrderDelivery;
import com.eflo.order.domain.model.dto.OrderDeliveryDTO;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {
    public OrderDeliveryDTO toDTO(OrderDelivery delivery) {
        if (delivery == null) return null;
        return OrderDeliveryDTO.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder() != null ? delivery.getOrder().getId() : null)
                .deliveryType(delivery.getDeliveryType())
                .scheduledDate(delivery.getScheduledDate())
                .scheduledTimeStart(delivery.getScheduledTimeStart())
                .scheduledTimeEnd(delivery.getScheduledTimeEnd())
                .actualDeliveryDate(delivery.getActualDeliveryDate())
                .deliveryAddressLine1(delivery.getDeliveryAddressLine1())
                .deliveryAddressLine2(delivery.getDeliveryAddressLine2())
                .deliveryCity(delivery.getDeliveryCity())
                .deliveryState(delivery.getDeliveryState())
                .deliveryPostalCode(delivery.getDeliveryPostalCode())
                .deliveryCountry(delivery.getDeliveryCountry())
                .deliveryContactName(delivery.getDeliveryContactName())
                .deliveryContactPhone(delivery.getDeliveryContactPhone())
                .deliveryContactEmail(delivery.getDeliveryContactEmail())
                .deliveryInstructions(delivery.getDeliveryInstructions())
                .deliveryStatus(delivery.getDeliveryStatus())
                .deliveredByUserId(delivery.getDeliveredByUserId())
                .deliveryNotes(delivery.getDeliveryNotes())
                .signatureCaptured(delivery.getSignatureCaptured())
                .signatureData(delivery.getSignatureData())
                .createdAt(delivery.getCreatedAt())
                .createdByUserId(delivery.getCreatedByUserId())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}

