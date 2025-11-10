package com.eflo.workflow.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Approval information
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {

    private Long id;
    private Long taskId;
    private Integer levelOrder;
    private Long approverUserId;
    private String approverUserName;
    private String status;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
