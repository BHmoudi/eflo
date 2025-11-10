package com.eflo.order.domain.model.dto;

import com.eflo.order.domain.entity.OrderAid;
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
public class OrderAidDTO {
    private Long id;
    private Long orderId;
    private String aidCode;
    private String aidName;
    private String aidType;
    private String description;
    private BigDecimal amount;
    private String provider;
    private String eligibilityCriteria;
    private OrderAid.ApprovalStatus approvalStatus;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
}
