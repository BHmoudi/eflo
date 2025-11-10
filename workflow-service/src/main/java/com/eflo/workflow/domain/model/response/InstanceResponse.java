package com.eflo.workflow.domain.model.response;

import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for workflow instance
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceResponse {

    private Long id;
    private Long processId;
    private String processCode;
    private String processName;
    private Long currentStateId;
    private String currentStateName;
    private Long orderId;
    private String instanceName;
    private InstanceStatus instanceStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime expectedCompletionDate;
    private LocalDateTime actualCompletionDate;
    private Boolean isOverdue;
    private LocalDateTime overdueSince;
    private Priority priority;
    private Map<String, Object> contextData;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
