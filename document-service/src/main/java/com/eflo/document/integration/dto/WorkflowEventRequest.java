package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for publishing workflow events to Workflow Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEventRequest {

    private String eventType;
    private String eventSource;
    private Map<String, Object> eventData;
    private String triggeredBy;
}
