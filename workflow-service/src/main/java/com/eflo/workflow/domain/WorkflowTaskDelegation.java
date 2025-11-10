package com.eflo.workflow.domain;

import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.DelegationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WorkflowTaskDelegation Entity
 * Maps to workflow_task_delegations table
 * Tracks delegation of tasks from one user to another
 */
@Entity
@Table(name = "workflow_task_delegations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskDelegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_task_id", nullable = false)
    private WorkflowInstanceTask instanceTask;

    @Column(name = "delegated_from_user_id", nullable = false)
    private Long delegatedFromUserId;

    @Column(name = "delegated_to_user_id", nullable = false)
    private Long delegatedToUserId;

    @Column(name = "delegation_reason", columnDefinition = "TEXT")
    private String delegationReason;

    @Column(name = "delegated_at", nullable = false)
    private LocalDateTime delegatedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delegation_status", nullable = false, length = 20)
    @Builder.Default
    private DelegationStatus delegationStatus = DelegationStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void accept() {
        this.delegationStatus = DelegationStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject() {
        this.delegationStatus = DelegationStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }

    public void complete() {
        this.delegationStatus = DelegationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.delegationStatus = DelegationStatus.CANCELLED;
    }

    public boolean isPending() {
        return DelegationStatus.PENDING.equals(delegationStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTaskDelegation)) return false;
        WorkflowTaskDelegation that = (WorkflowTaskDelegation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowTaskDelegation{" +
                "id=" + id +
                ", delegatedFromUserId=" + delegatedFromUserId +
                ", delegatedToUserId=" + delegatedToUserId +
                ", delegationStatus=" + delegationStatus +
                '}';
    }
}
