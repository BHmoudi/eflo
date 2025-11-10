package com.eflo.user.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Business Unit ID is required")
    private Long businessUnitId;

    @Builder.Default
    private Boolean isPrimary = false;
}
