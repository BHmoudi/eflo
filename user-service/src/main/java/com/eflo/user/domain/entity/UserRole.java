package com.eflo.user.domain.entity;

import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "businessUnit"})
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 50)
    private UserRoleEnum roleName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RoleSource source = RoleSource.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id")
    private BusinessUnit businessUnit;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "assigned_at")
    private LocalDate assignedAt;

    @Column(name = "revoked_at")
    private LocalDate revokedAt;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "revoked_by")
    private String revokedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Metadata
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "synced_from_keycloak_at")
    private LocalDateTime syncedFromKeycloakAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (assignedAt == null && isActive) {
            assignedAt = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void revoke(String revokedBy) {
        this.isActive = false;
        this.revokedAt = LocalDate.now();
        this.revokedBy = revokedBy;
    }

    public void reactivate(String assignedBy) {
        this.isActive = true;
        this.revokedAt = null;
        this.revokedBy = null;
        this.assignedBy = assignedBy;
    }

    public void markSyncedFromKeycloak() {
        this.syncedFromKeycloakAt = LocalDateTime.now();
    }
}
