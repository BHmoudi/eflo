package com.eflo.workflow.domain;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.entity.WorkflowInstance;

import com.eflo.workflow.domain.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_task_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_task_id", nullable = false)
    private WorkflowInstanceTask instanceTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_chain_id")
    private WorkflowApprovalChain approvalChain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_level_id")
    private WorkflowApprovalLevel approvalLevel;

    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;

    @Column(name = "approver_user_id", nullable = false)
    private Long approverUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_role_id")
    private WorkflowRole approverRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "delegated_to_user_id")
    private Long delegatedToUserId;

    @Column(name = "delegated_at")
    private LocalDateTime delegatedAt;

    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void approve(String comments) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.comments = comments;
    }

    public void reject(String comments) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.comments = comments;
    }

    public void delegate(Long toUserId) {
        this.approvalStatus = ApprovalStatus.DELEGATED;
        this.delegatedToUserId = toUserId;
        this.delegatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return ApprovalStatus.PENDING.equals(approvalStatus);
    }

    public boolean isCompleted() {
        return approvalStatus.isCompleted();
    }
}
