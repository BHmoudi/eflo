package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Approval Required Event
 *
 * Published when an approval is required for a task or workflow.
 * Contains information about the approval request including approver details and level.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequiredEvent implements Serializable {

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
     * Role code of the approver
     */
    private String roleCode;

    /**
     * Approval level order (for multi-level approvals)
     */
    private Integer levelOrder;

    /**
     * Due date for approval decision
     */
    private LocalDateTime dueDate;
}
