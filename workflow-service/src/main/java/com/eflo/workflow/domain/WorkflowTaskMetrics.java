package com.eflo.workflow.domain;

import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * WorkflowTaskMetrics Entity
 * Maps to workflow_task_metrics table
 * Stores performance metrics and analytics for task instances
 */
@Entity
@Table(name = "workflow_task_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_task_id", nullable = false, unique = true)
    private WorkflowInstanceTask instanceTask;

    @Column(name = "assigned_duration_seconds")
    private Long assignedDurationSeconds;

    @Column(name = "execution_duration_seconds")
    private Long executionDurationSeconds;

    @Column(name = "total_duration_seconds")
    private Long totalDurationSeconds;

    @Column(name = "approval_duration_seconds")
    private Long approvalDurationSeconds;

    @Column(name = "number_of_reassignments")
    @Builder.Default
    private Integer numberOfReassignments = 0;

    @Column(name = "number_of_escalations")
    @Builder.Default
    private Integer numberOfEscalations = 0;

    @Column(name = "number_of_delegations")
    @Builder.Default
    private Integer numberOfDelegations = 0;

    @Column(name = "sla_met")
    private Boolean slaMet;

    @Column(name = "sla_breach_hours")
    private Double slaBreachHours;

    @Column(name = "first_response_time_seconds")
    private Long firstResponseTimeSeconds;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    public void incrementReassignments() {
        this.numberOfReassignments++;
    }

    public void incrementEscalations() {
        this.numberOfEscalations++;
    }

    public void incrementDelegations() {
        this.numberOfDelegations++;
    }

    public void markCalculated() {
        this.calculatedAt = LocalDateTime.now();
    }

    public boolean hasBreachedSla() {
        return Boolean.FALSE.equals(slaMet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTaskMetrics)) return false;
        WorkflowTaskMetrics that = (WorkflowTaskMetrics) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowTaskMetrics{" +
                "id=" + id +
                ", totalDurationSeconds=" + totalDurationSeconds +
                ", numberOfReassignments=" + numberOfReassignments +
                ", numberOfEscalations=" + numberOfEscalations +
                ", numberOfDelegations=" + numberOfDelegations +
                ", slaMet=" + slaMet +
                '}';
    }
}
