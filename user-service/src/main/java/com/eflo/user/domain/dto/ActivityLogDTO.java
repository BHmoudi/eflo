package com.eflo.user.domain.dto;

import com.eflo.user.domain.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private ActivityType activityType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime performedAt;
    private String performedBy;
    private Long businessUnitId;
    private String businessUnitName;
    private String metadata;
    private LocalDateTime createdAt;
}
