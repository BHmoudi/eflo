package com.eflo.user.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event published when a user is updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    private String eventId;

    private String userId;

    private String keycloakId;

    private Map<String, Object> changes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String updatedBy;

    public static UserUpdatedEvent of(String userId, String keycloakId, Map<String, Object> changes, String updatedBy) {
        return UserUpdatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .keycloakId(keycloakId)
                .changes(changes)
                .timestamp(LocalDateTime.now())
                .updatedBy(updatedBy)
                .build();
    }
}
