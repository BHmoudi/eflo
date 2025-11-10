package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for workflow task information from Workflow Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private Long workflowId;
    private String taskName;
    private String taskType;
    private String status;
    private String assignedTo;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private Map<String, Object> taskData;
    private String completedBy;
}
