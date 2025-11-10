package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for workflow information from Workflow Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {

    private Long id;
    private String workflowName;
    private String workflowType;
    private Long orderId;
    private String currentState;
    private String status;
    private List<TaskResponse> tasks;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String createdBy;
}
