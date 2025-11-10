package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FixedAmountCalculatorTest {

    private FixedAmountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FixedAmountCalculator();
    }

    @Test
    void testSupports_ShouldReturnTrueForFixedAmountMethod() {
        assertTrue(calculator.supports(CalculationMethod.FIXED_AMOUNT));
    }

    @Test
    void testSupports_ShouldReturnFalseForOtherMethods() {
        assertFalse(calculator.supports(CalculationMethod.PERCENTAGE_MARGIN));
    }

    @Test
    void testCalculate_WithFixedAmount() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("FIXED-SCALE")
                .scaleName("Fixed Scale")
                .calculationMethod(CalculationMethod.FIXED_AMOUNT)
                .commissionType(CommissionType.COMBINED)
                .fixedAmount(BigDecimal.valueOf(500))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(1000))
                .orderTotalRevenue(BigDecimal.valueOf(5000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500.00).setScale(2), result.getTotalCommissionExclTax());
        assertEquals("FIXED_AMOUNT", result.getCalculationMethod());
    }

    @Test
    void testCalculate_WithTaxCalculation() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("FIXED-SCALE")
                .scaleName("Fixed Scale")
                .calculationMethod(CalculationMethod.FIXED_AMOUNT)
                .commissionType(CommissionType.COMBINED)
                .fixedAmount(BigDecimal.valueOf(1000))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(2000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        BigDecimal expectedExclTax = BigDecimal.valueOf(1000.00).setScale(2);
        BigDecimal expectedTax = BigDecimal.valueOf(210.00).setScale(2);
        BigDecimal expectedInclTax = BigDecimal.valueOf(1210.00).setScale(2);

        assertEquals(expectedExclTax, result.getTotalCommissionExclTax());
        assertEquals(expectedTax, result.getTotalCommissionTax());
        assertEquals(expectedInclTax, result.getTotalCommissionInclTax());
    }

    @Test
    void testCalculate_WithMinimumMarginNotMet() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("FIXED-SCALE")
                .scaleName("Fixed Scale")
                .calculationMethod(CalculationMethod.FIXED_AMOUNT)
                .commissionType(CommissionType.VEHICLE)
                .fixedAmount(BigDecimal.valueOf(500))
                .minimumMarginRequired(BigDecimal.valueOf(1000))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(500))
                .orderTotalRevenue(BigDecimal.valueOf(5000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        // Should return zero commission when minimum margin not met
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalCommissionExclTax());
    }

    @Test
    void testCalculate_WithMinimumMarginMet() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("FIXED-SCALE")
                .scaleName("Fixed Scale")
                .calculationMethod(CalculationMethod.FIXED_AMOUNT)
                .commissionType(CommissionType.VEHICLE)
                .fixedAmount(BigDecimal.valueOf(500))
                .minimumMarginRequired(BigDecimal.valueOf(800))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(1000))
                .orderTotalRevenue(BigDecimal.valueOf(5000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500.00).setScale(2), result.getTotalCommissionExclTax());
    }
}
