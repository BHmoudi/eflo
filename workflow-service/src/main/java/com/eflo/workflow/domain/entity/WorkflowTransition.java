package com.eflo.workflow.domain.entity;

import com.eflo.workflow.domain.enums.TransitionType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WorkflowTransition Entity
 *
 * Defines allowed transitions between workflow states.
 * Transitions control the flow and progression of workflow instances.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_transitions", indexes = {
    @Index(name = "idx_workflow_transitions_process", columnList = "process_id"),
    @Index(name = "idx_workflow_transitions_from_state", columnList = "from_state_id"),
    @Index(name = "idx_workflow_transitions_to_state", columnList = "to_state_id"),
    @Index(name = "idx_workflow_transitions_type", columnList = "transition_type"),
    @Index(name = "idx_workflow_transitions_auto", columnList = "auto_transition")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private WorkflowProcess process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_state_id", nullable = false)
    private WorkflowState fromState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_state_id", nullable = false)
    private WorkflowState toState;

    @Column(name = "transition_name", nullable = false)
    private String transitionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_type", nullable = false, length = 20)
    @Builder.Default
    private TransitionType transitionType = TransitionType.NORMAL;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(name = "requires_tasks_completion", nullable = false)
    @Builder.Default
    private Boolean requiresTasksCompletion = true;

    @Column(name = "requires_documents", nullable = false)
    @Builder.Default
    private Boolean requiresDocuments = false;

    @Column(name = "auto_transition", nullable = false)
    @Builder.Default
    private Boolean autoTransition = false;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = true;

    @Type(JsonBinaryType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
