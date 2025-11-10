package com.eflo.user.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a user is deactivated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeactivatedEvent {

    private String eventId;

    private String userId;

    private String keycloakId;

    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String deactivatedBy;

    public static UserDeactivatedEvent of(String userId, String keycloakId, String reason, String deactivatedBy) {
        return UserDeactivatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .keycloakId(keycloakId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .deactivatedBy(deactivatedBy)
                .build();
    }
}
