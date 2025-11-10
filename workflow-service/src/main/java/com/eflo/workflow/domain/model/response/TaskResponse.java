package com.eflo.workflow.domain.model.response;

import com.eflo.workflow.domain.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for workflow task
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private Long instanceId;
    private String taskCode;
    private String taskName;
    private TaskStatus taskStatus;
    private Long assignedToUserId;
    private String assignedToUserName;
    private String assignedToRole;
    private LocalDateTime assignedAt;
    private String assignedBy;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expectedCompletionAt;
    private Boolean isOverdue;
    private LocalDateTime overdueSince;
    private Integer escalationLevel;
    private Map<String, Object> taskData;
    private Map<String, Object> completionData;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
