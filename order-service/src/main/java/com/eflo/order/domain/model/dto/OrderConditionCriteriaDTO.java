package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderConditionCriteriaDTO {
    private Long id;
    private Long ruleId;
    private String fieldPath;
    private String operator;
    private Object value;
    private Integer criteriaGroup;
    private Integer sequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
