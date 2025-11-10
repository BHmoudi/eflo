package com.eflo.user.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener for workflow-related events from Workflow Service.
 * Note: This is a placeholder implementation. In production, this would use
 * Spring Kafka's @KafkaListener annotation to consume events from Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventListener {

    // @KafkaListener(topics = "workflow.started", groupId = "user-service")
    public void handleWorkflowStartedEvent(String eventJson) {
        log.info("Received workflow started event");
        try {
            // Parse event and process
            log.debug("Processing workflow started event: {}", eventJson);

            // Example: Update user's pending tasks count, send notifications

        } catch (Exception e) {
            log.error("Error processing workflow started event", e);
        }
    }

    // @KafkaListener(topics = "workflow.task.assigned", groupId = "user-service")
    public void handleWorkflowTaskAssignedEvent(String eventJson) {
        log.info("Received workflow task assigned event");
        try {
            // Parse event and process
            log.debug("Processing workflow task assigned event: {}", eventJson);

            // Example: Notify user of new task assignment

        } catch (Exception e) {
            log.error("Error processing workflow task assigned event", e);
        }
    }

    // @KafkaListener(topics = "workflow.completed", groupId = "user-service")
    public void handleWorkflowCompletedEvent(String eventJson) {
        log.info("Received workflow completed event");
        try {
            // Parse event and process
            log.debug("Processing workflow completed event: {}", eventJson);

            // Example: Update user metrics, trigger follow-up actions

        } catch (Exception e) {
            log.error("Error processing workflow completed event", e);
        }
    }

    // @KafkaListener(topics = "workflow.failed", groupId = "user-service")
    public void handleWorkflowFailedEvent(String eventJson) {
        log.info("Received workflow failed event");
        try {
            // Parse event and process
            log.debug("Processing workflow failed event: {}", eventJson);

            // Example: Notify relevant users, log for investigation

        } catch (Exception e) {
            log.error("Error processing workflow failed event", e);
        }
    }
}
