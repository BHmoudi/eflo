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
public class ConditionFieldDefinitionDTO {
    private Long id;
    private String fieldPath;
    private String fieldLabel;
    private String fieldType;
    private String entity;
    private List<String> availableOperators;
    private String valueSource;
    private String lookupTable;
    private String lookupField;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
