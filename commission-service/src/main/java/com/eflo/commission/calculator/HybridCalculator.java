package com.eflo.commission.calculator;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CalculationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Calculator for hybrid commissions (combination of vehicle, accessory, and service)
 */
@Component
@Slf4j
public class HybridCalculator implements CommissionCalculator {

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationContext context) {
        CommissionScale scale = context.getCommissionScale();

        log.debug("Calculating hybrid commission for order {} using scale {}",
                context.getOrderNumber(), scale.getScaleCode());

        Map<String, Object> config = scale.getConfigurationJson();
        if (config == null) {
            log.error("No hybrid configuration found for scale {}", scale.getScaleCode());
            return CommissionCalculationResult.builder()
                    .valid(false)
                    .validationMessage("No hybrid configuration found")
                    .totalCommissionExclTax(BigDecimal.ZERO)
                    .build();
        }

        // Calculate vehicle commission
        BigDecimal vehicleCommission = calculateVehicleCommission(context, config);

        // Calculate accessory commission
        BigDecimal accessoryCommission = calculateAccessoryCommission(context, config);

        // Calculate service commission
        BigDecimal serviceCommission = calculateServiceCommission(context, config);

        BigDecimal totalCommission = vehicleCommission
                .add(accessoryCommission)
                .add(serviceCommission);

        CommissionCalculationResult result = CommissionCalculationResult.builder()
                .vehicleCommissionExclTax(vehicleCommission)
                .accessoryCommissionExclTax(accessoryCommission)
                .serviceCommissionExclTax(serviceCommission)
                .totalCommissionExclTax(totalCommission)
                .calculationMethod(scale.getCalculationMethod().name())
                .valid(true)
                .build();

        result.addCalculationDetail("vehicleCommission", vehicleCommission);
        result.addCalculationDetail("accessoryCommission", accessoryCommission);
        result.addCalculationDetail("serviceCommission", serviceCommission);
        result.addCalculationDetail("configuration", config);

        log.debug("Hybrid commission - Vehicle: {}, Accessory: {}, Service: {}, Total: {}",
                vehicleCommission, accessoryCommission, serviceCommission, totalCommission);

        return result;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal calculateVehicleCommission(CommissionCalculationContext context, Map<String, Object> config) {
        Map<String, Object> vehicleConfig = (Map<String, Object>) config.get("vehicleCommission");
        if (vehicleConfig == null || context.getVehicleMargin() == null) {
            return BigDecimal.ZERO;
        }

        String method = (String) vehicleConfig.get("method");
        if ("PERCENTAGE_MARGIN".equals(method)) {
            Number rate = (Number) vehicleConfig.get("rate");
            return context.getVehicleMargin()
                    .multiply(BigDecimal.valueOf(rate.doubleValue()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("FIXED_AMOUNT".equals(method)) {
            Number amount = (Number) vehicleConfig.get("amount");
            return BigDecimal.valueOf(amount.doubleValue());
        }

        return BigDecimal.ZERO;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal calculateAccessoryCommission(CommissionCalculationContext context, Map<String, Object> config) {
        Map<String, Object> accessoryConfig = (Map<String, Object>) config.get("accessoryCommission");
        if (accessoryConfig == null || context.getAccessoryMargin() == null) {
            return BigDecimal.ZERO;
        }

        String method = (String) accessoryConfig.get("method");
        if ("PERCENTAGE_MARGIN".equals(method)) {
            Number rate = (Number) accessoryConfig.get("rate");
            return context.getAccessoryMargin()
                    .multiply(BigDecimal.valueOf(rate.doubleValue()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("FIXED_AMOUNT".equals(method)) {
            Number amount = (Number) accessoryConfig.get("amount");
            return BigDecimal.valueOf(amount.doubleValue());
        }

        return BigDecimal.ZERO;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal calculateServiceCommission(CommissionCalculationContext context, Map<String, Object> config) {
        Map<String, Object> serviceConfig = (Map<String, Object>) config.get("serviceCommission");
        if (serviceConfig == null || context.getServiceMargin() == null) {
            return BigDecimal.ZERO;
        }

        String method = (String) serviceConfig.get("method");
        if ("PERCENTAGE_MARGIN".equals(method)) {
            Number rate = (Number) serviceConfig.get("rate");
            return context.getServiceMargin()
                    .multiply(BigDecimal.valueOf(rate.doubleValue()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("FIXED_AMOUNT".equals(method)) {
            Number amount = (Number) serviceConfig.get("amount");
            return BigDecimal.valueOf(amount.doubleValue());
        }

        return BigDecimal.ZERO;
    }

    @Override
    public boolean supports(CalculationMethod method) {
        return method == CalculationMethod.HYBRID;
    }

    @Override
    public CalculationMethod getCalculationMethod() {
        return CalculationMethod.HYBRID;
    }
}
