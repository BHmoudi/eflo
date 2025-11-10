package com.eflo.commission.domain.model.request;

import com.eflo.commission.domain.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionSearchCriteria {

    private Long orderId;
    private String orderNumber;
    private String orderType;

    private Long businessUnitId;
    private Long salespersonId;
    private Long managerId;

    private String scaleCode;
    private List<CommissionStatus> statuses;

    private LocalDate calculationDateFrom;
    private LocalDate calculationDateTo;

    private LocalDate paymentDateFrom;
    private LocalDate paymentDateTo;

    private BigDecimal minCommissionAmount;
    private BigDecimal maxCommissionAmount;

    private Long paymentBatchId;

    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
