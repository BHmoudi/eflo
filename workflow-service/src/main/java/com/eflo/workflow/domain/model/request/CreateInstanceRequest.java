package com.eflo.workflow.domain.model.request;

import com.eflo.workflow.domain.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a workflow instance
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstanceRequest {

    @NotNull(message = "Process ID is required")
    private Long processId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private String instanceName;

    @Builder.Default
    private Priority priority = Priority.NORMAL;

    private Map<String, Object> contextData;

    private Integer expectedDurationDays;
}
