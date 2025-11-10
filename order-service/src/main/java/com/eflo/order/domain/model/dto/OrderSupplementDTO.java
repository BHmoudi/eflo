package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSupplementDTO {
    private Long id;
    private Long orderId;
    private String supplementCode;
    private String supplementName;
    private String supplementType;
    private String description;
    private BigDecimal amount;
    private String reason;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
}
