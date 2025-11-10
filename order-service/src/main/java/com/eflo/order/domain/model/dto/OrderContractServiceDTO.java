package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderContractServiceDTO {
    private Long id;
    private Long orderId;
    private String serviceCode;
    private String serviceName;
    private String serviceType;
    private String description;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer durationMonths;
    private String coverageDetails;
    private String provider;
}
