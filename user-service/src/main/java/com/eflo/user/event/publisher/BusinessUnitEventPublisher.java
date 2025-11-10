package com.eflo.user.event.publisher;

import com.eflo.user.event.model.BusinessUnitCreatedEvent;
import com.eflo.user.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publisher for business unit-related events.
 * Note: This is a placeholder implementation. In production, this would use
 * Spring Kafka's KafkaTemplate to publish events to Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessUnitEventPublisher {

    private static final String BUSINESS_UNIT_CREATED_TOPIC = "businessunit.created";
    private static final String BUSINESS_UNIT_UPDATED_TOPIC = "businessunit.updated";
    private static final String BUSINESS_UNIT_DELETED_TOPIC = "businessunit.deleted";

    // In production, inject KafkaTemplate here
    // private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Publish business unit created event.
     *
     * @param event the event to publish
     */
    @Async("kafkaTaskExecutor")
    public void publishBusinessUnitCreatedEvent(BusinessUnitCreatedEvent event) {
        log.info("Publishing business unit created event for: {}", event.getBusinessUnitId());
        try {
            String eventJson = JsonUtils.toJson(event);
            // kafkaTemplate.send(BUSINESS_UNIT_CREATED_TOPIC, event.getBusinessUnitId(), eventJson);
            log.debug("Business unit created event published: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish business unit created event for: {}", event.getBusinessUnitId(), e);
        }
    }
}
