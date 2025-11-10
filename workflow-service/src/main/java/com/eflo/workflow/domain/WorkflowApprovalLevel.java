package com.eflo.workflow.domain;

import com.eflo.workflow.domain.enums.ApprovalType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_approval_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowApprovalLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id", nullable = false)
    private WorkflowApprovalChain chain;

    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_role_id")
    private WorkflowRole requiredRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", length = 20)
    @Builder.Default
    private ApprovalType approvalType = ApprovalType.SINGLE;

    @Column(name = "is_parallel")
    @Builder.Default
    private Boolean isParallel = false;

    @Column(name = "timeout_hours")
    private Integer timeoutHours;

    @Column(name = "auto_approve_on_timeout")
    @Builder.Default
    private Boolean autoApproveOnTimeout = false;

    @Column(name = "notify_on_assignment")
    @Builder.Default
    private Boolean notifyOnAssignment = true;

    @Column(name = "notify_before_timeout_hours")
    private Integer notifyBeforeTimeoutHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
