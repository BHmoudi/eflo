package com.eflo.workflow.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new workflow process
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProcessRequest {

    @NotBlank(message = "Process code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Process code must contain only uppercase letters, numbers, and underscores")
    private String processCode;

    @NotBlank(message = "Process name is required")
    private String processName;

    private String description;

    @NotBlank(message = "Order type is required")
    @Pattern(regexp = "^(VN|VO|EVO)$", message = "Order type must be VN, VO, or EVO")
    private String orderType;

    @Positive(message = "Max duration must be positive")
    private Integer maxDurationDays;

    @Builder.Default
    private Boolean autoProgressEnabled = false;

    @Builder.Default
    private Boolean parallelExecutionAllowed = false;

    private Map<String, Object> configuration;
}
