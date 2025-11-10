package com.eflo.workflow.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for workflow process
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponse {

    private Long id;
    private String processCode;
    private String processName;
    private String description;
    private String orderType;
    private Integer processVersion;
    private Boolean isActive;
    private Integer maxDurationDays;
    private Boolean autoProgressEnabled;
    private Boolean parallelExecutionAllowed;
    private Map<String, Object> configuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
