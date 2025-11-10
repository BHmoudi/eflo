package com.eflo.commission.domain.model.request;

import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommissionScaleRequest {

    @Size(max = 255, message = "Scale name must not exceed 255 characters")
    private String scaleName;

    private String scaleDescription;

    private CommissionType commissionType;
    private CalculationMethod calculationMethod;

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate must be positive")
    @DecimalMax(value = "100.0", message = "Commission rate must not exceed 100%")
    private BigDecimal commissionRatePercentage;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fixed amount must be positive")
    private BigDecimal fixedAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum margin must be positive")
    private BigDecimal minimumMarginRequired;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum revenue must be positive")
    private BigDecimal minimumRevenueRequired;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum commission must be positive")
    private BigDecimal maximumCommissionAmount;

    private Boolean managerSplitEnabled;

    @DecimalMin(value = "0.0", inclusive = true, message = "Manager split percentage must be positive")
    @DecimalMax(value = "100.0", message = "Manager split percentage must not exceed 100%")
    private BigDecimal managerSplitPercentage;

    private Map<String, Object> configurationJson;

    private LocalDate validFrom;
    private LocalDate validTo;

    private Boolean isActive;
    private Boolean isDefault;

    @Valid
    private List<CommissionScaleTierRequest> tiers;
}
