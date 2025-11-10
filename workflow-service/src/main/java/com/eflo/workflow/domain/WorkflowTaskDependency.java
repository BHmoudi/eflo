package com.eflo.workflow.domain;

import com.eflo.workflow.domain.entity.WorkflowStateTask;
import com.eflo.workflow.domain.enums.DependencyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WorkflowTaskDependency Entity
 * Maps to workflow_task_dependencies table
 * Defines dependencies between workflow tasks
 */
@Entity
@Table(name = "workflow_task_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_task_id", nullable = false)
    private WorkflowStateTask dependentTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_task_id", nullable = false)
    private WorkflowStateTask requiredTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 30)
    private DependencyType dependencyType;

    @Column(name = "required_status", length = 20)
    private String requiredStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowTaskDependency)) return false;
        WorkflowTaskDependency that = (WorkflowTaskDependency) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowTaskDependency{" +
                "id=" + id +
                ", dependencyType=" + dependencyType +
                ", requiredStatus='" + requiredStatus + '\'' +
                '}';
    }
}
