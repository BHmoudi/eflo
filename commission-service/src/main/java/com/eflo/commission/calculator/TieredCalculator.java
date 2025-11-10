package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.entity.CommissionScaleTier;
import com.eflo.commission.domain.enums.CalculationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for tiered commission structures
 */
@Component
@Slf4j
public class TieredCalculator implements CommissionCalculator {

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationContext context) {
        CommissionScale scale = context.getCommissionScale();

        log.debug("Calculating tiered commission for order {} using scale {}",
                context.getOrderNumber(), scale.getScaleCode());

        BigDecimal calculationBase = context.getCalculationBase();
        List<CommissionScaleTier> tiers = scale.getTiers();

        if (tiers == null || tiers.isEmpty()) {
            log.error("No tiers configured for scale {}", scale.getScaleCode());
            return CommissionCalculationResult.builder()
                    .valid(false)
                    .validationMessage("No tiers configured for this scale")
                    .totalCommissionExclTax(BigDecimal.ZERO)
                    .build();
        }

        CommissionCalculationResult result = CommissionCalculationResult.builder()
                .calculationBase(calculationBase)
                .calculationMethod(scale.getCalculationMethod().name())
                .valid(true)
                .build();

        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal remainingAmount = calculationBase;

        // Calculate commission for each tier
        for (CommissionScaleTier tier : tiers) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal amountInTier = calculateAmountInTier(remainingAmount, tier);
            BigDecimal tierCommission = amountInTier
                    .multiply(tier.getCommissionRatePercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalCommission = totalCommission.add(tierCommission);
            remainingAmount = remainingAmount.subtract(amountInTier);

            // Add tier detail to result
            result.addTierDetail(CommissionCalculationResult.TierCalculationDetail.builder()
                    .tierOrder(tier.getTierOrder())
                    .tierDescription(tier.getTierDescription())
                    .thresholdMin(tier.getThresholdMin())
                    .thresholdMax(tier.getThresholdMax())
                    .tierRate(tier.getCommissionRatePercentage())
                    .amountInTier(amountInTier)
                    .tierCommission(tierCommission)
                    .build());

            log.debug("Tier {}: Amount {} × {}% = {}",
                    tier.getTierOrder(), amountInTier, tier.getCommissionRatePercentage(), tierCommission);
        }

        result.setVehicleCommissionExclTax(totalCommission);
        result.setTotalCommissionExclTax(totalCommission);
        result.addCalculationDetail("tierCount", tiers.size());
        result.addCalculationDetail("totalBase", calculationBase);

        log.debug("Total tiered commission: {} for order {}", totalCommission, context.getOrderNumber());

        return result;
    }

    private BigDecimal calculateAmountInTier(BigDecimal remainingAmount, CommissionScaleTier tier) {
        BigDecimal tierCapacity;

        if (tier.getThresholdMax() == null) {
            // Unlimited tier - use all remaining amount
            tierCapacity = remainingAmount;
        } else {
            // Calculate tier capacity
            BigDecimal tierRange = tier.getThresholdMax().subtract(tier.getThresholdMin());
            tierCapacity = tierRange.min(remainingAmount);
        }

        return tierCapacity;
    }

    @Override
    public boolean supports(CalculationMethod method) {
        return method == CalculationMethod.TIERED;
    }

    @Override
    public CalculationMethod getCalculationMethod() {
        return CalculationMethod.TIERED;
    }
}
