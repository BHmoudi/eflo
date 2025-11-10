package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "condition_dependency",
    uniqueConstraints = @UniqueConstraint(columnNames = {"condition_id", "depends_on_condition_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id", nullable = false)
    private OrderCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_condition_id", nullable = false)
    private OrderCondition dependsOnCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 50)
    private DependencyType dependencyType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DependencyType {
        REQUIRES,   // Condition A requires Condition B to be present
        EXCLUDES,   // Condition A excludes Condition B (cannot coexist)
        IMPLIES     // Condition A implies Condition B (automatically adds B when A is present)
    }
}
