package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.DocumentCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new document type.
 * Contains all fields required to define a new document type configuration.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeCreateRequest {

    /**
     * Unique code identifying the document type.
     * Must be unique across all document types.
     */
    @NotBlank(message = "Type code is required")
    @Size(max = 100, message = "Type code must not exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Type code must contain only uppercase letters, numbers, and underscores")
    private String typeCode;

    /**
     * Human-readable name of the document type.
     */
    @NotBlank(message = "Type name is required")
    @Size(max = 200, message = "Type name must not exceed 200 characters")
    private String typeName;

    /**
     * Description of the document type and its purpose.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Category to which this document type belongs.
     */
    @NotNull(message = "Category is required")
    private DocumentCategory category;

    /**
     * Flag indicating if this document type is mandatory for orders.
     */
    @NotNull(message = "Mandatory flag is required")
    @Builder.Default
    private Boolean isMandatory = false;

    /**
     * Minimum number of documents required for this type.
     */
    @Min(value = 0, message = "Minimum documents must be 0 or greater")
    @Max(value = 100, message = "Minimum documents must not exceed 100")
    @Builder.Default
    private Integer minDocuments = 0;

    /**
     * Maximum number of documents allowed for this type.
     */
    @Min(value = 1, message = "Maximum documents must be at least 1")
    @Max(value = 100, message = "Maximum documents must not exceed 100")
    @Builder.Default
    private Integer maxDocuments = 10;

    /**
     * List of allowed file formats (e.g., PDF, JPG, PNG).
     */
    @NotEmpty(message = "At least one allowed format is required")
    @Size(max = 20, message = "Cannot specify more than 20 allowed formats")
    private List<@NotBlank @Size(max = 10) String> allowedFormats;

    /**
     * Maximum file size allowed in megabytes.
     */
    @NotNull(message = "Maximum file size is required")
    @DecimalMin(value = "0.1", message = "Maximum file size must be at least 0.1 MB")
    @DecimalMax(value = "100.0", message = "Maximum file size must not exceed 100 MB")
    @Builder.Default
    private BigDecimal maxFileSizeMb = BigDecimal.valueOf(10.0);

    /**
     * Flag indicating if documents of this type require validation.
     */
    @NotNull(message = "Requires validation flag is required")
    @Builder.Default
    private Boolean requiresValidation = true;

    /**
     * List of roles authorized to validate documents of this type.
     */
    private List<@NotBlank @Size(max = 100) String> validatorRoles;

    /**
     * Conditions for automatic validation (JSON structure).
     */
    private Map<String, Object> autoValidateConditions;

    /**
     * Flag indicating if documents of this type have expiration dates.
     */
    @NotNull(message = "Has expiration flag is required")
    @Builder.Default
    private Boolean hasExpiration = false;

    /**
     * Number of days before expiration to send warning notifications.
     */
    @Min(value = 1, message = "Expiration warning days must be at least 1")
    @Max(value = 365, message = "Expiration warning days must not exceed 365")
    private Integer expirationWarningDays;

    /**
     * Default validity period in days for documents of this type.
     */
    @Min(value = 1, message = "Default validity days must be at least 1")
    @Max(value = 3650, message = "Default validity days must not exceed 3650 (10 years)")
    private Integer defaultValidityDays;

    /**
     * Custom validation rules (JSON structure).
     */
    private Map<String, Object> customValidationRules;

    /**
     * Metadata schema defining expected metadata fields (JSON schema).
     */
    private Map<String, Object> metadataSchema;

    /**
     * Flag indicating if this document type is active.
     */
    @NotNull(message = "Active flag is required")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Display order for UI presentation.
     */
    @Min(value = 0, message = "Display order must be 0 or greater")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Business unit ID for multi-tenant scenarios.
     */
    @Positive(message = "Business unit ID must be positive")
    private Long businessUnitId;

    /**
     * Username or identifier of the person creating this document type.
     */
    @NotBlank(message = "Created by is required")
    @Size(max = 255, message = "Created by must not exceed 255 characters")
    private String createdBy;
}
