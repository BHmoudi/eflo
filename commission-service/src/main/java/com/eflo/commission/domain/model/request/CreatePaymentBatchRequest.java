package com.eflo.commission.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentBatchRequest {

    @NotBlank(message = "Batch name is required")
    private String batchName;

    @NotNull(message = "Payment year is required")
    private Integer paymentYear;

    @NotNull(message = "Payment month is required")
    private Integer paymentMonth;

    private Long businessUnitId;
    private String businessUnitName;

    @NotNull(message = "Commission IDs are required")
    private List<Long> commissionIds;

    private String paymentMethod;
    private LocalDate scheduledPaymentDate;

    private String notes;
}
