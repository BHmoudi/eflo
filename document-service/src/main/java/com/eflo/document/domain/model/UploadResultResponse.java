package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for single document upload operations.
 * Contains upload success/failure information and document details.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultResponse {

    /**
     * Flag indicating if the upload was successful.
     */
    private Boolean success;

    /**
     * HTTP status code of the operation.
     */
    private Integer statusCode;

    /**
     * Message describing the upload result.
     */
    private String message;

    /**
     * ID of the uploaded document (if successful).
     */
    private Long documentId;

    /**
     * UUID of the uploaded document (if successful).
     */
    private UUID documentUuid;

    /**
     * Original filename of the uploaded document.
     */
    private String originalFilename;

    /**
     * Stored filename in the system.
     */
    private String storedFilename;

    /**
     * File size in bytes.
     */
    private Long fileSizeBytes;

    /**
     * File size in megabytes.
     */
    private Double fileSizeMB;

    /**
     * Document type code.
     */
    private String typeCode;

    /**
     * Associated order ID.
     */
    private Long orderId;

    /**
     * Associated order number.
     */
    private String orderNumber;

    /**
     * Upload timestamp.
     */
    private LocalDateTime uploadedAt;

    /**
     * User who uploaded the document.
     */
    private String uploadedBy;

    /**
     * Validation result for the upload.
     */
    private ValidationResult validationResult;

    /**
     * Virus scan status information.
     */
    private ScanResult scanResult;

    /**
     * Storage information.
     */
    private StorageInfo storageInfo;

    /**
     * List of errors encountered during upload (if any).
     */
    private List<String> errors;

    /**
     * List of warnings generated during upload (if any).
     */
    private List<String> warnings;

    /**
     * Download URL for the uploaded document (if available).
     */
    private String downloadUrl;

    /**
     * Additional metadata about the upload operation.
     */
    private UploadMetadata uploadMetadata;

    /**
     * Metadata about the upload operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadMetadata {

        /**
         * Processing time in milliseconds.
         */
        private Long processingTimeMs;

        /**
         * Flag indicating if virus scan was performed.
         */
        private Boolean virusScanPerformed;

        /**
         * Flag indicating if automatic validation was performed.
         */
        private Boolean autoValidationPerformed;

        /**
         * Flag indicating if the document was auto-validated.
         */
        private Boolean autoValidated;

        /**
         * Flag indicating if this is a duplicate document.
         */
        private Boolean isDuplicate;

        /**
         * ID of the duplicate document (if duplicate detected).
         */
        private Long duplicateDocumentId;
    }

    /**
     * Check if the upload was successful.
     *
     * @return true if upload succeeded
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * Check if there are any errors.
     *
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if there are any warnings.
     *
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Check if validation passed.
     *
     * @return true if validation passed
     */
    public boolean isValidationPassed() {
        return validationResult != null && validationResult.getIsValid();
    }

    /**
     * Check if virus scan passed.
     *
     * @return true if virus scan passed
     */
    public boolean isScanPassed() {
        return scanResult != null && scanResult.isClean();
    }

    /**
     * Create a success response.
     *
     * @param documentId the document ID
     * @param documentUuid the document UUID
     * @param message success message
     * @return UploadResultResponse
     */
    public static UploadResultResponse success(Long documentId, UUID documentUuid, String message) {
        return UploadResultResponse.builder()
                .success(true)
                .statusCode(201)
                .message(message)
                .documentId(documentId)
                .documentUuid(documentUuid)
                .build();
    }

    /**
     * Create a failure response.
     *
     * @param message error message
     * @param errors list of errors
     * @return UploadResultResponse
     */
    public static UploadResultResponse failure(String message, List<String> errors) {
        return UploadResultResponse.builder()
                .success(false)
                .statusCode(400)
                .message(message)
                .errors(errors)
                .build();
    }
}
