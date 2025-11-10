package com.eflo.workflow.web.dto.request;

import com.eflo.workflow.domain.enums.TransitionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to create a workflow transition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransitionRequest {

    @NotBlank(message = "Transition name is required")
    @Size(max = 255, message = "Transition name cannot exceed 255 characters")
    private String transitionName;

    @NotBlank(message = "From state code is required")
    private String fromStateCode;

    @NotBlank(message = "To state code is required")
    private String toStateCode;

    @Builder.Default
    private TransitionType transitionType = TransitionType.NORMAL;

    @Builder.Default
    private Boolean requiresApproval = false;

    @Builder.Default
    private Boolean autoTransition = false;

    private String conditionExpression;

    private String requiredRoleCode;

    private Map<String, Object> configuration;
}
