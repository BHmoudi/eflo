package com.eflo.workflow.domain.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reassigning a workflow task
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignTaskRequest {

    @NotNull(message = "New assignee user ID is required")
    private Long newAssigneeUserId;

    private String reason;
}
