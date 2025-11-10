package com.eflo.workflow.service;

import com.eflo.workflow.config.KafkaConfig;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.model.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Workflow Event Publisher Service
 *
 * Publishes workflow events to Kafka topics for event-driven architecture.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish a generic workflow event
     */
    public void publishWorkflowEvent(WorkflowEvent event) {
        try {
            String topic = event.getEventType().getKafkaTopic();
            kafkaTemplate.send(topic, event.getInstanceId().toString(), event);
            log.debug("Published workflow event: {} to topic: {}", event.getEventType(), topic);
        } catch (Exception e) {
            log.error("Failed to publish workflow event: {}", event.getEventType(), e);
        }
    }

    /**
     * Publish instance event
     */
    public void publishInstanceEvent(EventType eventType, Long instanceId, Long orderId,
                                     String processCode, Long userId, String userName,
                                     String description, Map<String, Object> eventData) {
        WorkflowEvent event = WorkflowEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .instanceId(instanceId)
                .orderId(orderId)
                .processCode(processCode)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .userName(userName)
                .description(description)
                .eventData(eventData)
                .build();

        publishWorkflowEvent(event);
    }

    /**
     * Publish state changed event
     */
    public void publishStateChangedEvent(StateChangedEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_STATE_EVENTS,
                             event.getInstanceId().toString(),
                             event);
            log.debug("Published state changed event for instance: {}", event.getInstanceId());
        } catch (Exception e) {
            log.error("Failed to publish state changed event", e);
        }
    }

    /**
     * Publish task event
     */
    public void publishTaskEvent(TaskEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_TASK_EVENTS,
                             event.getTaskId().toString(),
                             event);
            log.debug("Published task event: {} for task: {}", event.getEventType(), event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to publish task event", e);
        }
    }

    /**
     * Publish deadline event
     */
    public void publishDeadlineEvent(EventType eventType, Long instanceId, Long orderId,
                                     String description) {
        try {
            WorkflowEvent event = WorkflowEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .instanceId(instanceId)
                    .orderId(orderId)
                    .timestamp(LocalDateTime.now())
                    .description(description)
                    .build();

            kafkaTemplate.send(KafkaConfig.WORKFLOW_DEADLINE_EVENTS,
                             instanceId.toString(),
                             event);
            log.debug("Published deadline event: {} for instance: {}", eventType, instanceId);
        } catch (Exception e) {
            log.error("Failed to publish deadline event", e);
        }
    }

    /**
     * Publish escalation event
     */
    public void publishEscalationEvent(EventType eventType, Long instanceId, Long taskId,
                                      String description, Map<String, Object> eventData) {
        try {
            WorkflowEvent event = WorkflowEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .instanceId(instanceId)
                    .timestamp(LocalDateTime.now())
                    .description(description)
                    .eventData(eventData)
                    .build();

            kafkaTemplate.send(KafkaConfig.WORKFLOW_ESCALATION_EVENTS,
                             instanceId.toString(),
                             event);
            log.debug("Published escalation event: {} for instance: {}", eventType, instanceId);
        } catch (Exception e) {
            log.error("Failed to publish escalation event", e);
        }
    }

    /**
     * Publish task assigned event
     */
    public void publishTaskAssigned(TaskAssignedEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_TASK_EVENTS,
                             event.getTaskId().toString(),
                             event);
            log.debug("Published task assigned event for task: {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to publish task assigned event", e);
        }
    }

    /**
     * Publish task completed event
     */
    public void publishTaskCompleted(TaskCompletedEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_TASK_EVENTS,
                             event.getTaskId().toString(),
                             event);
            log.debug("Published task completed event for task: {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to publish task completed event", e);
        }
    }

    /**
     * Publish approval required event
     */
    public void publishApprovalRequired(ApprovalRequiredEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_TASK_EVENTS,
                             event.getApprovalId().toString(),
                             event);
            log.debug("Published approval required event for approval: {}", event.getApprovalId());
        } catch (Exception e) {
            log.error("Failed to publish approval required event", e);
        }
    }

    /**
     * Publish approval completed event
     */
    public void publishApprovalCompleted(ApprovalCompletedEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_TASK_EVENTS,
                             event.getApprovalId().toString(),
                             event);
            log.debug("Published approval completed event for approval: {}", event.getApprovalId());
        } catch (Exception e) {
            log.error("Failed to publish approval completed event", e);
        }
    }

    /**
     * Publish SLA breach event
     */
    public void publishSlaBreachEvent(SlaBreachEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_DEADLINE_EVENTS,
                             event.getTaskId().toString(),
                             event);
            log.debug("Published SLA breach event for task: {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to publish SLA breach event", e);
        }
    }

    /**
     * Publish escalation event (typed)
     */
    public void publishEscalationEvent(EscalationEvent event) {
        try {
            kafkaTemplate.send(KafkaConfig.WORKFLOW_ESCALATION_EVENTS,
                             event.getTaskId().toString(),
                             event);
            log.debug("Published escalation event for task: {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to publish escalation event", e);
        }
    }
}
