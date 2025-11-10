package com.eflo.commission.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of commission calculation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionCalculationResult {

    // Commission amounts
    private BigDecimal vehicleCommissionExclTax;
    private BigDecimal accessoryCommissionExclTax;
    private BigDecimal serviceCommissionExclTax;
    private BigDecimal totalCommissionExclTax;

    // Commission amounts with tax
    private BigDecimal vehicleCommissionBase;
    private BigDecimal vehicleCommissionRate;
    private BigDecimal vehicleCommissionTax;
    private BigDecimal vehicleCommissionInclTax;
    private BigDecimal accessoryCommissionTax;
    private BigDecimal accessoryCommissionInclTax;
    private BigDecimal serviceCommissionTax;
    private BigDecimal serviceCommissionInclTax;
    private BigDecimal totalCommissionTax;
    private BigDecimal totalCommissionInclTax;

    // Calculation details
    private BigDecimal calculationBase;
    private BigDecimal commissionRate;
    private String calculationMethod;

    // Tier breakdown (for tiered calculations)
    @Builder.Default
    private List<TierCalculationDetail> tierDetails = new ArrayList<>();

    // Additional calculation metadata
    @Builder.Default
    private Map<String, Object> calculationDetails = new HashMap<>();

    // Validation
    private boolean valid;
    private String validationMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TierCalculationDetail {
        private Integer tierOrder;
        private String tierDescription;
        private BigDecimal thresholdMin;
        private BigDecimal thresholdMax;
        private BigDecimal tierRate;
        private BigDecimal amountInTier;
        private BigDecimal tierCommission;
    }

    public void addTierDetail(TierCalculationDetail detail) {
        this.tierDetails.add(detail);
    }

    public void addCalculationDetail(String key, Object value) {
        this.calculationDetails.put(key, value);
    }

    public List<TierCalculationDetail> getTierBreakdown() {
        return this.tierDetails;
    }
}
