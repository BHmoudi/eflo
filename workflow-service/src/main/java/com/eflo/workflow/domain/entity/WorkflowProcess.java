package com.eflo.workflow.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * WorkflowProcess Entity
 *
 * Represents a workflow process definition (template) that defines
 * the structure and flow of a business process.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_processes", indexes = {
    @Index(name = "idx_workflow_processes_code", columnList = "process_code"),
    @Index(name = "idx_workflow_processes_order_type", columnList = "order_type"),
    @Index(name = "idx_workflow_processes_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_code", nullable = false, unique = true, length = 50)
    private String processCode;

    @Column(name = "process_name", nullable = false)
    private String processName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(name = "process_version", nullable = false)
    @Builder.Default
    private Integer processVersion = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "max_duration_days")
    private Integer maxDurationDays;

    @Column(name = "auto_progress_enabled", nullable = false)
    @Builder.Default
    private Boolean autoProgressEnabled = false;

    @Column(name = "parallel_execution_allowed", nullable = false)
    @Builder.Default
    private Boolean parallelExecutionAllowed = false;

    @Type(JsonBinaryType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @JsonIgnore
    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowState> states = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowInstance> instances = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowTransition> transitions = new ArrayList<>();

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
     * Add a state to this process
     */
    public void addState(WorkflowState state) {
        states.add(state);
        state.setProcess(this);
    }

    /**
     * Remove a state from this process
     */
    public void removeState(WorkflowState state) {
        states.remove(state);
        state.setProcess(null);
    }
}
