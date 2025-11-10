package com.eflo.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBusinessUnitDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Long businessUnitId;
    private String businessUnitCode;
    private String businessUnitName;
    private Boolean isPrimary;
    private Boolean isActive;
    private LocalDate assignedAt;
    private LocalDate removedAt;
    private String assignedBy;
    private String removedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
