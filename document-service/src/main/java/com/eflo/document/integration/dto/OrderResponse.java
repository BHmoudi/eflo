package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for order information from Order Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String orderType;
    private String status;
    private Long customerId;
    private String customerName;
    private Long businessUnitId;
    private String businessUnitName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
