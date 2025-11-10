package com.eflo.user.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a new user is created.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private String eventId;

    private String userId;

    private String keycloakId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String createdBy;

    public static UserCreatedEvent of(String userId, String keycloakId, String username, String email,
                                      String firstName, String lastName, String phone, String createdBy) {
        return UserCreatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .keycloakId(keycloakId)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .isActive(true)
                .timestamp(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }
}
