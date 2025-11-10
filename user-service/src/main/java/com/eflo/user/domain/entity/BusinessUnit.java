package com.eflo.user.domain.entity;

import com.eflo.user.domain.enums.BusinessUnitType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "business_units")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"userAssignments", "hierarchies"})
public class BusinessUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Business unit identification
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(name = "legal_name")
    private String legalName;

    // Type
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private BusinessUnitType type;

    // Regional organization
    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "region_name", length = 100)
    private String regionName;

    @Column(name = "rrf_code", length = 50)
    private String rrfCode;

    // Contact information
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "fax_number", length = 20)
    private String faxNumber;

    private String email;

    private String website;

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

    // GPS coordinates
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    // Operating hours (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opening_hours")
    @Builder.Default
    private String openingHours = "{}";

    // Manager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "manager_name")
    private String managerName;

    // Financial
    @Column(length = 14)
    private String siret;

    @Column(name = "vat_number", length = 20)
    private String vatNumber;

    // Brand affiliations
    @Column(columnDefinition = "varchar[]")
    @Builder.Default
    private String[] brands = new String[]{};

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    // Settings (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings")
    @Builder.Default
    private String settings = "{}";

    // Metadata
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "businessUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserBusinessUnit> userAssignments = new HashSet<>();

    @OneToMany(mappedBy = "businessUnit", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Hierarchy> hierarchies = new HashSet<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void deactivate() {
        this.isActive = false;
        this.closingDate = LocalDate.now();
    }

    public void reactivate() {
        this.isActive = true;
        this.closingDate = null;
    }

    public void setManager(User user) {
        this.manager = user;
        if (user != null) {
            this.managerName = user.getFirstName() + " " + user.getLastName();
        } else {
            this.managerName = null;
        }
    }
}
