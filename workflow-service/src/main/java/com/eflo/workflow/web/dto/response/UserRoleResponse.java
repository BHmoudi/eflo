package com.eflo.workflow.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for User Role information
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponse {

    private Long id;
    private Long userId;
    private String roleCode;
    private String roleName;
    private String affaireCode;
    private String branchCode;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean isActive;
}
