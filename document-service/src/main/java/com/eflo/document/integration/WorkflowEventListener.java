package com.eflo.document.integration;

import com.eflo.document.config.KafkaConfig;
import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kafka event listener for Workflow-related events
 * Handles workflow state changes and task completion events
 */
@Component
public class WorkflowEventListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEventListener.class);

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentEventPublisher eventPublisher;

    public WorkflowEventListener(
            DocumentRepository documentRepository,
            DocumentTypeRepository documentTypeRepository,
            DocumentEventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handle workflow state changed event
     * Updates document requirements based on workflow state transitions
     *
     * @param event Workflow state changed event payload
     * @param key Kafka message key
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_WORKFLOW_STATE_CHANGED,
            groupId = "${spring.kafka.consumer.group-id}-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    @Transactional
    public void onWorkflowStateChanged(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received workflow state changed event with key: {}", key);

            // Extract workflow details
            Long orderId = extractLongValue(event, "orderId");
            String orderNumber = extractStringValue(event, "orderNumber");
            String workflowId = extractStringValue(event, "workflowId");
            String previousState = extractStringValue(event, "previousState");
            String currentState = extractStringValue(event, "currentState");
            String changedBy = extractStringValue(event, "changedBy");

            if (orderId == null || currentState == null) {
                log.error("Invalid workflow state changed event: missing orderId or currentState");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing workflow state change - OrderId: {}, WorkflowId: {}, State: {} -> {}",
                    orderId, workflowId, previousState, currentState);

            // Update document requirements based on new workflow state
            updateDocumentRequirements(orderId, orderNumber, workflowId, currentState, changedBy);

            log.info("Document requirements updated for workflow state change - OrderId: {}, State: {}",
                    orderId, currentState);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing workflow state changed event", e);
            throw new RuntimeException("Failed to process workflow state changed event", e);
        }
    }

    /**
     * Handle workflow task completed event
     * Marks documents as complete when associated workflow tasks finish
     *
     * @param event Workflow task completed event payload
     * @param key Kafka message key
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_WORKFLOW_TASK_COMPLETED,
            groupId = "${spring.kafka.consumer.group-id}-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    @Transactional
    public void onTaskCompleted(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received workflow task completed event with key: {}", key);

            // Extract task details
            Long orderId = extractLongValue(event, "orderId");
            String orderNumber = extractStringValue(event, "orderNumber");
            String taskId = extractStringValue(event, "taskId");
            String taskType = extractStringValue(event, "taskType");
            String taskName = extractStringValue(event, "taskName");
            String completedBy = extractStringValue(event, "completedBy");
            Map<String, Object> taskResult = extractMapValue(event, "taskResult");

            if (orderId == null || taskType == null) {
                log.error("Invalid workflow task completed event: missing orderId or taskType");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing workflow task completed - OrderId: {}, TaskId: {}, TaskType: {}, CompletedBy: {}",
                    orderId, taskId, taskType, completedBy);

            // Handle document completion based on task type
            handleTaskCompletion(orderId, orderNumber, taskId, taskType, taskName, completedBy, taskResult);

            log.info("Document task completion processed for OrderId: {}, TaskType: {}",
                    orderId, taskType);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing workflow task completed event", e);
            throw new RuntimeException("Failed to process workflow task completed event", e);
        }
    }

    /**
     * Update document requirements based on workflow state
     *
     * @param orderId Order ID
     * @param orderNumber Order number
     * @param workflowId Workflow ID
     * @param currentState Current workflow state
     * @param changedBy User who changed the state
     */
    private void updateDocumentRequirements(Long orderId, String orderNumber, String workflowId,
                                           String currentState, String changedBy) {

        // Get all documents for this order
        List<Document> documents = documentRepository.findByOrderId(orderId);

        log.info("Found {} documents for order {} in workflow state '{}'",
                documents.size(), orderId, currentState);

        // Handle state-specific document requirements
        switch (currentState.toUpperCase()) {
            case "DOCUMENT_COLLECTION":
            case "AWAITING_DOCUMENTS":
                handleDocumentCollectionState(orderId, orderNumber, documents);
                break;

            case "DOCUMENT_VALIDATION":
            case "DOCUMENT_REVIEW":
                handleDocumentValidationState(orderId, orderNumber, documents);
                break;

            case "DOCUMENT_APPROVED":
            case "DOCUMENTS_COMPLETE":
                handleDocumentApprovedState(orderId, orderNumber, documents, changedBy);
                break;

            case "PROCESSING":
            case "IN_TRANSIT":
                handleProcessingState(orderId, orderNumber, documents);
                break;

            case "COMPLETED":
            case "DELIVERED":
                handleCompletedState(orderId, orderNumber, documents);
                break;

            case "CANCELLED":
            case "REJECTED":
                handleCancelledState(orderId, orderNumber, documents, changedBy);
                break;

            default:
                log.debug("No specific document handling for workflow state: {}", currentState);
        }
    }

    /**
     * Handle document completion based on workflow task
     *
     * @param orderId Order ID
     * @param orderNumber Order number
     * @param taskId Task ID
     * @param taskType Task type
     * @param taskName Task name
     * @param completedBy User who completed the task
     * @param taskResult Task result data
     */
    private void handleTaskCompletion(Long orderId, String orderNumber, String taskId,
                                      String taskType, String taskName, String completedBy,
                                      Map<String, Object> taskResult) {

        // Handle task type specific logic
        switch (taskType.toUpperCase()) {
            case "DOCUMENT_UPLOAD":
                handleDocumentUploadTask(orderId, orderNumber, taskResult, completedBy);
                break;

            case "DOCUMENT_VALIDATION":
            case "DOCUMENT_REVIEW":
                handleDocumentValidationTask(orderId, orderNumber, taskResult, completedBy);
                break;

            case "DOCUMENT_APPROVAL":
                handleDocumentApprovalTask(orderId, orderNumber, taskResult, completedBy);
                break;

            case "DOCUMENT_VERIFICATION":
                handleDocumentVerificationTask(orderId, orderNumber, taskResult, completedBy);
                break;

            default:
                log.debug("No specific document handling for task type: {}", taskType);
        }
    }

    // State handlers

    private void handleDocumentCollectionState(Long orderId, String orderNumber, List<Document> documents) {
        log.info("Order {} entered document collection state. {} documents currently uploaded.",
                orderId, documents.size());

        // Check if minimum required documents are present
        long pendingDocs = documents.stream()
                .filter(Document::isPending)
                .count();

        if (pendingDocs > 0) {
            log.info("Order {} has {} pending documents in collection state", orderId, pendingDocs);
        }
    }

    private void handleDocumentValidationState(Long orderId, String orderNumber, List<Document> documents) {
        log.info("Order {} entered document validation state. Checking documents requiring validation.",
                orderId);

        long docsRequiringValidation = documents.stream()
                .filter(doc -> doc.isPending() &&
                        doc.getDocumentType() != null &&
                        doc.getDocumentType().getRequiresValidation())
                .count();

        log.info("Order {} has {} documents requiring validation", orderId, docsRequiringValidation);
    }

    private void handleDocumentApprovedState(Long orderId, String orderNumber, List<Document> documents,
                                            String approvedBy) {
        log.info("Order {} documents approved by {}. Validating all pending documents.", orderId, approvedBy);

        // Auto-validate pending documents that don't require manual validation
        documents.stream()
                .filter(doc -> doc.isPending() &&
                        doc.getDocumentType() != null &&
                        !doc.getDocumentType().getRequiresValidation())
                .forEach(doc -> {
                    try {
                        doc.markAsValidated(approvedBy, "Auto-validated on workflow approval");
                        documentRepository.save(doc);
                        eventPublisher.publishDocumentValidated(doc, approvedBy);
                        log.info("Auto-validated document {} for order {}", doc.getId(), orderId);
                    } catch (Exception e) {
                        log.error("Error auto-validating document {} for order {}", doc.getId(), orderId, e);
                    }
                });

        // Check if all documents are validated
        long totalDocs = documents.size();
        long validatedDocs = documents.stream().filter(Document::isValidated).count();

        if (totalDocs > 0 && validatedDocs == totalDocs) {
            eventPublisher.publishAllDocumentsValidated(orderId, (int) totalDocs, (int) validatedDocs, approvedBy);
        }
    }

    private void handleProcessingState(Long orderId, String orderNumber, List<Document> documents) {
        log.info("Order {} entered processing state. {} documents attached.", orderId, documents.size());

        // Ensure all required documents are validated
        long unvalidatedDocs = documents.stream()
                .filter(doc -> !doc.isValidated() && !doc.isArchived())
                .count();

        if (unvalidatedDocs > 0) {
            log.warn("Order {} in processing state has {} unvalidated documents", orderId, unvalidatedDocs);
        }
    }

    private void handleCompletedState(Long orderId, String orderNumber, List<Document> documents) {
        log.info("Order {} completed. Archiving documents.", orderId);

        // Archive old versions and cleanup
        documents.stream()
                .filter(doc -> !doc.getIsLatestVersion() && doc.isActive())
                .forEach(doc -> {
                    try {
                        doc.markAsArchived();
                        documentRepository.save(doc);
                        eventPublisher.publishDocumentArchived(doc);
                        log.info("Archived old version of document {} for completed order {}", doc.getId(), orderId);
                    } catch (Exception e) {
                        log.error("Error archiving document {} for order {}", doc.getId(), orderId, e);
                    }
                });
    }

    private void handleCancelledState(Long orderId, String orderNumber, List<Document> documents,
                                     String cancelledBy) {
        log.info("Order {} cancelled by {}. Archiving {} documents.", orderId, cancelledBy, documents.size());

        // Archive all active documents
        documents.stream()
                .filter(Document::isActive)
                .forEach(doc -> {
                    try {
                        doc.markAsArchived();
                        documentRepository.save(doc);
                        eventPublisher.publishDocumentArchived(doc);
                        log.info("Archived document {} for cancelled order {}", doc.getId(), orderId);
                    } catch (Exception e) {
                        log.error("Error archiving document {} for cancelled order {}", doc.getId(), orderId, e);
                    }
                });
    }

    // Task handlers

    private void handleDocumentUploadTask(Long orderId, String orderNumber, Map<String, Object> taskResult,
                                         String uploadedBy) {
        log.info("Document upload task completed for order {} by {}", orderId, uploadedBy);

        // Task result might contain document IDs that were uploaded
        if (taskResult != null && taskResult.containsKey("documentIds")) {
            log.info("Documents uploaded in task: {}", taskResult.get("documentIds"));
        }
    }

    private void handleDocumentValidationTask(Long orderId, String orderNumber, Map<String, Object> taskResult,
                                             String validatedBy) {
        log.info("Document validation task completed for order {} by {}", orderId, validatedBy);

        // Extract document ID and validation result from task
        Long documentId = extractLongValue(taskResult, "documentId");
        Boolean isApproved = extractBooleanValue(taskResult, "approved");
        String comments = extractStringValue(taskResult, "comments");

        if (documentId != null && isApproved != null) {
            Optional<Document> docOpt = documentRepository.findById(documentId);
            if (docOpt.isPresent()) {
                Document document = docOpt.get();
                if (isApproved) {
                    document.markAsValidated(validatedBy, comments);
                    eventPublisher.publishDocumentValidated(document, validatedBy);
                } else {
                    document.markAsRejected(validatedBy, comments);
                    eventPublisher.publishDocumentRejected(document, comments);
                }
                documentRepository.save(document);
                log.info("Document {} validation completed with result: {}", documentId, isApproved);
            }
        }
    }

    private void handleDocumentApprovalTask(Long orderId, String orderNumber, Map<String, Object> taskResult,
                                           String approvedBy) {
        log.info("Document approval task completed for order {} by {}", orderId, approvedBy);

        // Get all pending documents and approve them
        List<Document> pendingDocs = documentRepository.findByOrderIdAndStatus(orderId, DocumentStatus.PENDING);
        pendingDocs.forEach(doc -> {
            try {
                doc.markAsValidated(approvedBy, "Approved via workflow task");
                documentRepository.save(doc);
                eventPublisher.publishDocumentValidated(doc, approvedBy);
                log.info("Approved document {} for order {}", doc.getId(), orderId);
            } catch (Exception e) {
                log.error("Error approving document {} for order {}", doc.getId(), orderId, e);
            }
        });

        // Check if all documents are now validated
        long totalDocs = documentRepository.countByOrderId(orderId);
        long validatedDocs = documentRepository.countByOrderIdAndStatus(orderId, DocumentStatus.VALIDATED);

        if (totalDocs > 0 && validatedDocs == totalDocs) {
            eventPublisher.publishAllDocumentsValidated(orderId, (int) totalDocs, (int) validatedDocs, approvedBy);
        }
    }

    private void handleDocumentVerificationTask(Long orderId, String orderNumber, Map<String, Object> taskResult,
                                               String verifiedBy) {
        log.info("Document verification task completed for order {} by {}", orderId, verifiedBy);

        // Handle verification results
        Boolean verificationPassed = extractBooleanValue(taskResult, "verificationPassed");
        String verificationNotes = extractStringValue(taskResult, "notes");

        if (verificationPassed != null && !verificationPassed) {
            log.warn("Document verification failed for order {}. Notes: {}", orderId, verificationNotes);
        }
    }

    // Utility methods

    private Long extractLongValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse Long value for key '{}': {}", key, value);
            return null;
        }
    }

    private String extractStringValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Boolean extractBooleanValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMapValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }
}
