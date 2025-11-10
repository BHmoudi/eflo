package com.eflo.order.domain.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRuleRequest {
    @NotNull(message = "Condition ID is required")
    private Long conditionId;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    @NotBlank(message = "Logical operator is required")
    private String logicalOperator; // AND or OR

    private Integer priority;
    private Boolean isActive;

    @Valid
    @NotNull(message = "Criteria are required")
    private List<CriteriaRequest> criteria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CriteriaRequest {
        @NotBlank(message = "Field path is required")
        private String fieldPath;

        @NotBlank(message = "Operator is required")
        private String operator;

        private Object value;
        private Integer criteriaGroup;
        private Integer sequence;
    }
}
