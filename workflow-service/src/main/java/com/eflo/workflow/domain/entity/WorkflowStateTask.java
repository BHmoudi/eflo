package com.eflo.workflow.domain.entity;

import com.eflo.workflow.domain.enums.TaskType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * WorkflowStateTask Entity
 *
 * Defines task templates for each workflow state.
 * These templates are used to create actual tasks when a workflow instance enters the state.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_state_tasks",
       uniqueConstraints = @UniqueConstraint(name = "unique_task_code_per_state",
                                            columnNames = {"state_id", "task_code"}),
       indexes = {
    @Index(name = "idx_workflow_state_tasks_state", columnList = "state_id"),
    @Index(name = "idx_workflow_state_tasks_code", columnList = "task_code"),
    @Index(name = "idx_workflow_state_tasks_type", columnList = "task_type"),
    @Index(name = "idx_workflow_state_tasks_role", columnList = "assigned_to_role"),
    @Index(name = "idx_workflow_state_tasks_user", columnList = "assigned_to_user_id"),
    @Index(name = "idx_workflow_state_tasks_order", columnList = "state_id, task_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowStateTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private WorkflowState state;

    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private TaskType taskType;

    @Column(name = "task_order", nullable = false)
    private Integer taskOrder;

    @Column(name = "assigned_to_role", length = 50)
    private String assignedToRole;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "expected_duration_hours")
    private Integer expectedDurationHours;

    @Column(name = "auto_assign", nullable = false)
    @Builder.Default
    private Boolean autoAssign = true;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    @Type(JsonBinaryType.class)
    @Column(name = "form_definition", columnDefinition = "jsonb")
    private Map<String, Object> formDefinition;

    @Type(JsonBinaryType.class)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;

    @Type(JsonBinaryType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @JsonIgnore
    @OneToMany(mappedBy = "stateTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowStateTaskDocument> requiredDocuments = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
