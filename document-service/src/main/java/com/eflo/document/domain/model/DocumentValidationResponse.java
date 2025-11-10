package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for document validation operations.
 * Contains the validation result and updated document status.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentValidationResponse {

    /**
     * Internal database ID of the validated document.
     */
    private Long documentId;

    /**
     * UUID of the validated document.
     */
    private UUID documentUuid;

    /**
     * Flag indicating if the validation was successful.
     */
    private Boolean validationSuccessful;

    /**
     * Flag indicating if the document was approved.
     */
    private Boolean isApproved;

    /**
     * Updated status of the document after validation.
     */
    private DocumentStatus status;

    /**
     * Timestamp when validation was performed.
     */
    private LocalDateTime validatedAt;

    /**
     * User who performed the validation.
     */
    private String validatedBy;

    /**
     * Comments or reasons for the validation decision.
     */
    private String validationComments;

    /**
     * Status reason (especially important for rejections).
     */
    private String statusReason;

    /**
     * Validation result details.
     */
    private ValidationResult validationResult;

    /**
     * Original filename of the validated document.
     */
    private String originalFilename;

    /**
     * Document type code.
     */
    private String typeCode;

    /**
     * Associated order number.
     */
    private String orderNumber;

    /**
     * Additional message about the validation outcome.
     */
    private String message;

    /**
     * Check if the validation resulted in approval.
     *
     * @return true if document was approved
     */
    public boolean wasApproved() {
        return Boolean.TRUE.equals(isApproved) && status == DocumentStatus.VALIDATED;
    }

    /**
     * Check if the validation resulted in rejection.
     *
     * @return true if document was rejected
     */
    public boolean wasRejected() {
        return Boolean.FALSE.equals(isApproved) && status == DocumentStatus.REJECTED;
    }

    /**
     * Check if the validation encountered errors.
     *
     * @return true if there were validation errors
     */
    public boolean hasErrors() {
        return validationResult != null && validationResult.hasErrors();
    }

    /**
     * Check if the validation has warnings.
     *
     * @return true if there were validation warnings
     */
    public boolean hasWarnings() {
        return validationResult != null && validationResult.hasWarnings();
    }
}
