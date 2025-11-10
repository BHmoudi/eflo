package com.eflo.workflow.web;

import com.eflo.workflow.domain.WorkflowTaskApproval;
import com.eflo.workflow.service.ApprovalExecutionService;
import com.eflo.workflow.web.dto.request.ApprovalRequest;
import com.eflo.workflow.web.dto.request.DelegationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Approval Controller
 *
 * REST API for managing workflow approvals.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@CrossOrigin
public class ApprovalController {

    private final ApprovalExecutionService approvalExecutionService;

    /**
     * Get pending approvals for a user
     *
     * @param userId User ID from header
     * @return List of pending approval executions
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowTaskApproval>> getPendingApprovals(
            @RequestHeader("X-User-Id") Long userId) {
        log.debug("REST request to get pending approvals for user: {}", userId);
        List<WorkflowTaskApproval> pendingApprovals = approvalExecutionService.getPendingApprovals(userId);
        return ResponseEntity.ok(pendingApprovals);
    }

    /**
     * Approve an approval request
     *
     * @param approvalId Approval execution ID
     * @param userId User ID from header
     * @param request Approval request with comments
     * @return Updated approval execution
     */
    @PostMapping("/{approvalId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowTaskApproval> approve(
            @PathVariable Long approvalId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ApprovalRequest request) {
        log.debug("REST request to approve approval {} by user {}", approvalId, userId);
        WorkflowTaskApproval approvalExecution = approvalExecutionService.approve(
                approvalId, userId, request.getComments());
        return ResponseEntity.ok(approvalExecution);
    }

    /**
     * Reject an approval request
     *
     * @param approvalId Approval execution ID
     * @param userId User ID from header
     * @param request Approval request with comments
     * @return Updated approval execution
     */
    @PostMapping("/{approvalId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowTaskApproval> reject(
            @PathVariable Long approvalId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ApprovalRequest request) {
        log.debug("REST request to reject approval {} by user {}", approvalId, userId);
        WorkflowTaskApproval approvalExecution = approvalExecutionService.reject(
                approvalId, userId, request.getComments());
        return ResponseEntity.ok(approvalExecution);
    }

    /**
     * Delegate an approval to another user
     *
     * @param approvalId Approval execution ID
     * @param userId User ID from header
     * @param request Delegation request with target user and reason
     * @return Updated approval execution
     */
    @PostMapping("/{approvalId}/delegate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowTaskApproval> delegate(
            @PathVariable Long approvalId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DelegationRequest request) {
        log.debug("REST request to delegate approval {} from user {} to user {}",
                approvalId, userId, request.getToUserId());
        WorkflowTaskApproval approvalExecution = approvalExecutionService.delegate(
                approvalId, userId, request.getToUserId(), request.getReason());
        return ResponseEntity.ok(approvalExecution);
    }
}
