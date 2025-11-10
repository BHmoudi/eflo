package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for completing a workflow task in Workflow Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {

    private String completedBy;
    private String completionNotes;
    private Map<String, Object> completionData;
    private Boolean success;
}
