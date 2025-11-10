package com.eflo.workflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_role_hierarchy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowRoleHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id", nullable = false)
    private WorkflowRole parentRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_role_id", nullable = false)
    private WorkflowRole childRole;

    @Column(name = "can_approve_for_child")
    @Builder.Default
    private Boolean canApproveForChild = true;

    @Column(name = "can_delegate_to_child")
    @Builder.Default
    private Boolean canDelegateToChild = false;

    @Column(name = "can_view_child_tasks")
    @Builder.Default
    private Boolean canViewChildTasks = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
