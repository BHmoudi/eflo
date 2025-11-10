package com.eflo.commission.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentBatchCreatedEvent {

    private Long paymentBatchId;
    private String batchNumber;
    private String batchName;

    private Integer paymentYear;
    private Integer paymentMonth;
    private String paymentPeriod;

    private Long businessUnitId;
    private String businessUnitName;

    private Integer totalCommissions;
    private BigDecimal totalAmountExclTax;
    private BigDecimal totalAmountInclTax;

    private LocalDate scheduledPaymentDate;
    private String createdBy;

    private String eventId;
    private LocalDateTime eventTimestamp;
}
