package com.eflo.commission.domain.model.dto;

import com.eflo.commission.domain.enums.PaymentStatus;
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
public class CommissionPaymentDTO {

    private Long id;
    private String batchNumber;
    private String batchName;

    private Integer paymentYear;
    private Integer paymentMonth;
    private String paymentPeriod;

    private Long businessUnitId;
    private String businessUnitName;

    private Integer totalCommissions;
    private BigDecimal totalAmountExclTax;
    private BigDecimal totalAmountTax;
    private BigDecimal totalAmountInclTax;

    private PaymentStatus status;
    private String paymentMethod;
    private String paymentReference;
    private String paymentFilePath;

    private LocalDate scheduledPaymentDate;
    private LocalDateTime processedDate;
    private LocalDateTime completedDate;

    private LocalDateTime processingStartedAt;
    private LocalDateTime processingCompletedAt;
    private String processingError;

    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
