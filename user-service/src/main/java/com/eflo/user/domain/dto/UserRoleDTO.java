package com.eflo.user.domain.dto;

import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
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
public class UserRoleDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private UserRoleEnum roleName;
    private RoleSource source;
    private Long businessUnitId;
    private String businessUnitName;
    private Boolean isActive;
    private LocalDate assignedAt;
    private LocalDate revokedAt;
    private String assignedBy;
    private String revokedBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
