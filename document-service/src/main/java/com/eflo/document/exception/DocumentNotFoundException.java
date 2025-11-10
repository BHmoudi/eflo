package com.eflo.document.exception;

/**
 * Exception thrown when a requested document cannot be found.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
public class DocumentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long documentId;
    private final String documentUuid;

    /**
     * Constructs a new DocumentNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public DocumentNotFoundException(String message) {
        super(message);
        this.documentId = null;
        this.documentUuid = null;
    }

    /**
     * Constructs a new DocumentNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.documentId = null;
        this.documentUuid = null;
    }

    /**
     * Constructs a new DocumentNotFoundException with document ID.
     *
     * @param message the detail message
     * @param documentId the ID of the document that was not found
     */
    public DocumentNotFoundException(String message, Long documentId) {
        super(message);
        this.documentId = documentId;
        this.documentUuid = null;
    }

    /**
     * Constructs a new DocumentNotFoundException with document UUID.
     *
     * @param message the detail message
     * @param documentUuid the UUID of the document that was not found
     */
    public DocumentNotFoundException(String message, String documentUuid) {
        super(message);
        this.documentId = null;
        this.documentUuid = documentUuid;
    }

    /**
     * Gets the document ID that was not found.
     *
     * @return the document ID
     */
    public Long getDocumentId() {
        return documentId;
    }

    /**
     * Gets the document UUID that was not found.
     *
     * @return the document UUID
     */
    public String getDocumentUuid() {
        return documentUuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (documentId != null || documentUuid != null) {
            sb.append(" [");
            if (documentId != null) {
                sb.append("documentId=").append(documentId);
            }
            if (documentUuid != null) {
                if (documentId != null) sb.append(", ");
                sb.append("documentUuid=").append(documentUuid);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
