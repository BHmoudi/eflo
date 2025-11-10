package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Escalation Event
 *
 * Published when a task is escalated from one user to another.
 * Contains information about the escalation including the reason.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationEvent implements Serializable {

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
     * User ID from whom the task is escalated
     */
    private Long fromUserId;

    /**
     * User ID to whom the task is escalated
     */
    private Long toUserId;

    /**
     * Timestamp when the escalation occurred
     */
    private LocalDateTime escalatedAt;

    /**
     * Reason for escalation
     */
    private String reason;
}
