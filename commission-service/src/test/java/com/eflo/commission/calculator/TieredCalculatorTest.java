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

class TieredCalculatorTest {

    private TieredCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TieredCalculator();
    }

    @Test
    void testSupports_ShouldReturnTrueForTieredMethod() {
        assertTrue(calculator.supports(CalculationMethod.TIERED));
    }

    @Test
    void testSupports_ShouldReturnFalseForOtherMethods() {
        assertFalse(calculator.supports(CalculationMethod.PERCENTAGE_MARGIN));
    }

    @Test
    void testCalculate_WithSingleTier() {
        List<CommissionScaleTier> tiers = new ArrayList<>();
        tiers.add(CommissionScaleTier.builder()
                .tierOrder(1)
                .tierName("Tier 1")
                .thresholdMin(BigDecimal.ZERO)
                .thresholdMax(BigDecimal.valueOf(1000))
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .build());

        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TIERED-SCALE")
                .scaleName("Tiered Scale")
                .calculationMethod(CalculationMethod.TIERED)
                .commissionType(CommissionType.VEHICLE)
                .tiers(tiers)
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(500))
                .vehicleMargin(BigDecimal.valueOf(500))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), result.getTotalCommissionExclTax());
        assertEquals("TIERED", result.getCalculationMethod());
    }

    @Test
    void testCalculate_WithMultipleTiers() {
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
                .thresholdMax(BigDecimal.valueOf(2000))
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .build());
        tiers.add(CommissionScaleTier.builder()
                .tierOrder(3)
                .tierName("Tier 3")
                .thresholdMin(BigDecimal.valueOf(2000))
                .thresholdMax(null)
                .commissionRatePercentage(BigDecimal.valueOf(15))
                .build());

        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TIERED-SCALE")
                .scaleName("Tiered Scale")
                .calculationMethod(CalculationMethod.TIERED)
                .commissionType(CommissionType.VEHICLE)
                .tiers(tiers)
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(2500))
                .vehicleMargin(BigDecimal.valueOf(2500))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        // Tier 1: 1000 * 5% = 50
        // Tier 2: 1000 * 10% = 100
        // Tier 3: 500 * 15% = 75
        // Total: 225
        assertEquals(BigDecimal.valueOf(225.00).setScale(2), result.getTotalCommissionExclTax());
    }

    @Test
    void testCalculate_WithTaxCalculation() {
        List<CommissionScaleTier> tiers = new ArrayList<>();
        tiers.add(CommissionScaleTier.builder()
                .tierOrder(1)
                .tierName("Tier 1")
                .thresholdMin(BigDecimal.ZERO)
                .thresholdMax(null)
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .build());

        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TIERED-SCALE")
                .scaleName("Tiered Scale")
                .calculationMethod(CalculationMethod.TIERED)
                .commissionType(CommissionType.VEHICLE)
                .tiers(tiers)
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
    void testCalculate_WithNoMatchingTier() {
        List<CommissionScaleTier> tiers = new ArrayList<>();
        tiers.add(CommissionScaleTier.builder()
                .tierOrder(1)
                .tierName("Tier 1")
                .thresholdMin(BigDecimal.valueOf(1000))
                .thresholdMax(BigDecimal.valueOf(2000))
                .commissionRatePercentage(BigDecimal.valueOf(10))
                .build());

        CommissionScale scale = CommissionScale.builder()
                .scaleCode("TIERED-SCALE")
                .scaleName("Tiered Scale")
                .calculationMethod(CalculationMethod.TIERED)
                .commissionType(CommissionType.VEHICLE)
                .tiers(tiers)
                .validFrom(LocalDate.now())
                .build();

        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderNetMargin(BigDecimal.valueOf(500))
                .vehicleMargin(BigDecimal.valueOf(500))
                .commissionScale(scale)
                .taxRate(BigDecimal.valueOf(21))
                .build();

        CommissionCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        // No tier matches, should return zero
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalCommissionExclTax());
    }
}
