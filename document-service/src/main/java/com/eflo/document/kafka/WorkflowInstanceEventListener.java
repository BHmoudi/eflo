package com.eflo.document.kafka;

import com.eflo.document.service.DocumentInitializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Workflow Instance Event Listener
 *
 * Consumes workflow instance events from Kafka to trigger document operations.
 * Automatically creates document placeholders when workflow instances are created.
 *
 * @author Document Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowInstanceEventListener {

    private final DocumentInitializationService documentInitializationService;

    /**
     * Handle workflow events from Kafka
     */
    @KafkaListener(topics = "workflow.events", groupId = "document-service-group")
    public void handleWorkflowEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("Received workflow event: {} - {}", eventType, event);

            if ("INSTANCE_CREATED".equals(eventType)) {
                handleWorkflowInstanceCreated(event);
            } else {
                log.debug("Unhandled workflow event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing workflow event: {}", event, e);
            // Don't throw - let Kafka retry mechanism handle it
        }
    }

    /**
     * Handle WORKFLOW_INSTANCE_CREATED event
     * Creates document placeholders for required documents
     */
    private void handleWorkflowInstanceCreated(Map<String, Object> event) {
        try {
            // Extract event data
            Long instanceId = extractLong(event.get("instanceId"));
            Long orderId = extractLong(event.get("orderId"));
            String processCode = (String) event.get("processCode");
            String createdBy = (String) event.get("createdBy");

            // Extract required documents from event details
            Map<String, Object> details = (Map<String, Object>) event.get("details");
            List<Map<String, Object>> requiredDocuments = null;

            if (details != null) {
                requiredDocuments = (List<Map<String, Object>>) details.get("requiredDocuments");
            }

            if (orderId == null || instanceId == null) {
                log.error("Invalid WORKFLOW_INSTANCE_CREATED event - missing orderId or instanceId: {}", event);
                return;
            }

            if (requiredDocuments == null || requiredDocuments.isEmpty()) {
                log.info("No required documents for workflow instance: instanceId={}, orderId={}",
                        instanceId, orderId);
                return;
            }

            log.info("Processing WORKFLOW_INSTANCE_CREATED: orderId={}, instanceId={}, processCode={}, documents={}",
                    orderId, instanceId, processCode, requiredDocuments.size());

            // Initialize document placeholders
            documentInitializationService.initializeDocumentsForWorkflowInstance(
                    orderId,
                    instanceId,
                    requiredDocuments,
                    createdBy
            );

            log.info("Document placeholders initialized successfully for instance: {}", instanceId);

        } catch (Exception e) {
            log.error("Failed to process WORKFLOW_INSTANCE_CREATED event", e);
            throw e; // Re-throw to trigger Kafka retry
        }
    }

    /**
     * Helper method to safely extract Long from Object
     */
    private Long extractLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse Long from string: {}", value);
                return null;
            }
        }
        return null;
    }
}
