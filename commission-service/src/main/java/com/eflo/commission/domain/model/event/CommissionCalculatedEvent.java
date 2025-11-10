package com.eflo.commission.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionCalculatedEvent {

    private Long commissionId;
    private Long orderId;
    private String orderNumber;

    private Long salespersonId;
    private String salespersonName;

    private BigDecimal totalCommissionExclTax;
    private BigDecimal totalCommissionInclTax;

    private String scaleCode;
    private String calculationMethod;

    private LocalDateTime calculationDate;
    private String eventId;
    private LocalDateTime eventTimestamp;
}
