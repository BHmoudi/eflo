package com.eflo.workflow.domain.entity;

import com.eflo.workflow.domain.enums.StateType;
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
 * WorkflowState Entity
 *
 * Represents a state definition within a workflow process.
 * States define the stages through which a workflow instance progresses.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_states",
       uniqueConstraints = @UniqueConstraint(name = "unique_state_code_per_process",
                                            columnNames = {"process_id", "state_code"}),
       indexes = {
    @Index(name = "idx_workflow_states_process", columnList = "process_id"),
    @Index(name = "idx_workflow_states_code", columnList = "state_code"),
    @Index(name = "idx_workflow_states_type", columnList = "state_type"),
    @Index(name = "idx_workflow_states_order", columnList = "process_id, state_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private WorkflowProcess process;

    @Column(name = "state_code", nullable = false, length = 50)
    private String stateCode;

    @Column(name = "state_name", nullable = false)
    private String stateName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "state_order", nullable = false)
    private Integer stateOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_type", nullable = false, length = 20)
    @Builder.Default
    private StateType stateType = StateType.NORMAL;

    @Column(name = "expected_duration_hours")
    private Integer expectedDurationHours;

    @Column(name = "is_final_state", nullable = false)
    @Builder.Default
    private Boolean isFinalState = false;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(name = "allow_skip", nullable = false)
    @Builder.Default
    private Boolean allowSkip = false;

    @Type(JsonBinaryType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @JsonIgnore
    @OneToMany(mappedBy = "fromState", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowTransition> outgoingTransitions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "toState", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkflowTransition> incomingTransitions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "state", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowStateTask> tasks = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Add a task to this state
     */
    public void addTask(WorkflowStateTask task) {
        tasks.add(task);
        task.setState(this);
    }

    /**
     * Remove a task from this state
     */
    public void removeTask(WorkflowStateTask task) {
        tasks.remove(task);
        task.setState(null);
    }

    /**
     * Add an outgoing transition
     */
    public void addOutgoingTransition(WorkflowTransition transition) {
        outgoingTransitions.add(transition);
        transition.setFromState(this);
    }
}
