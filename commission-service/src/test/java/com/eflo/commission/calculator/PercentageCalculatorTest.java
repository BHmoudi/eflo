package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PercentageCalculatorTest {

    private PercentageCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PercentageCalculator();
    }

    @Test
    void testSupports_ShouldReturnTrueForPercentageMethod() {
        assertTrue(calculator.supports(CalculationMethod.PERCENTAGE_MARGIN));
    }

    @Test
    void testSupports_ShouldReturnFalseForOtherMethods() {
        assertFalse(calculator.supports(CalculationMethod.FIXED_AMOUNT));
    }

    @Test
    void testCalculate_WithMarginBase() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TEST-SCALE")
                .scaleName("Test Scale")
                .calculationMethod(CalculationMethod.PERCENTAGE_MARGIN)
                .commissionType(CommissionType.VEHICLE)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(1000))
                .vehicleMargin(BigDecimal.valueOf(800))
                .accessoryMargin(BigDecimal.valueOf(150))
                .serviceMargin(BigDecimal.valueOf(50))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(80.00).setScale(2), result.getVehicleCommissionExclTax());
        assertEquals(BigDecimal.valueOf(15.00).setScale(2), result.getAccessoryCommissionExclTax());
        assertEquals(BigDecimal.valueOf(5.00).setScale(2), result.getServiceCommissionExclTax());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result.getTotalCommissionExclTax());
        assertEquals("PERCENTAGE", result.getCalculationMethod());
    }

    @Test
    void testCalculate_WithRevenueBase() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TEST-SCALE")
                .scaleName("Test Scale")
                .calculationMethod(CalculationMethod.PERCENTAGE_REVENUE)
                .commissionType(CommissionType.VEHICLE)
                .commissionRatePercentage(BigDecimal.valueOf(5))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderTotalRevenue(BigDecimal.valueOf(10000))
                .orderNetMargin(BigDecimal.valueOf(2000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500.00).setScale(2), result.getTotalCommissionExclTax());
        assertEquals("PERCENTAGE_ON_REVENUE", result.getCalculationMethod());
    }

    @Test
    void testCalculate_WithMaximumCap() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TEST-SCALE")
                .scaleName("Test Scale")
                .calculationMethod(CalculationMethod.PERCENTAGE_MARGIN)
                .commissionType(CommissionType.VEHICLE)
                .commissionRatePercentage(BigDecimal.valueOf(20))
                .maximumCommissionAmount(BigDecimal.valueOf(150))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(1000))
                .vehicleMargin(BigDecimal.valueOf(1000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        // 20% of 1000 = 200, but capped at 150
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), result.getTotalCommissionExclTax());
    }

    @Test
    void testCalculate_WithTaxCalculation() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TEST-SCALE")
                .scaleName("Test Scale")
                .calculationMethod(CalculationMethod.PERCENTAGE_MARGIN)
                .commissionType(CommissionType.VEHICLE)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(1000))
                .vehicleMargin(BigDecimal.valueOf(1000))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        BigDecimal expectedExclTax = BigDecimal.valueOf(100.00).setScale(2);
        BigDecimal expectedTax = BigDecimal.valueOf(21.00).setScale(2);
        BigDecimal expectedInclTax = BigDecimal.valueOf(121.00).setScale(2);

        assertEquals(expectedExclTax, result.getTotalCommissionExclTax());
        assertEquals(expectedTax, result.getTotalCommissionTax());
        assertEquals(expectedInclTax, result.getTotalCommissionInclTax());
    }

    @Test
    void testCalculate_WithZeroMargin() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TEST-SCALE")
                .scaleName("Test Scale")
                .calculationMethod(CalculationMethod.PERCENTAGE_MARGIN)
                .commissionType(CommissionType.VEHICLE)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.ZERO)
                .vehicleMargin(BigDecimal.ZERO)
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalCommissionExclTax());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalCommissionTax());
    }
}
