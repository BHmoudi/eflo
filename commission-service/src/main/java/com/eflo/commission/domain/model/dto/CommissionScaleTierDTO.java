package com.eflo.commission.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionScaleTierDTO {

    private Long id;
    private Integer tierOrder;
    private String tierName;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private BigDecimal commissionRate;
    private BigDecimal fixedAmount;

    private String description;
}
