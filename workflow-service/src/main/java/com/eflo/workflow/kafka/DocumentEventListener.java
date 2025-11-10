package com.eflo.workflow.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Document Event Listener
 *
 * Consumes document-related events from Kafka to update workflow state.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventListener {

    /**
     * Handle document events
     */
    @KafkaListener(topics = "document.events", groupId = "workflow-service-group")
    public void handleDocumentEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("Received document event: {}", eventType);

            switch (eventType) {
                case "DOCUMENT_UPLOADED":
                    handleDocumentUploaded(event);
                    break;
                case "DOCUMENT_VERIFIED":
                    handleDocumentVerified(event);
                    break;
                case "DOCUMENT_REJECTED":
                    handleDocumentRejected(event);
                    break;
                default:
                    log.debug("Unhandled document event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing document event", e);
        }
    }

    private void handleDocumentUploaded(Map<String, Object> event) {
        log.info("Processing DOCUMENT_UPLOADED event");
        // TODO: Update task status or trigger auto-transition if all required documents uploaded
    }

    private void handleDocumentVerified(Map<String, Object> event) {
        log.info("Processing DOCUMENT_VERIFIED event");
        // TODO: Complete validation tasks automatically
    }

    private void handleDocumentRejected(Map<String, Object> event) {
        log.info("Processing DOCUMENT_REJECTED event");
        // TODO: Create new task for document re-upload
    }
}
