package com.eflo.workflow.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Approval Chain information
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalChainResponse {

    private Long id;
    private String chainCode;
    private String chainName;
    private String description;
    private List<ApprovalLevelResponse> levels;
    private Boolean isActive;
}
