package com.eflo.order.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConditionRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Label is required")
    private String label;

    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Integer priority;
    private Boolean isActive;
    private String color;
    private String icon;
}
