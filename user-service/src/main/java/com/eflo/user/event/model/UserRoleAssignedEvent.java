package com.eflo.user.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a role is assigned to a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignedEvent {

    private String eventId;

    private String userId;

    private String roleId;

    private String roleName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String assignedBy;

    public static UserRoleAssignedEvent of(String userId, String roleId, String roleName, String assignedBy) {
        return UserRoleAssignedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .roleId(roleId)
                .roleName(roleName)
                .timestamp(LocalDateTime.now())
                .assignedBy(assignedBy)
                .build();
    }
}
