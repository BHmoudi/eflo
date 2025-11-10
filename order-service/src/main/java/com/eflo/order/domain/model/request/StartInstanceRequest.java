package com.eflo.order.domain.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartInstanceRequest {
    private Long processId;
    private Long orderId;
    private String instanceName;
    private String priority;
    private Map<String, Object> contextData;
    private Integer expectedDurationDays;
}
