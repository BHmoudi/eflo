package com.eflo.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusResponse {
    private String status;
    private int keycloakUserCount;
    private long databaseUserCount;
    private LocalDateTime lastCheck;
    private String message;
    private Map<String, Object> details;
}
