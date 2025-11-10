package com.eflo.commission.domain.model.dto;

import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionScaleDTO {

    private Long id;
    private String scaleCode;
    private String scaleName;
    private String scaleDescription;

    private Long businessUnitId;
    private String orderType;

    private CommissionType commissionType;
    private CalculationMethod calculationMethod;

    private BigDecimal commissionRatePercentage;
    private BigDecimal fixedAmount;

    private BigDecimal minimumMarginRequired;
    private BigDecimal minimumRevenueRequired;
    private BigDecimal maximumCommissionAmount;

    private Boolean managerSplitEnabled;
    private BigDecimal managerSplitPercentage;

    private Map<String, Object> configurationJson;

    private LocalDate validFrom;
    private LocalDate validTo;

    private Boolean isActive;
    private Boolean isDefault;

    @Builder.Default
    private List<CommissionScaleTierDTO> tiers = new ArrayList<>();

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Integer version;
}
