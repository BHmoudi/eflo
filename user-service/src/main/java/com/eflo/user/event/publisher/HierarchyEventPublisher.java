package com.eflo.user.event.publisher;

import com.eflo.user.event.model.HierarchyChangedEvent;
import com.eflo.user.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publisher for hierarchy-related events.
 * Note: This is a placeholder implementation. In production, this would use
 * Spring Kafka's KafkaTemplate to publish events to Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HierarchyEventPublisher {

    private static final String HIERARCHY_CHANGED_TOPIC = "hierarchy.changed";

    // In production, inject KafkaTemplate here
    // private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Publish hierarchy changed event.
     *
     * @param event the event to publish
     */
    @Async("kafkaTaskExecutor")
    public void publishHierarchyChangedEvent(HierarchyChangedEvent event) {
        log.info("Publishing hierarchy changed event for: {}", event.getHierarchyId());
        try {
            String eventJson = JsonUtils.toJson(event);
            // kafkaTemplate.send(HIERARCHY_CHANGED_TOPIC, event.getHierarchyId(), eventJson);
            log.debug("Hierarchy changed event published: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish hierarchy changed event for: {}", event.getHierarchyId(), e);
        }
    }
}
