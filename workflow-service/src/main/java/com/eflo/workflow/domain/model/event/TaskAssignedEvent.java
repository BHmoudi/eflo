package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Task Assigned Event
 *
 * Published when a task is assigned to a user or role.
 * Contains information about the task assignment including assignee details and due date.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the task
     */
    private Long taskId;

    /**
     * Workflow instance identifier
     */
    private Long instanceId;

    /**
     * Associated order identifier
     */
    private Long orderId;

    /**
     * User ID to whom the task is assigned
     */
    private Long userId;

    /**
     * User name to whom the task is assigned
     */
    private String userName;

    /**
     * Role code for role-based assignment
     */
    private String roleCode;

    /**
     * Task code/identifier
     */
    private String taskCode;

    /**
     * Task display name
     */
    private String taskName;

    /**
     * Timestamp when the task was assigned
     */
    private LocalDateTime assignedAt;

    /**
     * Due date for task completion
     */
    private LocalDateTime dueDate;
}
