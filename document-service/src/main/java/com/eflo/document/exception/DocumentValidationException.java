package com.eflo.document.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when document validation fails.
 * Contains validation errors and details about why the validation failed.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
public class DocumentValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long documentId;
    private final String documentUuid;
    private final List<String> validationErrors;
    private final String validationContext;

    /**
     * Constructs a new DocumentValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public DocumentValidationException(String message) {
        super(message);
        this.documentId = null;
        this.documentUuid = null;
        this.validationErrors = new ArrayList<>();
        this.validationContext = null;
    }

    /**
     * Constructs a new DocumentValidationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DocumentValidationException(String message, Throwable cause) {
        super(message, cause);
        this.documentId = null;
        this.documentUuid = null;
        this.validationErrors = new ArrayList<>();
        this.validationContext = null;
    }

    /**
     * Constructs a new DocumentValidationException with validation errors.
     *
     * @param message the detail message
     * @param validationErrors list of validation error messages
     */
    public DocumentValidationException(String message, List<String> validationErrors) {
        super(message);
        this.documentId = null;
        this.documentUuid = null;
        this.validationErrors = validationErrors != null ? new ArrayList<>(validationErrors) : new ArrayList<>();
        this.validationContext = null;
    }

    /**
     * Constructs a new DocumentValidationException with document ID and validation errors.
     *
     * @param message the detail message
     * @param documentId the ID of the document that failed validation
     * @param validationErrors list of validation error messages
     */
    public DocumentValidationException(String message, Long documentId, List<String> validationErrors) {
        super(message);
        this.documentId = documentId;
        this.documentUuid = null;
        this.validationErrors = validationErrors != null ? new ArrayList<>(validationErrors) : new ArrayList<>();
        this.validationContext = null;
    }

    /**
     * Constructs a new DocumentValidationException with all details.
     *
     * @param message the detail message
     * @param documentId the ID of the document that failed validation
     * @param validationErrors list of validation error messages
     * @param validationContext the context where validation failed
     */
    public DocumentValidationException(String message, Long documentId, List<String> validationErrors, String validationContext) {
        super(message);
        this.documentId = documentId;
        this.documentUuid = null;
        this.validationErrors = validationErrors != null ? new ArrayList<>(validationErrors) : new ArrayList<>();
        this.validationContext = validationContext;
    }

    /**
     * Gets the document ID that failed validation.
     *
     * @return the document ID
     */
    public Long getDocumentId() {
        return documentId;
    }

    /**
     * Gets the document UUID that failed validation.
     *
     * @return the document UUID
     */
    public String getDocumentUuid() {
        return documentUuid;
    }

    /**
     * Gets the list of validation errors.
     *
     * @return list of validation error messages
     */
    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }

    /**
     * Gets the validation context.
     *
     * @return the validation context
     */
    public String getValidationContext() {
        return validationContext;
    }

    /**
     * Checks if there are validation errors.
     *
     * @return true if validation errors exist
     */
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (documentId != null || documentUuid != null || !validationErrors.isEmpty() || validationContext != null) {
            sb.append(" [");
            if (documentId != null) {
                sb.append("documentId=").append(documentId);
            }
            if (documentUuid != null) {
                if (documentId != null) sb.append(", ");
                sb.append("documentUuid=").append(documentUuid);
            }
            if (validationContext != null) {
                if (documentId != null || documentUuid != null) sb.append(", ");
                sb.append("context=").append(validationContext);
            }
            if (!validationErrors.isEmpty()) {
                if (documentId != null || documentUuid != null || validationContext != null) sb.append(", ");
                sb.append("errors=").append(validationErrors);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
