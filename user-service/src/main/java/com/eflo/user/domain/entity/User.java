package com.eflo.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"businessUnits", "roles", "hierarchiesAsEmployee", "hierarchiesAsManager"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keycloak integration
    @NotNull
    @Column(name = "keycloak_id", nullable = false, unique = true)
    private UUID keycloakId;

    @NotBlank
    @Column(name = "keycloak_username", nullable = false, unique = true)
    private String keycloakUsername;

    // Employee identification
    @NotBlank
    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    private String employeeNumber;

    @Column(name = "user_ipn", unique = true, length = 50)
    private String userIpn;

    // Personal information
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    // full_name is a generated column in database
    @Column(name = "full_name", insertable = false, updatable = false)
    private String fullName;

    // Contact information
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "office_extension", length = 10)
    private String officeExtension;

    // Professional information
    @Column(name = "job_title")
    private String jobTitle;

    @Column(length = 100)
    private String department;

    // Address
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    @Builder.Default
    private String country = "FR";

    // Employment
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    // Login tracking
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_count")
    @Builder.Default
    private Integer loginCount = 0;

    // Profile
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Preferences (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences")
    @Builder.Default
    private String preferences = "{}";

    // Metadata
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "synced_from_keycloak_at")
    private LocalDateTime syncedFromKeycloakAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserBusinessUnit> businessUnits = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Hierarchy> hierarchiesAsEmployee = new HashSet<>();

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Hierarchy> hierarchiesAsManager = new HashSet<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (loginCount == null) {
            loginCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount = (this.loginCount != null ? this.loginCount : 0) + 1;
    }

    public void deactivate() {
        this.isActive = false;
        this.terminationDate = LocalDate.now();
    }

    public void reactivate() {
        this.isActive = true;
        this.terminationDate = null;
    }

    public void markSyncedFromKeycloak() {
        this.syncedFromKeycloakAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.isActive != null && this.isActive;
    }

    public Set<UserBusinessUnit> getUserBusinessUnits() {
        return this.businessUnits;
    }

    public String getUsername() {
        return this.keycloakUsername;
    }

    public String getPhone() {
        return this.phoneNumber;
    }

    /**
     * Get RRF code from user's primary business unit
     * @return RRF code or null if no primary BU
     */
    public String getRrf() {
        if (businessUnits == null || businessUnits.isEmpty()) {
            return null;
        }

        return businessUnits.stream()
                .filter(ubu -> ubu.getIsPrimary() != null && ubu.getIsPrimary())
                .filter(ubu -> ubu.getBusinessUnit() != null)
                .map(ubu -> ubu.getBusinessUnit().getRrfCode())
                .findFirst()
                .orElse(null);
    }

    /**
     * Get primary business unit ID
     * @return Business unit ID or null
     */
    public Long getPrimaryBusinessUnitId() {
        if (businessUnits == null || businessUnits.isEmpty()) {
            return null;
        }

        return businessUnits.stream()
                .filter(ubu -> ubu.getIsPrimary() != null && ubu.getIsPrimary())
                .filter(ubu -> ubu.getBusinessUnit() != null)
                .map(ubu -> ubu.getBusinessUnit().getId())
                .findFirst()
                .orElse(null);
    }
}
