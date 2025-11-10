package com.eflo.commission.domain.model.response;

import com.eflo.commission.domain.model.dto.CommissionPaymentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentBatchResponse {

    private CommissionPaymentDTO payment;
    private List<Long> includedCommissionIds;
    private Integer successCount;
    private Integer failureCount;
    private List<String> errors;
}
