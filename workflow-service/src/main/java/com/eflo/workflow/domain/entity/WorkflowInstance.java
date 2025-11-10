package com.eflo.workflow.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.enums.Priority;
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

/**
 * WorkflowInstance Entity
 *
 * Represents a running instance of a workflow process.
 * Each instance tracks the execution of a specific workflow for a specific order.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_instances", indexes = {
    @Index(name = "idx_workflow_instances_process", columnList = "process_id"),
    @Index(name = "idx_workflow_instances_current_state", columnList = "current_state_id"),
    @Index(name = "idx_workflow_instances_order", columnList = "order_id"),
    @Index(name = "idx_workflow_instances_status", columnList = "instance_status"),
    @Index(name = "idx_workflow_instances_priority", columnList = "priority"),
    @Index(name = "idx_workflow_instances_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_workflow_instances_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private WorkflowProcess process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_state_id")
    private WorkflowState currentState;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "instance_name")
    private String instanceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "instance_status", nullable = false, length = 20)
    @Builder.Default
    private InstanceStatus instanceStatus = InstanceStatus.CREATED;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "expected_completion_date")
    private LocalDateTime expectedCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;

    @Column(name = "is_overdue", nullable = false)
    @Builder.Default
    private Boolean isOverdue = false;

    @Column(name = "overdue_since")
    private LocalDateTime overdueSince;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Type(JsonBinaryType.class)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private Map<String, Object> contextData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonBinaryType.class)
    @Column(name = "error_details", columnDefinition = "jsonb")
    private Map<String, Object> errorDetails;

    @JsonIgnore
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowInstanceTask> tasks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowHistory> historyEntries = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Add a task to this instance
     */
    public void addTask(WorkflowInstanceTask task) {
        tasks.add(task);
        task.setInstance(this);
    }

    /**
     * Add a history entry
     */
    public void addHistoryEntry(WorkflowHistory history) {
        historyEntries.add(history);
        history.setInstance(this);
    }

    /**
     * Update the current state
     */
    public void updateCurrentState(WorkflowState newState) {
        this.currentState = newState;
    }

    /**
     * Start the instance
     */
    public void start() {
        this.instanceStatus = InstanceStatus.RUNNING;
        this.startDate = LocalDateTime.now();
    }

    /**
     * Complete the instance
     */
    public void complete() {
        this.instanceStatus = InstanceStatus.COMPLETED;
        this.endDate = LocalDateTime.now();
        this.actualCompletionDate = LocalDateTime.now();
    }

    /**
     * Pause the instance
     */
    public void pause() {
        this.instanceStatus = InstanceStatus.PAUSED;
    }

    /**
     * Resume the instance
     */
    public void resume() {
        this.instanceStatus = InstanceStatus.RUNNING;
    }

    /**
     * Cancel the instance
     */
    public void cancel() {
        this.instanceStatus = InstanceStatus.CANCELLED;
        this.endDate = LocalDateTime.now();
    }

    /**
     * Mark as error
     */
    public void markError(String errorMessage) {
        this.instanceStatus = InstanceStatus.ERROR;
        this.errorMessage = errorMessage;
        this.endDate = LocalDateTime.now();
    }

    /**
     * Check if overdue
     */
    public void checkOverdue() {
        if (expectedCompletionDate != null &&
            LocalDateTime.now().isAfter(expectedCompletionDate) &&
            !instanceStatus.isTerminal()) {
            this.isOverdue = true;
            if (this.overdueSince == null) {
                this.overdueSince = LocalDateTime.now();
            }
        }
    }
}
