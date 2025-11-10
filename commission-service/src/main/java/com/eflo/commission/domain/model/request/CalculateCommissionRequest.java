package com.eflo.commission.domain.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateCommissionRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotBlank(message = "Order type is required")
    private String orderType;

    @NotNull(message = "Business unit ID is required")
    private Long businessUnitId;

    private String businessUnitName;

    @NotNull(message = "Salesperson ID is required")
    private Long salespersonId;

    @NotBlank(message = "Salesperson name is required")
    private String salespersonName;

    private Long managerId;
    private String managerName;

    @NotNull(message = "Order total revenue is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Order total revenue must be positive")
    private BigDecimal orderTotalRevenue;

    @NotNull(message = "Order net margin is required")
    private BigDecimal orderNetMargin;

    @DecimalMin(value = "0.0", inclusive = true, message = "Vehicle margin must be positive")
    private BigDecimal vehicleMargin;

    @DecimalMin(value = "0.0", inclusive = true, message = "Accessory margin must be positive")
    private BigDecimal accessoryMargin;

    @DecimalMin(value = "0.0", inclusive = true, message = "Service margin must be positive")
    private BigDecimal serviceMargin;

    private String scaleCode;
    private BigDecimal taxRate;

    private Map<String, Object> additionalData;
}
