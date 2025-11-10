package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CalculationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Calculator for fixed amount commissions
 */
@Component
@Slf4j
public class FixedAmountCalculator implements CommissionCalculator {

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationContext context) {
        CommissionScale scale = context.getCommissionScale();

        log.debug("Calculating fixed amount commission for order {} using scale {}",
                context.getOrderNumber(), scale.getScaleCode());

        if (scale.getFixedAmount() == null) {
            log.error("No fixed amount configured for scale {}", scale.getScaleCode());
            return CommissionCalculationResult.builder()
                    .valid(false)
                    .validationMessage("No fixed amount configured for this scale")
                    .totalCommissionExclTax(BigDecimal.ZERO)
                    .build();
        }

        // Check minimum requirements
        if (!meetsMinimumRequirements(context, scale)) {
            return CommissionCalculationResult.builder()
                    .valid(false)
                    .validationMessage("Order does not meet minimum requirements")
                    .totalCommissionExclTax(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal fixedAmount = scale.getFixedAmount();

        CommissionCalculationResult result = CommissionCalculationResult.builder()
                .vehicleCommissionExclTax(fixedAmount)
                .totalCommissionExclTax(fixedAmount)
                .calculationBase(context.getOrderNetMargin())
                .calculationMethod(scale.getCalculationMethod().name())
                .valid(true)
                .build();

        result.addCalculationDetail("fixedAmount", fixedAmount);
        result.addCalculationDetail("margin", context.getOrderNetMargin());
        result.addCalculationDetail("revenue", context.getOrderTotalRevenue());

        log.debug("Fixed commission: {} for order {}", fixedAmount, context.getOrderNumber());

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
        return method == CalculationMethod.FIXED_AMOUNT;
    }

    @Override
    public CalculationMethod getCalculationMethod() {
        return CalculationMethod.FIXED_AMOUNT;
    }
}
