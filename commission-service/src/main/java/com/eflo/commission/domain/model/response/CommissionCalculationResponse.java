package com.eflo.commission.domain.model.response;

import com.eflo.commission.domain.model.dto.CommissionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionCalculationResponse {

    private CommissionDTO commission;
    private boolean calculated;
    private String calculationMessage;
    private Map<String, Object> metadata;
}
