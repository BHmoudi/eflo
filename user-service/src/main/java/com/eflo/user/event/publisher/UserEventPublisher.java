package com.eflo.user.event.publisher;

import com.eflo.user.event.model.UserCreatedEvent;
import com.eflo.user.event.model.UserDeactivatedEvent;
import com.eflo.user.event.model.UserRoleAssignedEvent;
import com.eflo.user.event.model.UserUpdatedEvent;
import com.eflo.user.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publisher for user-related events.
 * Note: This is a placeholder implementation. In production, this would use
 * Spring Kafka's KafkaTemplate to publish events to Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private static final String USER_CREATED_TOPIC = "user.created";
    private static final String USER_UPDATED_TOPIC = "user.updated";
    private static final String USER_DEACTIVATED_TOPIC = "user.deactivated";
    private static final String USER_ROLE_ASSIGNED_TOPIC = "user.role.assigned";

    // In production, inject KafkaTemplate here
    // private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Publish user created event.
     *
     * @param event the event to publish
     */
    @Async("kafkaTaskExecutor")
    public void publishUserCreatedEvent(UserCreatedEvent event) {
        log.info("Publishing user created event for user: {}", event.getUserId());
        try {
            String eventJson = JsonUtils.toJson(event);
            // kafkaTemplate.send(USER_CREATED_TOPIC, event.getUserId(), eventJson);
            log.debug("User created event published: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish user created event for user: {}", event.getUserId(), e);
        }
    }

    /**
     * Publish user updated event.
     *
     * @param event the event to publish
     */
    @Async("kafkaTaskExecutor")
    public void publishUserUpdatedEvent(UserUpdatedEvent event) {
        log.info("Publishing user updated event for user: {}", event.getUserId());
        try {
            String eventJson = JsonUtils.toJson(event);
            // kafkaTemplate.send(USER_UPDATED_TOPIC, event.getUserId(), eventJson);
            log.debug("User updated event published: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish user updated event for user: {}", event.getUserId(), e);
        }
    }

    /**
     * Publish user deactivated event.
     *
     * @param event the event to publish
     */
    @Async("kafkaTaskExecutor")
    public void publishUserDeactivatedEvent(UserDeactivatedEvent event) {
        log.info("Publishing user deactivated event for user: {}", event.getUserId());
        try {
            String eventJson = JsonUtils.toJson(event);
            // kafkaTemplate.send(USER_DEACTIVATED_TOPIC, event.getUserId(), eventJson);
            log.debug("User deactivated event published: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish user deactivated event for user: {}", event.getUserId(), e);
        }
    }

    /**
     * Publish user role assigned event.
     *
     * @param event the event to publish
     */
    @Async("kafkaTaskExecutor")
    public void publishUserRoleAssignedEvent(UserRoleAssignedEvent event) {
        log.info("Publishing user role assigned event for user: {}", event.getUserId());
        try {
            String eventJson = JsonUtils.toJson(event);
            // kafkaTemplate.send(USER_ROLE_ASSIGNED_TOPIC, event.getUserId(), eventJson);
            log.debug("User role assigned event published: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish user role assigned event for user: {}", event.getUserId(), e);
        }
    }
}
