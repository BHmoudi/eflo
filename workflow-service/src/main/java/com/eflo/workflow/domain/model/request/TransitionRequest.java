package com.eflo.workflow.domain.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for executing a workflow transition
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionRequest {

    @NotNull(message = "Target state ID is required")
    private Long toStateId;

    private String comment;

    private Map<String, Object> contextData;

    private Boolean forceTransition;
}
