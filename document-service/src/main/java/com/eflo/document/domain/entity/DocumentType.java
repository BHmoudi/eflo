package com.eflo.document.domain.entity;

import com.eflo.document.domain.enums.DocumentCategory;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "document_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, unique = true, length = 100)
    private String typeCode;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private DocumentCategory category;

    // Mandatory Settings
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @Column(name = "min_documents")
    private Integer minDocuments = 0;

    @Column(name = "max_documents")
    private Integer maxDocuments = 10;

    // File Settings
    @Column(name = "allowed_formats", columnDefinition = "text[]")
    @Builder.Default
    private List<String> allowedFormats = new ArrayList<>();

    @Column(name = "max_file_size_mb", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxFileSizeMb = BigDecimal.valueOf(10.0);

    // Validation Settings
    @Column(name = "requires_validation", nullable = false)
    private Boolean requiresValidation = true;

    @Column(name = "validator_roles", columnDefinition = "text[]")
    @Builder.Default
    private List<String> validatorRoles = new ArrayList<>();

    @Type(JsonBinaryType.class)
    @Column(name = "auto_validate_conditions", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> autoValidateConditions = new HashMap<>();

    // Expiration Settings
    @Column(name = "has_expiration", nullable = false)
    private Boolean hasExpiration = false;

    @Column(name = "expiration_warning_days")
    private Integer expirationWarningDays = 30;

    @Column(name = "default_validity_days")
    private Integer defaultValidityDays;

    // Business Rules
    @Type(JsonBinaryType.class)
    @Column(name = "custom_validation_rules", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> customValidationRules = new HashMap<>();

    @Type(JsonBinaryType.class)
    @Column(name = "metadata_schema", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadataSchema = new HashMap<>();

    // Configuration
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Audit Fields
    @Column(name = "business_unit_id")
    private Long businessUnitId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isFileFormatAllowed(String format) {
        if (allowedFormats == null || allowedFormats.isEmpty()) {
            return true;
        }
        return allowedFormats.stream()
                .anyMatch(allowedFormat -> allowedFormat.equalsIgnoreCase(format));
    }

    public boolean isFileSizeAllowed(long fileSizeBytes) {
        long maxSizeBytes = maxFileSizeMb.longValue() * 1024 * 1024;
        return fileSizeBytes <= maxSizeBytes;
    }

    public boolean canUserValidate(String role) {
        if (validatorRoles == null || validatorRoles.isEmpty()) {
            return true;
        }
        return validatorRoles.contains(role);
    }
}
