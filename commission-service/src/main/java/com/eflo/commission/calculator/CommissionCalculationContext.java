package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Context for commission calculation containing all necessary data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionCalculationContext {

    // Order information
    private Long orderId;
    private String orderNumber;
    private String orderType;

    // Financial data
    private BigDecimal orderTotalRevenue;
    private BigDecimal orderNetMargin;
    private BigDecimal vehicleMargin;
    private BigDecimal accessoryMargin;
    private BigDecimal serviceMargin;

    // Commission scale
    private CommissionScale commissionScale;

    // Business context
    private Long businessUnitId;
    private String businessUnitName;

    // People
    private Long salespersonId;
    private String salespersonName;
    private Long managerId;
    private String managerName;

    // Additional data
    private Map<String, Object> additionalData;

    // Tax rate
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(21); // Default 21% VAT

    public BigDecimal getCalculationBase() {
        return commissionScale.getCalculationMethod().name().contains("REVENUE")
                ? orderTotalRevenue
                : orderNetMargin;
    }
}
