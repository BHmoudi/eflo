package com.eflo.workflow.domain.entity;

import com.eflo.workflow.domain.enums.TaskStatus;
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
 * WorkflowInstanceTask Entity
 *
 * Represents an actual task instance created from a task template.
 * These are the tasks that users work on during workflow execution.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_instance_tasks", indexes = {
    @Index(name = "idx_workflow_instance_tasks_instance", columnList = "instance_id"),
    @Index(name = "idx_workflow_instance_tasks_state_task", columnList = "state_task_id"),
    @Index(name = "idx_workflow_instance_tasks_status", columnList = "task_status"),
    @Index(name = "idx_workflow_instance_tasks_assigned_user", columnList = "assigned_to_user_id"),
    @Index(name = "idx_workflow_instance_tasks_assigned_role", columnList = "assigned_to_role"),
    @Index(name = "idx_workflow_instance_tasks_dates", columnList = "assigned_at, completed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowInstanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private WorkflowInstance instance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_task_id", nullable = false)
    private WorkflowStateTask stateTask;

    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus taskStatus = TaskStatus.PENDING;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(name = "assigned_to_role", length = 50)
    private String assignedToRole;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expected_completion_at")
    private LocalDateTime expectedCompletionAt;

    @Column(name = "is_overdue", nullable = false)
    @Builder.Default
    private Boolean isOverdue = false;

    @Column(name = "overdue_since")
    private LocalDateTime overdueSince;

    @Column(name = "escalation_level", nullable = false)
    @Builder.Default
    private Integer escalationLevel = 0;

    @Column(name = "escalated_to_user_id")
    private Long escalatedToUserId;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Type(JsonBinaryType.class)
    @Column(name = "task_data", columnDefinition = "jsonb")
    private Map<String, Object> taskData;

    @Type(JsonBinaryType.class)
    @Column(name = "completion_data", columnDefinition = "jsonb")
    private Map<String, Object> completionData;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Assign task to user
     */
    public void assignTo(Long userId, String assignedByUser) {
        this.assignedToUserId = userId;
        this.assignedAt = LocalDateTime.now();
        this.assignedBy = assignedByUser;
        this.taskStatus = TaskStatus.ASSIGNED;
    }

    /**
     * Start task
     */
    public void start() {
        this.taskStatus = TaskStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Complete task
     */
    public void complete(Map<String, Object> completionData) {
        this.taskStatus = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.completionData = completionData;
    }

    /**
     * Skip task
     */
    public void skip() {
        this.taskStatus = TaskStatus.SKIPPED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Fail task
     */
    public void fail(String reason) {
        this.taskStatus = TaskStatus.FAILED;
        this.comments = reason;
    }

    /**
     * Cancel task
     */
    public void cancel() {
        this.taskStatus = TaskStatus.CANCELLED;
    }

    /**
     * Escalate task
     */
    public void escalate(Long escalatedToUserId) {
        this.escalationLevel++;
        this.escalatedToUserId = escalatedToUserId;
        this.escalatedAt = LocalDateTime.now();
    }

    /**
     * Check if overdue
     */
    public void checkOverdue() {
        if (expectedCompletionAt != null &&
            LocalDateTime.now().isAfter(expectedCompletionAt) &&
            !taskStatus.isTerminal()) {
            this.isOverdue = true;
            if (this.overdueSince == null) {
                this.overdueSince = LocalDateTime.now();
            }
        }
    }
}
