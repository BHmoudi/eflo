package com.eflo.order.domain.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyDiscountRequest {

    @DecimalMin(value = "0.0", message = "Discount amount must be positive")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", message = "Discount percentage must be positive")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    private BigDecimal discountPercentage;

    private String reason;
}
