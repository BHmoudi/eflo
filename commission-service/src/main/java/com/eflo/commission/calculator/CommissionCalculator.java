package com.eflo.commission.calculator;

import com.eflo.commission.domain.enums.CalculationMethod;

import java.math.BigDecimal;

/**
 * Strategy interface for commission calculation
 */
public interface CommissionCalculator {

    /**
     * Calculate commission based on the provided context
     */
    CommissionCalculationResult calculate(CommissionCalculationContext context);

    /**
     * Check if this calculator supports the given calculation method
     */
    boolean supports(CalculationMethod method);

    /**
     * Get the calculation method this calculator supports
     */
    CalculationMethod getCalculationMethod();
}
