package com.eflo.workflow.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for delegating approvals
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelegationRequest {

    @NotNull(message = "Target user ID is required")
    private Long toUserId;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
