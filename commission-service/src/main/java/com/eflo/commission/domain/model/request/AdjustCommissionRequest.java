package com.eflo.commission.domain.model.request;

import jakarta.validation.constraints.NotBlank;
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
public class AdjustCommissionRequest {

    @NotNull(message = "Adjustment amount is required")
    private BigDecimal adjustmentAmount;

    @NotBlank(message = "Adjustment reason is required")
    private String adjustmentReason;

    private String notes;
}
