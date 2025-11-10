package com.eflo.workflow.domain;

import com.eflo.workflow.domain.entity.WorkflowStateTask;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WorkflowTaskApprovalChain Entity
 * Maps to workflow_task_approval_chains table
 * Links tasks to approval chains that must be completed
 */
@Entity
@Table(name = "workflow_task_approval_chains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskApprovalChain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private WorkflowStateTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id", nullable = false)
    private WorkflowApprovalChain approvalChain;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTaskApprovalChain)) return false;
        WorkflowTaskApprovalChain that = (WorkflowTaskApprovalChain) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowTaskApprovalChain{" +
                "id=" + id +
                ", isMandatory=" + isMandatory +
                '}';
    }
}
