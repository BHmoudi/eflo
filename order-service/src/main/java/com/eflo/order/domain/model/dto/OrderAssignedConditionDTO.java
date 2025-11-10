package com.eflo.order.domain.model.dto;

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
public class OrderAssignedConditionDTO {
    private Long id;
    private Long orderId;
    private Long conditionId;
    private String conditionCode;
    private String conditionLabel;
    private String conditionColor;
    private String conditionIcon;
    private Long ruleId;
    private String ruleName;
    private LocalDateTime assignedAt;
    private String assignedBy;
    private Boolean isManual;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
