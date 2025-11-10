package com.eflo.workflow.domain.entity;

import com.eflo.workflow.domain.enums.EventType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WorkflowHistory Entity
 *
 * Complete audit trail of all workflow changes and events.
 * Provides full traceability and compliance tracking.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_history", indexes = {
    @Index(name = "idx_workflow_history_instance", columnList = "instance_id"),
    @Index(name = "idx_workflow_history_event_type", columnList = "event_type"),
    @Index(name = "idx_workflow_history_created_at", columnList = "created_at"),
    @Index(name = "idx_workflow_history_user", columnList = "user_id"),
    @Index(name = "idx_workflow_history_states", columnList = "from_state_id, to_state_id"),
    @Index(name = "idx_workflow_history_task", columnList = "task_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private WorkflowInstance instance;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_state_id")
    private WorkflowState fromState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_state_id")
    private WorkflowState toState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_id")
    private WorkflowTransition transition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private WorkflowInstanceTask task;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Type(JsonBinaryType.class)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Create a history entry for state change
     */
    public static WorkflowHistory forStateChange(WorkflowInstance instance,
                                                  WorkflowState fromState,
                                                  WorkflowState toState,
                                                  WorkflowTransition transition,
                                                  Long userId,
                                                  String userName) {
        return WorkflowHistory.builder()
                .instance(instance)
                .eventType(EventType.STATE_CHANGED)
                .eventDescription(String.format("State changed from %s to %s",
                                              fromState != null ? fromState.getStateName() : "START",
                                              toState.getStateName()))
                .fromState(fromState)
                .toState(toState)
                .transition(transition)
                .userId(userId)
                .userName(userName)
                .build();
    }

    /**
     * Create a history entry for task event
     */
    public static WorkflowHistory forTaskEvent(WorkflowInstance instance,
                                               WorkflowInstanceTask task,
                                               EventType eventType,
                                               String description,
                                               Long userId,
                                               String userName) {
        return WorkflowHistory.builder()
                .instance(instance)
                .task(task)
                .eventType(eventType)
                .eventDescription(description)
                .userId(userId)
                .userName(userName)
                .build();
    }

    /**
     * Create a history entry for instance event
     */
    public static WorkflowHistory forInstanceEvent(WorkflowInstance instance,
                                                   EventType eventType,
                                                   String description,
                                                   Long userId,
                                                   String userName) {
        return WorkflowHistory.builder()
                .instance(instance)
                .eventType(eventType)
                .eventDescription(description)
                .userId(userId)
                .userName(userName)
                .build();
    }
}
