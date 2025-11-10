package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowApprovalChain;
import com.eflo.workflow.domain.WorkflowApprovalLevel;
import com.eflo.workflow.domain.WorkflowNotification;
import com.eflo.workflow.domain.WorkflowTaskApproval;
import com.eflo.workflow.domain.WorkflowTaskHistory;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.ApprovalStatus;
import com.eflo.workflow.domain.enums.NotificationChannel;
import com.eflo.workflow.domain.enums.TaskActionType;
import com.eflo.workflow.domain.repository.WorkflowApprovalChainRepository;
import com.eflo.workflow.domain.repository.WorkflowApprovalLevelRepository;
import com.eflo.workflow.domain.repository.WorkflowNotificationRepository;
import com.eflo.workflow.domain.repository.WorkflowTaskApprovalRepository;
import com.eflo.workflow.domain.repository.WorkflowTaskHistoryRepository;
import com.eflo.workflow.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ApprovalExecutionService
 *
 * Handles approval workflow execution including starting approval processes,
 * processing approvals/rejections, and managing delegations.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalExecutionService {

    private final WorkflowTaskApprovalRepository taskApprovalRepository;
    private final WorkflowApprovalChainRepository approvalChainRepository;
    private final WorkflowApprovalLevelRepository approvalLevelRepository;
    private final UserRoleService userRoleService;
    private final WorkflowNotificationRepository notificationRepository;
    private final WorkflowTaskHistoryRepository taskHistoryRepository;

    /**
     * Start approval process for a task
     *
     * @param task Task requiring approval
     * @param chain Approval chain to use
     * @throws WorkflowException if chain has no levels
     */
    @Transactional
    public void startApprovalProcess(WorkflowInstanceTask task, WorkflowApprovalChain chain) {
        log.info("Starting approval process for task ID: {} with chain: {}", task.getId(), chain.getChainCode());

        List<WorkflowApprovalLevel> levels = approvalLevelRepository.findByChainIdOrderByLevelOrderAsc(chain.getId());

        if (levels.isEmpty()) {
            log.error("Approval chain {} has no levels configured", chain.getChainCode());
            throw new WorkflowException("Approval chain " + chain.getChainCode() + " has no levels configured", "CHAIN_NO_LEVELS");
        }

        // Create approval records for each level
        for (WorkflowApprovalLevel level : levels) {
            List<Long> eligibleApprovers = getEligibleApprovers(level, task);

            if (eligibleApprovers.isEmpty()) {
                log.warn("No eligible approvers found for level {} in chain {}", level.getLevelName(), chain.getChainCode());
                continue;
            }

            // Create approval record for each eligible approver
            for (Long approverId : eligibleApprovers) {
                WorkflowTaskApproval approval = WorkflowTaskApproval.builder()
                        .instanceTask(task)
                        .approvalChain(chain)
                        .approvalLevel(level)
                        .levelOrder(level.getLevelOrder())
                        .approverUserId(approverId)
                        .approverRole(level.getRequiredRole())
                        .approvalStatus(ApprovalStatus.PENDING)
                        .build();

                // Set timeout if configured
                if (level.getTimeoutHours() != null) {
                    approval.setTimeoutAt(LocalDateTime.now().plusHours(level.getTimeoutHours()));
                }

                taskApprovalRepository.save(approval);

                // Send notification if configured
                if (Boolean.TRUE.equals(level.getNotifyOnAssignment())) {
                    sendApprovalNotification(approval, task);
                }

                log.debug("Created approval record for user {} at level {}", approverId, level.getLevelName());
            }
        }

        // Record history
        recordHistory(task, TaskActionType.APPROVAL_STARTED, null, "Approval process started with chain: " + chain.getChainName());

        log.info("Successfully started approval process for task ID: {} with {} levels", task.getId(), levels.size());
    }

    /**
     * Approve a task
     *
     * @param approvalId Approval record ID
     * @param userId User ID performing the approval
     * @param comments Approval comments
     * @throws WorkflowException if approval not found or already processed
     */
    @Transactional
    public void approveTask(Long approvalId, Long userId, String comments) {
        log.info("Processing approval for approval ID: {} by user: {}", approvalId, userId);

        WorkflowTaskApproval approval = taskApprovalRepository.findById(approvalId)
                .orElseThrow(() -> {
                    log.error("Approval not found with ID: {}", approvalId);
                    return new WorkflowException("Approval not found with ID: " + approvalId, "APPROVAL_NOT_FOUND");
                });

        // Validate approver
        if (!approval.getApproverUserId().equals(userId)) {
            log.error("User {} is not authorized to approve approval ID: {}", userId, approvalId);
            throw new WorkflowException("User not authorized to approve this task", "UNAUTHORIZED_APPROVER");
        }

        // Check if already processed
        if (!approval.isPending()) {
            log.error("Approval ID: {} is already processed with status: {}", approvalId, approval.getApprovalStatus());
            throw new WorkflowException("Approval already processed with status: " + approval.getApprovalStatus(), "APPROVAL_ALREADY_PROCESSED");
        }

        // Approve
        approval.approve(comments);
        taskApprovalRepository.save(approval);

        // Record history
        recordHistory(approval.getInstanceTask(), TaskActionType.APPROVED, userId,
                "Approved at level " + approval.getLevelOrder() + ": " + (comments != null ? comments : ""));

        log.info("Successfully approved task approval ID: {} by user: {}", approvalId, userId);
    }

    /**
     * Reject a task
     *
     * @param approvalId Approval record ID
     * @param userId User ID performing the rejection
     * @param comments Rejection comments
     * @throws WorkflowException if approval not found or already processed
     */
    @Transactional
    public void rejectTask(Long approvalId, Long userId, String comments) {
        log.info("Processing rejection for approval ID: {} by user: {}", approvalId, userId);

        WorkflowTaskApproval approval = taskApprovalRepository.findById(approvalId)
                .orElseThrow(() -> {
                    log.error("Approval not found with ID: {}", approvalId);
                    return new WorkflowException("Approval not found with ID: " + approvalId, "APPROVAL_NOT_FOUND");
                });

        // Validate approver
        if (!approval.getApproverUserId().equals(userId)) {
            log.error("User {} is not authorized to reject approval ID: {}", userId, approvalId);
            throw new WorkflowException("User not authorized to reject this task", "UNAUTHORIZED_APPROVER");
        }

        // Check if already processed
        if (!approval.isPending()) {
            log.error("Approval ID: {} is already processed with status: {}", approvalId, approval.getApprovalStatus());
            throw new WorkflowException("Approval already processed with status: " + approval.getApprovalStatus(), "APPROVAL_ALREADY_PROCESSED");
        }

        // Reject
        approval.reject(comments);
        taskApprovalRepository.save(approval);

        // Record history
        recordHistory(approval.getInstanceTask(), TaskActionType.REJECTED, userId,
                "Rejected at level " + approval.getLevelOrder() + ": " + (comments != null ? comments : ""));

        log.info("Successfully rejected task approval ID: {} by user: {}", approvalId, userId);
    }

    /**
     * Delegate approval to another user
     *
     * @param approvalId Approval record ID
     * @param fromUserId User ID delegating the approval
     * @param toUserId User ID to delegate to
     * @throws WorkflowException if approval not found or already processed
     */
    @Transactional
    public void delegateApproval(Long approvalId, Long fromUserId, Long toUserId) {
        log.info("Delegating approval ID: {} from user: {} to user: {}", approvalId, fromUserId, toUserId);

        WorkflowTaskApproval approval = taskApprovalRepository.findById(approvalId)
                .orElseThrow(() -> {
                    log.error("Approval not found with ID: {}", approvalId);
                    return new WorkflowException("Approval not found with ID: " + approvalId, "APPROVAL_NOT_FOUND");
                });

        // Validate delegator
        if (!approval.getApproverUserId().equals(fromUserId)) {
            log.error("User {} is not authorized to delegate approval ID: {}", fromUserId, approvalId);
            throw new WorkflowException("User not authorized to delegate this approval", "UNAUTHORIZED_DELEGATOR");
        }

        // Check if already processed
        if (!approval.isPending()) {
            log.error("Approval ID: {} is already processed with status: {}", approvalId, approval.getApprovalStatus());
            throw new WorkflowException("Approval already processed with status: " + approval.getApprovalStatus(), "APPROVAL_ALREADY_PROCESSED");
        }

        // Delegate
        approval.delegate(toUserId);
        taskApprovalRepository.save(approval);

        // Record history
        recordHistory(approval.getInstanceTask(), TaskActionType.DELEGATED, fromUserId,
                "Delegated approval to user ID: " + toUserId);

        log.info("Successfully delegated approval ID: {} from user: {} to user: {}", approvalId, fromUserId, toUserId);
    }

    /**
     * Get pending approvals for a user
     *
     * @param userId User ID
     * @return List of pending approvals
     */
    public List<WorkflowTaskApproval> getPendingApprovalsForUser(Long userId) {
        log.debug("Retrieving pending approvals for user: {}", userId);
        List<WorkflowTaskApproval> approvals = taskApprovalRepository.findPendingApprovalsByUser(userId);
        log.debug("Found {} pending approvals for user: {}", approvals.size(), userId);
        return approvals;
    }

    /**
     * Check if approval is complete for a task
     *
     * @param task Task to check
     * @return true if all required approvals are complete
     */
    public boolean isApprovalComplete(WorkflowInstanceTask task) {
        log.debug("Checking if approval is complete for task ID: {}", task.getId());

        List<WorkflowTaskApproval> approvals = taskApprovalRepository.findByInstanceTaskId(task.getId());

        if (approvals.isEmpty()) {
            log.debug("No approvals found for task ID: {}", task.getId());
            return false;
        }

        // Get unique levels
        List<Integer> levels = approvals.stream()
                .map(WorkflowTaskApproval::getLevelOrder)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Check each level
        for (Integer level : levels) {
            List<WorkflowTaskApproval> levelApprovals = approvals.stream()
                    .filter(a -> a.getLevelOrder().equals(level))
                    .collect(Collectors.toList());

            // Check if any approval in this level is approved
            boolean levelApproved = levelApprovals.stream()
                    .anyMatch(a -> ApprovalStatus.APPROVED.equals(a.getApprovalStatus()));

            // Check if any approval in this level is rejected
            boolean levelRejected = levelApprovals.stream()
                    .anyMatch(a -> ApprovalStatus.REJECTED.equals(a.getApprovalStatus()));

            if (levelRejected) {
                log.debug("Approval rejected at level {} for task ID: {}", level, task.getId());
                return false;
            }

            if (!levelApproved) {
                log.debug("Approval pending at level {} for task ID: {}", level, task.getId());
                return false;
            }
        }

        log.debug("All approval levels complete for task ID: {}", task.getId());
        return true;
    }

    /**
     * Get current approval level for a task
     *
     * @param task Task to check
     * @return Current approval level number, or null if no approvals
     */
    public Integer getCurrentApprovalLevel(WorkflowInstanceTask task) {
        log.debug("Getting current approval level for task ID: {}", task.getId());

        List<WorkflowTaskApproval> approvals = taskApprovalRepository.findByInstanceTaskId(task.getId());

        if (approvals.isEmpty()) {
            log.debug("No approvals found for task ID: {}", task.getId());
            return null;
        }

        // Find the first level that is not approved
        return approvals.stream()
                .filter(a -> ApprovalStatus.PENDING.equals(a.getApprovalStatus()))
                .map(WorkflowTaskApproval::getLevelOrder)
                .min(Integer::compareTo)
                .orElse(null);
    }

    /**
     * Get user IDs who can approve the next level
     *
     * @param task Task to check
     * @return List of user IDs who can approve next
     */
    public List<Long> getNextApprovers(WorkflowInstanceTask task) {
        log.debug("Getting next approvers for task ID: {}", task.getId());

        Integer currentLevel = getCurrentApprovalLevel(task);

        if (currentLevel == null) {
            log.debug("No pending approval level for task ID: {}", task.getId());
            return new ArrayList<>();
        }

        List<WorkflowTaskApproval> approvals = taskApprovalRepository.findByInstanceTaskId(task.getId());

        List<Long> approvers = approvals.stream()
                .filter(a -> a.getLevelOrder().equals(currentLevel))
                .filter(a -> ApprovalStatus.PENDING.equals(a.getApprovalStatus()))
                .map(WorkflowTaskApproval::getApproverUserId)
                .collect(Collectors.toList());

        log.debug("Found {} approvers for level {} of task ID: {}", approvers.size(), currentLevel, task.getId());
        return approvers;
    }

    /**
     * Get eligible approvers for a level based on role requirements
     */
    private List<Long> getEligibleApprovers(WorkflowApprovalLevel level, WorkflowInstanceTask task) {
        if (level.getRequiredRole() == null) {
            log.warn("Approval level {} has no required role defined", level.getLevelName());
            return new ArrayList<>();
        }

        String roleCode = level.getRequiredRole().getRoleCode();
        List<Long> users = userRoleService.getUsersByRole(roleCode);

        log.debug("Found {} eligible approvers with role {} for level {}", users.size(), roleCode, level.getLevelName());
        return users;
    }

    /**
     * Send notification for approval assignment
     */
    private void sendApprovalNotification(WorkflowTaskApproval approval, WorkflowInstanceTask task) {
        WorkflowNotification notification = WorkflowNotification.builder()
                .instanceTask(task)
                .recipientUserId(approval.getApproverUserId())
                .notificationType("APPROVAL_REQUIRED")
                .notificationChannel(NotificationChannel.IN_APP)
                .subject("Approval Required: " + task.getTaskName())
                .message("You have been assigned to approve task: " + task.getTaskName())
                .build();

        notificationRepository.save(notification);
        log.debug("Sent approval notification to user: {}", approval.getApproverUserId());
    }

    /**
     * Get pending approvals for a user (alias for getPendingApprovalsForUser)
     *
     * @param userId User ID
     * @return List of pending approvals
     */
    public List<WorkflowTaskApproval> getPendingApprovals(Long userId) {
        return getPendingApprovalsForUser(userId);
    }

    /**
     * Approve a task (alias for approveTask) that returns the approval
     *
     * @param approvalId Approval record ID
     * @param userId User ID performing the approval
     * @param comments Approval comments
     * @return The approved WorkflowTaskApproval
     */
    @Transactional
    public WorkflowTaskApproval approve(Long approvalId, Long userId, String comments) {
        approveTask(approvalId, userId, comments);
        return taskApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new WorkflowException("Approval not found", "APPROVAL_NOT_FOUND"));
    }

    /**
     * Reject a task (alias for rejectTask) that returns the approval
     *
     * @param approvalId Approval record ID
     * @param userId User ID performing the rejection
     * @param comments Rejection comments
     * @return The rejected WorkflowTaskApproval
     */
    @Transactional
    public WorkflowTaskApproval reject(Long approvalId, Long userId, String comments) {
        rejectTask(approvalId, userId, comments);
        return taskApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new WorkflowException("Approval not found", "APPROVAL_NOT_FOUND"));
    }

    /**
     * Delegate approval to another user (returns the approval)
     *
     * @param approvalId Approval record ID
     * @param userId User ID delegating the approval
     * @param toUserId User ID to delegate to
     * @param reason Reason for delegation
     * @return The delegated WorkflowTaskApproval
     */
    @Transactional
    public WorkflowTaskApproval delegate(Long approvalId, Long userId, Long toUserId, String reason) {
        delegateApproval(approvalId, userId, toUserId);
        return taskApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new WorkflowException("Approval not found", "APPROVAL_NOT_FOUND"));
    }

    /**
     * Record task history entry
     */
    private void recordHistory(WorkflowInstanceTask task, TaskActionType actionType, Long userId, String comments) {
        WorkflowTaskHistory history = WorkflowTaskHistory.builder()
                .instanceTask(task)
                .actionType(actionType)
                .actionByUserId(userId != null ? userId : 0L)
                .comments(comments)
                .build();

        taskHistoryRepository.save(history);
    }
}
