package com.eflo.document.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all document events
 * Uses Jackson polymorphic type handling for proper serialization/deserialization
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DocumentEvent.DocumentUploadedEvent.class, name = "DOCUMENT_UPLOADED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentValidatedEvent.class, name = "DOCUMENT_VALIDATED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentRejectedEvent.class, name = "DOCUMENT_REJECTED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentExpiredEvent.class, name = "DOCUMENT_EXPIRED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentDeletedEvent.class, name = "DOCUMENT_DELETED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentArchivedEvent.class, name = "DOCUMENT_ARCHIVED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentVersionCreatedEvent.class, name = "DOCUMENT_VERSION_CREATED"),
    @JsonSubTypes.Type(value = DocumentEvent.DocumentScanCompletedEvent.class, name = "DOCUMENT_SCAN_COMPLETED"),
    @JsonSubTypes.Type(value = DocumentEvent.AllDocumentsValidatedEvent.class, name = "ALL_DOCUMENTS_VALIDATED")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class DocumentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique event identifier
     */
    private String eventId;

    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;

    /**
     * Type of the event
     */
    private String eventType;

    /**
     * Document ID associated with this event
     */
    private Long documentId;

    /**
     * Document UUID
     */
    private UUID documentUuid;

    /**
     * Order ID associated with the document
     */
    private Long orderId;

    /**
     * Order number for easier tracking
     */
    private String orderNumber;

    /**
     * User or system that triggered the event
     */
    private String triggeredBy;

    /**
     * Additional metadata about the event
     */
    private Map<String, Object> metadata;

    /**
     * Event: Document Uploaded
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentUploadedEvent extends DocumentEvent {
        private String documentType;
        private String fileName;
        private String mimeType;
        private Long fileSizeBytes;
        private String storagePath;
        private String uploadedBy;
        private Boolean requiresValidation;
    }

    /**
     * Event: Document Validated
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentValidatedEvent extends DocumentEvent {
        private String documentType;
        private String validatedBy;
        private LocalDateTime validatedAt;
        private String validationComments;
        private String previousStatus;
    }

    /**
     * Event: Document Rejected
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentRejectedEvent extends DocumentEvent {
        private String documentType;
        private String rejectedBy;
        private String rejectionReason;
        private LocalDateTime rejectedAt;
        private String previousStatus;
        private Boolean requiresReupload;
    }

    /**
     * Event: Document Expired
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentExpiredEvent extends DocumentEvent {
        private String documentType;
        private LocalDateTime expirationDate;
        private String previousStatus;
        private Boolean wasNotified;
        private LocalDateTime notificationSentAt;
        private Boolean requiresRenewal;
    }

    /**
     * Event: Document Deleted
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentDeletedEvent extends DocumentEvent {
        private String documentType;
        private String deletedBy;
        private LocalDateTime deletedAt;
        private String deletionReason;
        private Boolean isSoftDelete;
        private String previousStatus;
    }

    /**
     * Event: Document Archived
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentArchivedEvent extends DocumentEvent {
        private String documentType;
        private String archivedBy;
        private LocalDateTime archivedAt;
        private String archiveReason;
        private String archiveLocation;
        private String previousStatus;
    }

    /**
     * Event: Document Version Created
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentVersionCreatedEvent extends DocumentEvent {
        private String documentType;
        private Long parentDocumentId;
        private UUID parentDocumentUuid;
        private Integer oldVersion;
        private Integer newVersion;
        private String versionCreatedBy;
        private String versionReason;
        private Boolean isPreviousVersionArchived;
    }

    /**
     * Event: Document Scan Completed
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class DocumentScanCompletedEvent extends DocumentEvent {
        private String documentType;
        private String scanType;
        private String scanStatus;
        private LocalDateTime scanCompletedAt;
        private Boolean isSafe;
        private String threatDetected;
        private Map<String, Object> scanResult;
        private String actionTaken;
    }

    /**
     * Event: All Documents Validated for Order
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class AllDocumentsValidatedEvent extends DocumentEvent {
        private Integer totalDocuments;
        private Integer validatedDocuments;
        private LocalDateTime allValidatedAt;
        private String validatedBy;
        private Boolean orderReadyForProcessing;
        private String nextWorkflowStep;
    }

    /**
     * Helper method to create event ID
     */
    public static String generateEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Helper method to get current timestamp
     */
    public static LocalDateTime getCurrentTimestamp() {
        return LocalDateTime.now();
    }
}
