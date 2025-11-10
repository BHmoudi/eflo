package com.eflo.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow Role entity
 * Defines roles for workflow task assignment and approvals (CDV, CDR, Commercial, etc.)
 */
@Entity
@Table(name = "workflow_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", unique = true, nullable = false, length = 50)
    private String roleCode;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Hierarchy level where 1 is highest authority (e.g., 1=CDR, 2=CDV, 3=Commercial)
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowUserRole> userRoles = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "parentRole", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkflowRoleHierarchy> childRoles = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "childRole", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkflowRoleHierarchy> parentRoles = new ArrayList<>();

    // Helper methods
    public boolean isHigherThan(WorkflowRole other) {
        return this.level < other.level;
    }

    public boolean isLowerThan(WorkflowRole other) {
        return this.level > other.level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowRole)) return false;
        WorkflowRole that = (WorkflowRole) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkflowRole{" +
                "id=" + id +
                ", roleCode='" + roleCode + '\'' +
                ", roleName='" + roleName + '\'' +
                ", level=" + level +
                '}';
    }
}
