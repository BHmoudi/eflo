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

/**
 * Kafka event listener for Order-related events
 * Handles order lifecycle events and manages document requirements accordingly
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentEventPublisher eventPublisher;

    public OrderEventListener(
            DocumentRepository documentRepository,
            DocumentTypeRepository documentTypeRepository,
            DocumentEventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handle order created event
     * Initializes document requirements for the new order
     *
     * @param event Order created event payload
     * @param key Kafka message key
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_ORDER_CREATED,
            groupId = "${spring.kafka.consumer.group-id}-order",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    @Transactional
    public void onOrderCreated(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received order created event with key: {}", key);

            // Extract order details from event
            Long orderId = extractLongValue(event, "orderId");
            String orderNumber = extractStringValue(event, "orderNumber");
            String orderType = extractStringValue(event, "orderType");
            Long businessUnitId = extractLongValue(event, "businessUnitId");

            if (orderId == null || orderNumber == null) {
                log.error("Invalid order created event: missing orderId or orderNumber");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing order created - OrderId: {}, OrderNumber: {}, Type: {}",
                    orderId, orderNumber, orderType);

            // Initialize document requirements based on order type
            initializeDocumentRequirements(orderId, orderNumber, orderType, businessUnitId);

            log.info("Document requirements initialized for order {}", orderId);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing order created event", e);
            // Don't acknowledge - message will be retried
            throw new RuntimeException("Failed to process order created event", e);
        }
    }

    /**
     * Handle order delivered event
     * Checks if all required documents are complete and validated
     *
     * @param event Order delivered event payload
     * @param key Kafka message key
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_ORDER_DELIVERED,
            groupId = "${spring.kafka.consumer.group-id}-order",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    @Transactional
    public void onOrderDelivered(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received order delivered event with key: {}", key);

            // Extract order details
            Long orderId = extractLongValue(event, "orderId");
            String orderNumber = extractStringValue(event, "orderNumber");

            if (orderId == null) {
                log.error("Invalid order delivered event: missing orderId");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing order delivered - OrderId: {}, OrderNumber: {}",
                    orderId, orderNumber);

            // Check document completion status
            checkDocumentCompletion(orderId, orderNumber);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing order delivered event", e);
            throw new RuntimeException("Failed to process order delivered event", e);
        }
    }

    /**
     * Handle order cancelled event
     * Handles document cleanup for cancelled orders
     *
     * @param event Order cancelled event payload
     * @param key Kafka message key
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_ORDER_CANCELLED,
            groupId = "${spring.kafka.consumer.group-id}-order",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    @Transactional
    public void onOrderCancelled(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received order cancelled event with key: {}", key);

            // Extract order details
            Long orderId = extractLongValue(event, "orderId");
            String orderNumber = extractStringValue(event, "orderNumber");
            String cancelledBy = extractStringValue(event, "cancelledBy");
            String cancellationReason = extractStringValue(event, "cancellationReason");

            if (orderId == null) {
                log.error("Invalid order cancelled event: missing orderId");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing order cancelled - OrderId: {}, OrderNumber: {}, CancelledBy: {}",
                    orderId, orderNumber, cancelledBy);

            // Handle document cleanup
            handleDocumentCleanup(orderId, orderNumber, cancelledBy, cancellationReason);

            log.info("Document cleanup completed for cancelled order {}", orderId);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing order cancelled event", e);
            throw new RuntimeException("Failed to process order cancelled event", e);
        }
    }

    /**
     * Initialize document requirements for a new order
     *
     * @param orderId Order ID
     * @param orderNumber Order number
     * @param orderType Order type
     * @param businessUnitId Business unit ID
     */
    private void initializeDocumentRequirements(Long orderId, String orderNumber, String orderType, Long businessUnitId) {
        // Check if documents already exist for this order
        long existingDocCount = documentRepository.countByOrderId(orderId);
        if (existingDocCount > 0) {
            log.info("Documents already exist for order {}. Skipping initialization.", orderId);
            return;
        }

        // Get required document types
        // Note: Document types are queried by business unit, not order type
        // In a real implementation, you might have a mapping between order types and document types
        List<DocumentType> requiredTypes;
        if (businessUnitId != null) {
            requiredTypes = documentTypeRepository.findMandatoryByBusinessUnit(businessUnitId);
        } else {
            requiredTypes = documentTypeRepository.findByIsMandatoryTrue();
        }

        log.info("Found {} mandatory document types for order (business unit: {})",
                requiredTypes.size(), businessUnitId);

        // Log required document types for tracking purposes
        // Note: We don't create placeholder documents, but we log the requirements
        if (!requiredTypes.isEmpty()) {
            log.info("Order {} requires the following document types:", orderId);
            requiredTypes.forEach(type ->
                    log.info("  - {} ({}): {} - Requires Validation: {}",
                            type.getTypeCode(),
                            type.getTypeName(),
                            type.getDescription(),
                            type.getRequiresValidation())
            );
        }

        // Store requirement info in metadata if needed
        // This can be used by the frontend to show required documents
        log.info("Document requirements initialized for order {} with {} required types",
                orderId, requiredTypes.size());
    }

    /**
     * Check document completion status for an order
     *
     * @param orderId Order ID
     * @param orderNumber Order number
     */
    private void checkDocumentCompletion(Long orderId, String orderNumber) {
        // Get all documents for this order
        List<Document> documents = documentRepository.findByOrderId(orderId);

        if (documents.isEmpty()) {
            log.warn("Order {} delivered but has no documents", orderId);
            return;
        }

        // Count validated documents
        long totalDocs = documents.size();
        long validatedDocs = documents.stream()
                .filter(Document::isValidated)
                .count();

        long pendingDocs = documents.stream()
                .filter(Document::isPending)
                .count();

        long rejectedDocs = documents.stream()
                .filter(Document::isRejected)
                .count();

        log.info("Order {} document status - Total: {}, Validated: {}, Pending: {}, Rejected: {}",
                orderId, totalDocs, validatedDocs, pendingDocs, rejectedDocs);

        // Check for incomplete documents
        if (pendingDocs > 0) {
            log.warn("Order {} delivered with {} pending documents", orderId, pendingDocs);
        }

        if (rejectedDocs > 0) {
            log.warn("Order {} delivered with {} rejected documents", orderId, rejectedDocs);
        }

        // If all documents are validated, publish completion event
        if (validatedDocs == totalDocs && totalDocs > 0) {
            log.info("All documents validated for delivered order {}", orderId);
            eventPublisher.publishAllDocumentsValidated(
                    orderId,
                    (int) totalDocs,
                    (int) validatedDocs,
                    "SYSTEM"
            );
        }
    }

    /**
     * Handle document cleanup for cancelled orders
     *
     * @param orderId Order ID
     * @param orderNumber Order number
     * @param cancelledBy User who cancelled the order
     * @param reason Cancellation reason
     */
    private void handleDocumentCleanup(Long orderId, String orderNumber, String cancelledBy, String reason) {
        // Get all active documents for this order
        List<Document> documents = documentRepository.findByOrderId(orderId);

        if (documents.isEmpty()) {
            log.info("No documents found for cancelled order {}", orderId);
            return;
        }

        log.info("Processing {} documents for cancelled order {}", documents.size(), orderId);

        // Archive or delete documents based on status
        for (Document document : documents) {
            try {
                if (document.isActive()) {
                    // Archive active documents
                    document.markAsArchived();
                    documentRepository.save(document);

                    // Publish archived event
                    eventPublisher.publishDocumentArchived(document);

                    log.info("Archived document {} for cancelled order {}", document.getId(), orderId);
                }
            } catch (Exception e) {
                log.error("Error archiving document {} for cancelled order {}", document.getId(), orderId, e);
            }
        }

        log.info("Document cleanup completed for cancelled order {}. Archived {} documents.",
                orderId, documents.stream().filter(Document::isArchived).count());
    }

    /**
     * Extract Long value from event map
     *
     * @param event Event map
     * @param key Key to extract
     * @return Long value or null
     */
    private Long extractLongValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
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

    /**
     * Extract String value from event map
     *
     * @param event Event map
     * @param key Key to extract
     * @return String value or null
     */
    private String extractStringValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Extract Boolean value from event map
     *
     * @param event Event map
     * @param key Key to extract
     * @return Boolean value or null
     */
    private Boolean extractBooleanValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
