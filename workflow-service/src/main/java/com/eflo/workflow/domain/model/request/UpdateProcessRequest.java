package com.eflo.workflow.domain.model.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing workflow process
 *
 * All fields are optional - only provided fields will be updated
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcessRequest {

    private String processName;

    private String description;

    @Pattern(regexp = "^(VN|VO|EVO)$", message = "Order type must be VN, VO, or EVO")
    private String orderType;

    @Positive(message = "Max duration must be positive")
    private Integer maxDurationDays;

    private Boolean autoProgressEnabled;

    private Boolean parallelExecutionAllowed;

    private Map<String, Object> configuration;
}
