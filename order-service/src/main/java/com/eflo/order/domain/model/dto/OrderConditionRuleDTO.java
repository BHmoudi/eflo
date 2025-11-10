package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderConditionRuleDTO {
    private Long id;
    private Long conditionId;
    private String name;
    private String description;
    private String logicalOperator; // AND or OR
    private Integer priority;
    private Boolean isActive;
    private List<OrderConditionCriteriaDTO> criteria;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
