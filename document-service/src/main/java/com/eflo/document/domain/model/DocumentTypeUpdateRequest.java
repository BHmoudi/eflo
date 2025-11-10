package com.eflo.document.domain.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating an existing document type.
 * Contains updatable fields for document type configuration.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeUpdateRequest {

    /**
     * Updated human-readable name of the document type.
     */
    @Size(max = 200, message = "Type name must not exceed 200 characters")
    private String typeName;

    /**
     * Updated description of the document type.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Updated mandatory flag.
     */
    private Boolean isMandatory;

    /**
     * Updated minimum number of documents required.
     */
    @Min(value = 0, message = "Minimum documents must be 0 or greater")
    @Max(value = 100, message = "Minimum documents must not exceed 100")
    private Integer minDocuments;

    /**
     * Updated maximum number of documents allowed.
     */
    @Min(value = 1, message = "Maximum documents must be at least 1")
    @Max(value = 100, message = "Maximum documents must not exceed 100")
    private Integer maxDocuments;

    /**
     * Updated list of allowed file formats.
     */
    @Size(max = 20, message = "Cannot specify more than 20 allowed formats")
    private List<@NotBlank @Size(max = 10) String> allowedFormats;

    /**
     * Updated maximum file size in megabytes.
     */
    @DecimalMin(value = "0.1", message = "Maximum file size must be at least 0.1 MB")
    @DecimalMax(value = "100.0", message = "Maximum file size must not exceed 100 MB")
    private BigDecimal maxFileSizeMb;

    /**
     * Updated validation requirement flag.
     */
    private Boolean requiresValidation;

    /**
     * Updated list of validator roles.
     */
    private List<@NotBlank @Size(max = 100) String> validatorRoles;

    /**
     * Updated automatic validation conditions.
     */
    private Map<String, Object> autoValidateConditions;

    /**
     * Updated expiration flag.
     */
    private Boolean hasExpiration;

    /**
     * Updated expiration warning days.
     */
    @Min(value = 1, message = "Expiration warning days must be at least 1")
    @Max(value = 365, message = "Expiration warning days must not exceed 365")
    private Integer expirationWarningDays;

    /**
     * Updated default validity period in days.
     */
    @Min(value = 1, message = "Default validity days must be at least 1")
    @Max(value = 3650, message = "Default validity days must not exceed 3650")
    private Integer defaultValidityDays;

    /**
     * Updated custom validation rules.
     */
    private Map<String, Object> customValidationRules;

    /**
     * Updated metadata schema.
     */
    private Map<String, Object> metadataSchema;

    /**
     * Updated active status.
     */
    private Boolean isActive;

    /**
     * Updated display order.
     */
    @Min(value = 0, message = "Display order must be 0 or greater")
    private Integer displayOrder;

    /**
     * Username or identifier of the person updating this document type.
     */
    @NotBlank(message = "Updated by is required")
    @Size(max = 255, message = "Updated by must not exceed 255 characters")
    private String updatedBy;
}
