package com.eflo.user.domain.dto;

import com.eflo.user.domain.enums.UserRoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role name is required")
    private UserRoleEnum roleName;

    private Long businessUnitId;
    private String notes;
}
