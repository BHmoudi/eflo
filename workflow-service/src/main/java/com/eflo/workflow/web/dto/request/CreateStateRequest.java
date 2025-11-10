package com.eflo.workflow.web.dto.request;

import com.eflo.workflow.domain.enums.StateType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to create a workflow state
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStateRequest {

    @NotBlank(message = "State code is required")
    @Size(max = 50, message = "State code cannot exceed 50 characters")
    private String stateCode;

    @NotBlank(message = "State name is required")
    @Size(max = 255, message = "State name cannot exceed 255 characters")
    private String stateName;

    private String description;

    @NotNull(message = "State order is required")
    @Min(value = 1, message = "State order must be at least 1")
    private Integer stateOrder;

    @Builder.Default
    private StateType stateType = StateType.NORMAL;

    private Integer expectedDurationHours;

    @Builder.Default
    private Boolean isFinalState = false;

    @Builder.Default
    private Boolean requiresApproval = false;

    @Builder.Default
    private Boolean allowSkip = false;

    private Map<String, Object> configuration;
}
