package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceDTO {
    private Long id;
    private Long processId;
    private String processCode;
    private String processName;
    private Long currentStateId;
    private String currentStateName;
    private Long orderId;
    private String instanceName;
    private String instanceStatus;
    private String priority;
    private String workflowCode;
    private String currentState;
    private String entityType;
    private Long entityId;
    private Map<String, Object> metadata;
    private Map<String, Object> contextData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long createdByUserId;
    private Long updatedByUserId;

    // Helper method to get current state name (supports both field names)
    public String getCurrentState() {
        return currentStateName != null ? currentStateName : currentState;
    }
}
