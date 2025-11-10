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
public class CommissionPaidEvent {

    private Long commissionId;
    private Long orderId;
    private String orderNumber;

    private Long salespersonId;
    private String salespersonName;

    private BigDecimal totalCommissionExclTax;
    private BigDecimal totalCommissionInclTax;

    private Long paymentBatchId;
    private String paymentBatchNumber;
    private String paymentReference;

    private LocalDateTime paymentDate;
    private String eventId;
    private LocalDateTime eventTimestamp;
}
