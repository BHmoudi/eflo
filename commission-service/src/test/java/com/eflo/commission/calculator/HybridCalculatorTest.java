package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.entity.CommissionScaleTier;
import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HybridCalculatorTest {

    private HybridCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HybridCalculator();
    }

    @Test
    void testSupports_ShouldReturnTrueForHybridMethod() {
        assertTrue(calculator.supports(CalculationMethod.HYBRID));
    }

    @Test
    void testSupports_ShouldReturnFalseForOtherMethods() {
        assertFalse(calculator.supports(CalculationMethod.PERCENTAGE_MARGIN));
    }

    @Test
    void testCalculate_WithPercentageAndFixedAmount() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("HYBRID-SCALE")
                .scaleName("Hybrid Scale")
                .calculationMethod(CalculationMethod.HYBRID)
                .commissionType(CommissionType.COMBINED)
                .commissionRatePercentage(BigDecimal.valueOf(5))
                .fixedAmount(BigDecimal.valueOf(100))
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
        // 5% of 1000 = 50, plus fixed 100 = 150
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), result.getTotalCommissionExclTax());
        assertEquals("HYBRID", result.getCalculationMethod());
    }

    @Test
    void testCalculate_WithTiersAndFixedAmount() {
        List<CommissionScaleTier> tiers = new ArrayList<>();
        tiers.add(CommissionScaleTier.builder()
                .tierOrder(1)
                .tierName("Tier 1")
                .thresholdMin(BigDecimal.ZERO)
                .thresholdMax(BigDecimal.valueOf(1000))
                .commissionRatePercentage(BigDecimal.valueOf(5))
                .build());
        tiers.add(CommissionScaleTier.builder()
                .tierOrder(2)
                .tierName("Tier 2")
                .thresholdMin(BigDecimal.valueOf(1000))
                .thresholdMax(null)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .build());

        CommissionScale scale = CommissionScale.builder()
                .scaleCode("HYBRID-SCALE")
                .scaleName("Hybrid Scale")
                .calculationMethod(CalculationMethod.HYBRID)
                .commissionType(CommissionType.COMBINED)
                .fixedAmount(BigDecimal.valueOf(50))
                .tiers(tiers)
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(1500))
                .vehicleMargin(BigDecimal.valueOf(1500))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        // Tier 1: 1000 * 5% = 50
        // Tier 2: 500 * 10% = 50
        // Fixed: 50
        // Total: 150
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), result.getTotalCommissionExclTax());
    }

    @Test
    void testCalculate_WithMaximumCap() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("HYBRID-SCALE")
                .scaleName("Hybrid Scale")
                .calculationMethod(CalculationMethod.HYBRID)
                .commissionType(CommissionType.COMBINED)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .fixedAmount(BigDecimal.valueOf(100))
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
        // 10% of 1000 = 100, plus fixed 100 = 200, but capped at 150
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), result.getTotalCommissionExclTax());
    }

    @Test
    void testCalculate_WithOnlyFixedAmount() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("HYBRID-SCALE")
                .scaleName("Hybrid Scale")
                .calculationMethod(CalculationMethod.HYBRID)
                .commissionType(CommissionType.COMBINED)
                .fixedAmount(BigDecimal.valueOf(200))
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
        assertEquals(BigDecimal.valueOf(200.00).setScale(2), result.getTotalCommissionExclTax());
    }

    @Test
    void testCalculate_WithTaxCalculation() {
        CommissionScale scale = CommissionScale.builder()
                .scaleCode("HYBRID-SCALE")
                .scaleName("Hybrid Scale")
                .calculationMethod(CalculationMethod.HYBRID)
                .commissionType(CommissionType.COMBINED)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .fixedAmount(BigDecimal.valueOf(50))
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
        // 10% of 1000 = 100, plus 50 = 150
        BigDecimal expectedExclTax = BigDecimal.valueOf(150.00).setScale(2);
        BigDecimal expectedTax = BigDecimal.valueOf(31.50).setScale(2);
        BigDecimal expectedInclTax = BigDecimal.valueOf(181.50).setScale(2);

        assertEquals(expectedExclTax, result.getTotalCommissionExclTax());
        assertEquals(expectedTax, result.getTotalCommissionTax());
        assertEquals(expectedInclTax, result.getTotalCommissionInclTax());
    }
}
