package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SLA Breach Event
 *
 * Published when a task breaches its Service Level Agreement (SLA).
 * Contains information about the breach including the time overdue.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaBreachEvent implements Serializable {

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
     * Task code/identifier
     */
    private String taskCode;

    /**
     * Task display name
     */
    private String taskName;

    /**
     * User ID to whom the task was assigned
     */
    private Long assignedUserId;

    /**
     * Original due date of the task
     */
    private LocalDateTime dueDate;

    /**
     * Timestamp when the breach occurred
     */
    private LocalDateTime breachedAt;

    /**
     * Number of hours the task is overdue
     */
    private Long breachHours;
}
