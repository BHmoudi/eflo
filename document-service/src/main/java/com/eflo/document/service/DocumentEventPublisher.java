package com.eflo.document.service;

import com.eflo.document.config.KafkaConfig;
import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.model.DocumentEvent;
import com.eflo.document.scanner.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing document events to Kafka
 * Handles all document-related event publishing with proper error handling and logging
 */
@Service
public class DocumentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DocumentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DocumentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish document uploaded event
     *
     * @param document the uploaded document
     */
    public void publishDocumentUploaded(Document document) {
        try {
            DocumentEvent.DocumentUploadedEvent event = DocumentEvent.DocumentUploadedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_UPLOADED")
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .orderId(document.getOrderId())
                    .orderNumber(document.getOrderNumber())
                    .triggeredBy(document.getUploadedBy())
                    .documentType(document.getTypeCode())
                    .fileName(document.getOriginalFilename())
                    .mimeType(document.getMimeType())
                    .fileSizeBytes(document.getFileSizeBytes())
                    .storagePath(document.getStoragePath())
                    .uploadedBy(document.getUploadedBy())
                    .requiresValidation(document.getDocumentType() != null && document.getDocumentType().getRequiresValidation())
                    .metadata(buildDocumentMetadata(document))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_UPLOADED, document.getOrderNumber(), event);

            log.info("Published document uploaded event - DocumentId: {}, OrderId: {}, Type: {}",
                    document.getId(), document.getOrderId(), document.getTypeCode());

        } catch (Exception e) {
            log.error("Failed to publish document uploaded event for document {}", document.getId(), e);
        }
    }

    /**
     * Publish document validated event
     *
     * @param document    the validated document
     * @param validatedBy user who validated the document
     */
    public void publishDocumentValidated(Document document, String validatedBy) {
        try {
            DocumentEvent.DocumentValidatedEvent event = DocumentEvent.DocumentValidatedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_VALIDATED")
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .orderId(document.getOrderId())
                    .orderNumber(document.getOrderNumber())
                    .triggeredBy(validatedBy)
                    .documentType(document.getTypeCode())
                    .validatedBy(validatedBy)
                    .validatedAt(document.getValidatedAt())
                    .validationComments(document.getValidationComments())
                    .previousStatus(document.getStatus() != null ? document.getStatus().name() : null)
                    .metadata(buildDocumentMetadata(document))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_VALIDATED, document.getOrderNumber(), event);

            log.info("Published document validated event - DocumentId: {}, OrderId: {}, ValidatedBy: {}",
                    document.getId(), document.getOrderId(), validatedBy);

        } catch (Exception e) {
            log.error("Failed to publish document validated event for document {}", document.getId(), e);
        }
    }

    /**
     * Publish document rejected event
     *
     * @param document the rejected document
     * @param reason   rejection reason
     */
    public void publishDocumentRejected(Document document, String reason) {
        try {
            DocumentEvent.DocumentRejectedEvent event = DocumentEvent.DocumentRejectedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_REJECTED")
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .orderId(document.getOrderId())
                    .orderNumber(document.getOrderNumber())
                    .triggeredBy(document.getValidatedBy())
                    .documentType(document.getTypeCode())
                    .rejectedBy(document.getValidatedBy())
                    .rejectionReason(reason)
                    .rejectedAt(document.getValidatedAt())
                    .previousStatus(document.getStatus() != null ? document.getStatus().name() : null)
                    .requiresReupload(document.getDocumentType() != null && document.getDocumentType().getIsMandatory())
                    .metadata(buildDocumentMetadata(document))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_REJECTED, document.getOrderNumber(), event);

            log.info("Published document rejected event - DocumentId: {}, OrderId: {}, Reason: {}",
                    document.getId(), document.getOrderId(), reason);

        } catch (Exception e) {
            log.error("Failed to publish document rejected event for document {}", document.getId(), e);
        }
    }

    /**
     * Publish document expired event
     *
     * @param document the expired document
     */
    public void publishDocumentExpired(Document document) {
        try {
            DocumentEvent.DocumentExpiredEvent event = DocumentEvent.DocumentExpiredEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_EXPIRED")
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .orderId(document.getOrderId())
                    .orderNumber(document.getOrderNumber())
                    .triggeredBy("SYSTEM")
                    .documentType(document.getTypeCode())
                    .expirationDate(document.getExpirationDate() != null ? document.getExpirationDate().atStartOfDay() : null)
                    .previousStatus(document.getStatus() != null ? document.getStatus().name() : null)
                    .wasNotified(document.getExpirationNotified())
                    .notificationSentAt(document.getExpirationNotificationSentAt())
                    .requiresRenewal(document.getDocumentType() != null && document.getDocumentType().getIsMandatory())
                    .metadata(buildDocumentMetadata(document))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_EXPIRED, document.getOrderNumber(), event);

            log.info("Published document expired event - DocumentId: {}, OrderId: {}, ExpirationDate: {}",
                    document.getId(), document.getOrderId(), document.getExpirationDate());

        } catch (Exception e) {
            log.error("Failed to publish document expired event for document {}", document.getId(), e);
        }
    }

    /**
     * Publish document deleted event
     *
     * @param documentId document ID
     * @param deletedBy  user who deleted the document
     */
    public void publishDocumentDeleted(Long documentId, String deletedBy) {
        publishDocumentDeleted(documentId, null, null, deletedBy, "Document deleted");
    }

    /**
     * Publish document deleted event with additional details
     *
     * @param documentId  document ID
     * @param orderId     order ID
     * @param orderNumber order number
     * @param deletedBy   user who deleted the document
     * @param reason      deletion reason
     */
    public void publishDocumentDeleted(Long documentId, Long orderId, String orderNumber, String deletedBy, String reason) {
        try {
            DocumentEvent.DocumentDeletedEvent event = DocumentEvent.DocumentDeletedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_DELETED")
                    .documentId(documentId)
                    .orderId(orderId)
                    .orderNumber(orderNumber)
                    .triggeredBy(deletedBy)
                    .deletedBy(deletedBy)
                    .deletedAt(LocalDateTime.now())
                    .deletionReason(reason)
                    .isSoftDelete(true)
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_DELETED, orderNumber, event);

            log.info("Published document deleted event - DocumentId: {}, OrderId: {}, DeletedBy: {}",
                    documentId, orderId, deletedBy);

        } catch (Exception e) {
            log.error("Failed to publish document deleted event for document {}", documentId, e);
        }
    }

    /**
     * Publish document archived event
     *
     * @param document the archived document
     */
    public void publishDocumentArchived(Document document) {
        try {
            DocumentEvent.DocumentArchivedEvent event = DocumentEvent.DocumentArchivedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_ARCHIVED")
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .orderId(document.getOrderId())
                    .orderNumber(document.getOrderNumber())
                    .triggeredBy(document.getUpdatedBy() != null ? document.getUpdatedBy() : "SYSTEM")
                    .documentType(document.getTypeCode())
                    .archivedBy(document.getUpdatedBy() != null ? document.getUpdatedBy() : "SYSTEM")
                    .archivedAt(LocalDateTime.now())
                    .archiveReason("Document archived")
                    .archiveLocation(document.getStoragePath())
                    .previousStatus(document.getStatus() != null ? document.getStatus().name() : null)
                    .metadata(buildDocumentMetadata(document))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_ARCHIVED, document.getOrderNumber(), event);

            log.info("Published document archived event - DocumentId: {}, OrderId: {}",
                    document.getId(), document.getOrderId());

        } catch (Exception e) {
            log.error("Failed to publish document archived event for document {}", document.getId(), e);
        }
    }

    /**
     * Publish document version created event
     *
     * @param parentDocument parent document
     * @param newVersion     new version document
     */
    public void publishVersionCreated(Document parentDocument, Document newVersion) {
        try {
            DocumentEvent.DocumentVersionCreatedEvent event = DocumentEvent.DocumentVersionCreatedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_VERSION_CREATED")
                    .documentId(newVersion.getId())
                    .documentUuid(newVersion.getDocumentUuid())
                    .orderId(newVersion.getOrderId())
                    .orderNumber(newVersion.getOrderNumber())
                    .triggeredBy(newVersion.getUploadedBy())
                    .documentType(newVersion.getTypeCode())
                    .parentDocumentId(parentDocument.getId())
                    .parentDocumentUuid(parentDocument.getDocumentUuid())
                    .oldVersion(parentDocument.getVersion())
                    .newVersion(newVersion.getVersion())
                    .versionCreatedBy(newVersion.getUploadedBy())
                    .versionReason("New version uploaded")
                    .isPreviousVersionArchived(parentDocument.isArchived())
                    .metadata(buildDocumentMetadata(newVersion))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_VERSION_CREATED, newVersion.getOrderNumber(), event);

            log.info("Published version created event - NewDocumentId: {}, ParentId: {}, Version: {} -> {}",
                    newVersion.getId(), parentDocument.getId(), parentDocument.getVersion(), newVersion.getVersion());

        } catch (Exception e) {
            log.error("Failed to publish version created event for document {}", newVersion.getId(), e);
        }
    }

    /**
     * Publish document scan completed event
     *
     * @param document   the scanned document
     * @param scanResult scan result details
     */
    public void publishScanCompleted(Document document, ScanResult scanResult) {
        try {
            DocumentEvent.DocumentScanCompletedEvent event = DocumentEvent.DocumentScanCompletedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("DOCUMENT_SCAN_COMPLETED")
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .orderId(document.getOrderId())
                    .orderNumber(document.getOrderNumber())
                    .triggeredBy("VIRUS_SCANNER")
                    .documentType(document.getTypeCode())
                    .scanType("VIRUS_SCAN")
                    .scanStatus(scanResult.getStatus() != null ? scanResult.getStatus().name() : "UNKNOWN")
                    .scanCompletedAt(scanResult.getScanDate())
                    .isSafe(scanResult.isSafe())
                    .threatDetected(scanResult.getThreatName())
                    .scanResult(scanResult.toMap())
                    .actionTaken(scanResult.requiresAction() ? "QUARANTINE" : "APPROVED")
                    .metadata(buildScanMetadata(scanResult))
                    .build();

            sendEvent(KafkaConfig.TOPIC_DOCUMENT_SCAN_COMPLETED, document.getOrderNumber(), event);

            log.info("Published scan completed event - DocumentId: {}, Status: {}, Safe: {}",
                    document.getId(), scanResult.getStatus(), scanResult.isSafe());

        } catch (Exception e) {
            log.error("Failed to publish scan completed event for document {}", document.getId(), e);
        }
    }

    /**
     * Publish all documents validated event for an order
     *
     * @param orderId            order ID
     * @param totalDocuments     total number of documents
     * @param validatedDocuments number of validated documents
     * @param validatedBy        user who validated the last document
     */
    public void publishAllDocumentsValidated(Long orderId) {
        publishAllDocumentsValidated(orderId, 0, 0, "SYSTEM");
    }

    /**
     * Publish all documents validated event for an order with details
     *
     * @param orderId            order ID
     * @param totalDocuments     total number of documents
     * @param validatedDocuments number of validated documents
     * @param validatedBy        user who validated the last document
     */
    public void publishAllDocumentsValidated(Long orderId, Integer totalDocuments, Integer validatedDocuments, String validatedBy) {
        try {
            DocumentEvent.AllDocumentsValidatedEvent event = DocumentEvent.AllDocumentsValidatedEvent.builder()
                    .eventId(DocumentEvent.generateEventId())
                    .timestamp(DocumentEvent.getCurrentTimestamp())
                    .eventType("ALL_DOCUMENTS_VALIDATED")
                    .orderId(orderId)
                    .triggeredBy(validatedBy)
                    .totalDocuments(totalDocuments)
                    .validatedDocuments(validatedDocuments)
                    .allValidatedAt(LocalDateTime.now())
                    .validatedBy(validatedBy)
                    .orderReadyForProcessing(totalDocuments.equals(validatedDocuments))
                    .nextWorkflowStep("DOCUMENT_VALIDATION_COMPLETE")
                    .build();

            sendEvent(KafkaConfig.TOPIC_ALL_DOCUMENTS_VALIDATED, String.valueOf(orderId), event);

            log.info("Published all documents validated event - OrderId: {}, Total: {}, Validated: {}",
                    orderId, totalDocuments, validatedDocuments);

        } catch (Exception e) {
            log.error("Failed to publish all documents validated event for order {}", orderId, e);
        }
    }

    /**
     * Send event to Kafka with error handling
     *
     * @param topic Kafka topic
     * @param key   message key
     * @param event event object
     */
    private void sendEvent(String topic, String key, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Event sent successfully to topic: {} with key: {} at offset: {}",
                            topic, key, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send event to topic: {} with key: {}", topic, key, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error sending event to Kafka topic: {}", topic, e);
            throw new RuntimeException("Failed to publish event to Kafka", e);
        }
    }

    /**
     * Build document metadata map
     *
     * @param document the document
     * @return metadata map
     */
    private Map<String, Object> buildDocumentMetadata(Document document) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", document.getOriginalFilename());
        metadata.put("fileSize", document.getFileSizeBytes());
        metadata.put("mimeType", document.getMimeType());
        metadata.put("version", document.getVersion());
        metadata.put("isLatestVersion", document.getIsLatestVersion());
        metadata.put("uploadedAt", document.getUploadedAt());
        metadata.put("businessUnitId", document.getBusinessUnitId());

        if (document.getCustomMetadata() != null) {
            metadata.put("customMetadata", document.getCustomMetadata());
        }

        if (document.getTags() != null && !document.getTags().isEmpty()) {
            metadata.put("tags", document.getTags());
        }

        return metadata;
    }

    /**
     * Build scan result metadata map
     *
     * @param scanResult the scan result
     * @return metadata map
     */
    private Map<String, Object> buildScanMetadata(ScanResult scanResult) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scannerEngine", scanResult.getScannerEngine());
        metadata.put("scannerVersion", scanResult.getScannerVersion());
        metadata.put("scanDurationMs", scanResult.getScanDurationMs());
        metadata.put("message", scanResult.getMessage());

        if (scanResult.getMetadata() != null) {
            metadata.putAll(scanResult.getMetadata());
        }

        return metadata;
    }
}
