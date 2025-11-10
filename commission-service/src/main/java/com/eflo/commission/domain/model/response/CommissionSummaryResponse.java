package com.eflo.commission.domain.model.response;

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
public class CommissionSummaryResponse {

    private Integer totalCommissions;
    private BigDecimal totalAmountExclTax;
    private BigDecimal totalAmountInclTax;
    private BigDecimal averageCommission;

    private Map<String, Integer> commissionsByStatus;
    private Map<String, BigDecimal> commissionsByType;
    private Map<String, BigDecimal> commissionsBySalesperson;
    private Map<String, BigDecimal> commissionsByBusinessUnit;
}
