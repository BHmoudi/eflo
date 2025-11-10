package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAccessoryDTO {
    private Long id;
    private Long orderId;
    private String accessoryCode;
    private String accessoryName;
    private String accessoryCategory;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal costPerUnit;
    private BigDecimal totalCost;
    private String supplier;
    private Boolean installationRequired;
}
