package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Approval Completed Event
 *
 * Published when an approval decision is made (approved or rejected).
 * Contains information about the approval decision including comments.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the approval
     */
    private Long approvalId;

    /**
     * Associated task identifier
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
     * User ID of the approver
     */
    private Long approverId;

    /**
     * User name of the approver
     */
    private String approverName;

    /**
     * Approval decision (true = approved, false = rejected)
     */
    private Boolean approved;

    /**
     * Approver's comments on the decision
     */
    private String comments;

    /**
     * Timestamp when the approval was completed
     */
    private LocalDateTime completedAt;
}
