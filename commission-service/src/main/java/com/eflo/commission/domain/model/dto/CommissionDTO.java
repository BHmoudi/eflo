package com.eflo.commission.domain.model.dto;

import com.eflo.commission.domain.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionDTO {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private String orderType;

    private Long commissionScaleId;
    private String scaleCode;
    private String scaleName;

    private Long businessUnitId;
    private String businessUnitName;

    private Long salespersonId;
    private String salespersonName;
    private Long managerId;
    private String managerName;

    private BigDecimal orderTotalRevenue;
    private BigDecimal orderNetMargin;

    private BigDecimal vehicleCommissionBase;
    private BigDecimal vehicleCommissionRate;
    private BigDecimal vehicleCommissionExclTax;
    private BigDecimal vehicleCommissionTax;
    private BigDecimal vehicleCommissionInclTax;

    private BigDecimal accessoryCommissionExclTax;
    private BigDecimal accessoryCommissionTax;
    private BigDecimal accessoryCommissionInclTax;

    private BigDecimal serviceCommissionExclTax;
    private BigDecimal serviceCommissionTax;
    private BigDecimal serviceCommissionInclTax;

    private BigDecimal totalCommissionExclTax;
    private BigDecimal totalCommissionTax;
    private BigDecimal totalCommissionInclTax;

    private Boolean managerSplitEnabled;
    private BigDecimal managerSplitPercentage;
    private BigDecimal managerCommissionExclTax;
    private BigDecimal salespersonNetCommission;

    private String calculationMethod;
    private Map<String, Object> calculationDetails;
    private Map<String, Object> tierBreakdown;

    private CommissionStatus status;
    private LocalDateTime calculationDate;
    private LocalDateTime validationDate;
    private LocalDateTime paymentDate;

    private Long paymentBatchId;
    private String paymentReference;
    private LocalDate paymentDueDate;

    private String adjustmentReason;
    private BigDecimal adjustmentAmount;
    private String adjustedBy;
    private LocalDateTime adjustedAt;

    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Integer version;
}
