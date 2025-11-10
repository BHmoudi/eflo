package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.DocumentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO containing document type information.
 * Used for retrieving document type configuration and metadata.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponse {

    /**
     * Internal database ID of the document type.
     */
    private Long id;

    /**
     * Unique code identifying the document type.
     */
    private String typeCode;

    /**
     * Human-readable name of the document type.
     */
    private String typeName;

    /**
     * Description of the document type.
     */
    private String description;

    /**
     * Category to which this document type belongs.
     */
    private DocumentCategory category;

    // Mandatory Settings

    /**
     * Flag indicating if this document type is mandatory.
     */
    private Boolean isMandatory;

    /**
     * Minimum number of documents required.
     */
    private Integer minDocuments;

    /**
     * Maximum number of documents allowed.
     */
    private Integer maxDocuments;

    // File Settings

    /**
     * List of allowed file formats.
     */
    private List<String> allowedFormats;

    /**
     * Maximum file size allowed in megabytes.
     */
    private BigDecimal maxFileSizeMb;

    // Validation Settings

    /**
     * Flag indicating if validation is required.
     */
    private Boolean requiresValidation;

    /**
     * List of roles that can validate this document type.
     */
    private List<String> validatorRoles;

    /**
     * Conditions for automatic validation.
     */
    private Map<String, Object> autoValidateConditions;

    // Expiration Settings

    /**
     * Flag indicating if documents of this type have expiration.
     */
    private Boolean hasExpiration;

    /**
     * Number of days before expiration to send warnings.
     */
    private Integer expirationWarningDays;

    /**
     * Default validity period in days.
     */
    private Integer defaultValidityDays;

    // Business Rules

    /**
     * Custom validation rules.
     */
    private Map<String, Object> customValidationRules;

    /**
     * Metadata schema for documents of this type.
     */
    private Map<String, Object> metadataSchema;

    // Configuration

    /**
     * Flag indicating if this document type is active.
     */
    private Boolean isActive;

    /**
     * Display order for UI presentation.
     */
    private Integer displayOrder;

    // Business Context

    /**
     * Business unit ID.
     */
    private Long businessUnitId;

    // Audit Information

    /**
     * User who created the document type.
     */
    private String createdBy;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * User who last updated the document type.
     */
    private String updatedBy;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;

    // Statistics (optional computed fields)

    /**
     * Total count of documents of this type.
     */
    private Long documentCount;

    /**
     * Count of active documents of this type.
     */
    private Long activeDocumentCount;

    /**
     * Count of pending documents of this type.
     */
    private Long pendingDocumentCount;
}
