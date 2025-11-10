package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CalculationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for percentage-based commissions (on margin or revenue)
 */
@Component
@Slf4j
public class PercentageCalculator implements CommissionCalculator {

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationContext context) {
        CommissionScale scale = context.getCommissionScale();

        log.debug("Calculating percentage commission for order {} using scale {}",
                context.getOrderNumber(), scale.getScaleCode());

        // Get calculation base (margin or revenue)
        BigDecimal calculationBase = context.getCalculationBase();
        BigDecimal rate = scale.getCommissionRatePercentage();

        // Check minimum requirements
        if (!meetsMinimumRequirements(context, scale)) {
            return CommissionCalculationResult.builder()
                    .valid(false)
                    .validationMessage("Order does not meet minimum requirements")
                    .totalCommissionExclTax(BigDecimal.ZERO)
                    .build();
        }

        // Calculate commission
        BigDecimal commission = calculationBase
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Apply maximum cap if configured
        if (scale.getMaximumCommissionAmount() != null
                && commission.compareTo(scale.getMaximumCommissionAmount()) > 0) {
            log.debug("Commission {} exceeds maximum {}, capping",
                    commission, scale.getMaximumCommissionAmount());
            commission = scale.getMaximumCommissionAmount();
        }

        CommissionCalculationResult result = CommissionCalculationResult.builder()
                .vehicleCommissionExclTax(commission)
                .totalCommissionExclTax(commission)
                .calculationBase(calculationBase)
                .commissionRate(rate)
                .calculationMethod(scale.getCalculationMethod().name())
                .valid(true)
                .build();

        result.addCalculationDetail("baseAmount", calculationBase);
        result.addCalculationDetail("rate", rate);
        result.addCalculationDetail("formula", String.format("%.2f × %.2f%% = %.2f",
                calculationBase, rate, commission));

        log.debug("Calculated commission: {} for order {}", commission, context.getOrderNumber());

        return result;
    }

    private boolean meetsMinimumRequirements(CommissionCalculationContext context, CommissionScale scale) {
        if (scale.getMinimumMarginRequired() != null
                && context.getOrderNetMargin().compareTo(scale.getMinimumMarginRequired()) < 0) {
            log.debug("Order margin {} below minimum required {}",
                    context.getOrderNetMargin(), scale.getMinimumMarginRequired());
            return false;
        }

        if (scale.getMinimumRevenueRequired() != null
                && context.getOrderTotalRevenue().compareTo(scale.getMinimumRevenueRequired()) < 0) {
            log.debug("Order revenue {} below minimum required {}",
                    context.getOrderTotalRevenue(), scale.getMinimumRevenueRequired());
            return false;
        }

        return true;
    }

    @Override
    public boolean supports(CalculationMethod method) {
        return method == CalculationMethod.PERCENTAGE_MARGIN
                || method == CalculationMethod.PERCENTAGE_REVENUE;
    }

    @Override
    public CalculationMethod getCalculationMethod() {
        return CalculationMethod.PERCENTAGE_MARGIN;
    }
}
