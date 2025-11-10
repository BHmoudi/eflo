package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Task Completed Event
 *
 * Published when a task is successfully completed by a user.
 * Contains information about the task completion including input data provided.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletedEvent implements Serializable {

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
     * User ID who completed the task
     */
    private Long userId;

    /**
     * User name who completed the task
     */
    private String userName;

    /**
     * Task code/identifier
     */
    private String taskCode;

    /**
     * Task display name
     */
    private String taskName;

    /**
     * Timestamp when the task was completed
     */
    private LocalDateTime completedAt;

    /**
     * Input data provided during task completion
     */
    private Map<String, Object> inputData;
}
