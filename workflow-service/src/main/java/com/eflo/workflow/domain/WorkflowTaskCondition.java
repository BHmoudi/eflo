package com.eflo.workflow.domain;

import com.eflo.workflow.domain.entity.WorkflowStateTask;
import com.eflo.workflow.domain.enums.ConditionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WorkflowTaskCondition Entity
 * Maps to workflow_task_conditions table
 * Defines conditions that must be met for tasks to be activated or executed
 */
@Entity
@Table(name = "workflow_task_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private WorkflowStateTask task;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    private ConditionType conditionType;

    @Column(name = "condition_field", length = 100)
    private String conditionField;

    @Column(name = "condition_operator", length = 50)
    private String conditionOperator;

    @Column(name = "condition_value", columnDefinition = "TEXT")
    private String conditionValue;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTaskCondition)) return false;
        WorkflowTaskCondition that = (WorkflowTaskCondition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowTaskCondition{" +
                "id=" + id +
                ", conditionType=" + conditionType +
                ", conditionField='" + conditionField + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
