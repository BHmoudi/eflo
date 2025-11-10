package com.eflo.workflow.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Task information
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
    private String taskCode;
    private String taskName;
    private String taskType;
    private String status;
    private Long assignedUserId;
    private String assignedUserName;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private Long completedByUserId;
}
