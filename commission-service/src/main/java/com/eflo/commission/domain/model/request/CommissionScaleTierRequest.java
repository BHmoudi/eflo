package com.eflo.commission.domain.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionScaleTierRequest {

    @NotNull(message = "Tier order is required")
    private Integer tierOrder;

    private String tierName;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum amount must be positive")
    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate must be positive")
    private BigDecimal commissionRate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fixed amount must be positive")
    private BigDecimal fixedAmount;

    private String description;
}
