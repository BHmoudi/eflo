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
public class OrderConditionDTO {
    private Long id;
    private String code;
    private String label;
    private String description;
    private Long categoryId;
    private String categoryCode;
    private String categoryLabel;
    private Integer priority;
    private Boolean isActive;
    private String color;
    private String icon;
    private List<OrderConditionRuleDTO> rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
