package com.eflo.commission.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCompletedEvent {

    private Long orderId;
    private String orderNumber;
    private String orderType;

    private Long businessUnitId;
    private String businessUnitName;

    private Long salespersonId;
    private String salespersonName;
    private Long managerId;
    private String managerName;

    private BigDecimal orderTotalRevenue;
    private BigDecimal orderNetMargin;

    private BigDecimal vehicleMargin;
    private BigDecimal accessoryMargin;
    private BigDecimal serviceMargin;

    private LocalDateTime orderCompletedDate;
    private String eventId;
    private LocalDateTime eventTimestamp;

    private Map<String, Object> metadata;
}
