package com.eflo.workflow.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Approval Level information
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalLevelResponse {

    private Long id;
    private Integer levelOrder;
    private String levelName;
    private String requiredRoleCode;
    private String requiredRoleName;
    private String approvalType;
    private Integer timeoutHours;
}
