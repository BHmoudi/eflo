package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowTaskHistory;
import com.eflo.workflow.domain.entity.WorkflowInstance;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.entity.WorkflowState;
import com.eflo.workflow.domain.entity.WorkflowStateTask;
import com.eflo.workflow.domain.enums.TaskActionType;
import com.eflo.workflow.domain.enums.TaskStatus;
import com.eflo.workflow.domain.repository.WorkflowInstanceTaskRepository;
import com.eflo.workflow.domain.repository.WorkflowStateTaskRepository;
import com.eflo.workflow.domain.repository.WorkflowTaskHistoryRepository;
import com.eflo.workflow.domain.repository.WorkflowUserRoleRepository;
import com.eflo.workflow.exception.TaskNotFoundException;
import com.eflo.workflow.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TaskAssignmentService
 *
 * Handles automatic task assignment based on roles, user availability,
 * and workload balancing.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskAssignmentService {

    private final WorkflowInstanceTaskRepository instanceTaskRepository;
    private final WorkflowStateTaskRepository stateTaskRepository;
    private final UserRoleService userRoleService;
    private final WorkflowUserRoleRepository userRoleRepository;
    private final WorkflowTaskHistoryRepository taskHistoryRepository;

    /**
     * Auto-assign all tasks for a workflow state
     *
     * @param instance Workflow instance
     * @param state Workflow state
     */
    @Transactional
    public void assignTasksForState(WorkflowInstance instance, WorkflowState state) {
        log.info("Auto-assigning tasks for instance ID: {} in state: {}", instance.getId(), state.getStateName());

        List<WorkflowStateTask> stateTasks = stateTaskRepository.findByStateIdOrderByTaskOrderAsc(state.getId());

        int assignedCount = 0;
        for (WorkflowStateTask stateTask : stateTasks) {
            // Skip if auto-assign is disabled
            if (!Boolean.TRUE.equals(stateTask.getAutoAssign())) {
                log.debug("Auto-assign disabled for task: {}", stateTask.getTaskCode());
                continue;
            }

            // Skip if already has specific user assignment
            if (stateTask.getAssignedToUserId() != null) {
                log.debug("Task {} has specific user assignment: {}", stateTask.getTaskCode(), stateTask.getAssignedToUserId());
                assignTaskToSpecificUser(instance, stateTask, stateTask.getAssignedToUserId());
                assignedCount++;
                continue;
            }

            // Get eligible users based on role
            if (stateTask.getAssignedToRole() != null) {
                List<Long> eligibleUsers = getEligibleUsers(stateTask, instance.getContextData() != null ?
                        (String) instance.getContextData().get("affaireCode") : null);

                if (eligibleUsers.isEmpty()) {
                    log.warn("No eligible users found for task {} with role: {}",
                            stateTask.getTaskCode(), stateTask.getAssignedToRole());
                    continue;
                }

                // Find least busy user
                Long selectedUser = findLeastBusyUser(eligibleUsers);
                assignTaskToUser(instance, stateTask, selectedUser);
                assignedCount++;
            } else {
                log.warn("Task {} has no role or user assignment defined", stateTask.getTaskCode());
            }
        }

        log.info("Successfully auto-assigned {} tasks for instance ID: {} in state: {}",
                assignedCount, instance.getId(), state.getStateName());
    }

    /**
     * Get eligible users for a state task based on role requirements
     *
     * @param stateTask State task definition
     * @param affaireCode Affaire code for context-based role filtering
     * @return List of eligible user IDs
     */
    public List<Long> getEligibleUsers(WorkflowStateTask stateTask, String affaireCode) {
        log.debug("Getting eligible users for task: {} with role: {}",
                stateTask.getTaskCode(), stateTask.getAssignedToRole());

        if (stateTask.getAssignedToRole() == null) {
            log.warn("Task {} has no assigned role", stateTask.getTaskCode());
            return new ArrayList<>();
        }

        // Get all users with the required role
        List<Long> users = userRoleService.getUsersByRole(stateTask.getAssignedToRole());

        // Filter by affaire code if provided
        if (affaireCode != null && !affaireCode.isEmpty()) {
            users = users.stream()
                    .filter(userId -> hasRoleForAffaire(userId, stateTask.getAssignedToRole(), affaireCode))
                    .collect(Collectors.toList());
        }

        log.debug("Found {} eligible users for task: {}", users.size(), stateTask.getTaskCode());
        return users;
    }

    /**
     * Reassign a task to a different user
     *
     * @param instanceTaskId Instance task ID
     * @param newUserId New user ID to assign to
     * @param reason Reason for reassignment
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public void reassignTask(Long instanceTaskId, Long newUserId, String reason) {
        log.info("Reassigning task ID: {} to user: {} (reason: {})", instanceTaskId, newUserId, reason);

        WorkflowInstanceTask task = instanceTaskRepository.findById(instanceTaskId)
                .orElseThrow(() -> new TaskNotFoundException(instanceTaskId));

        Long oldUserId = task.getAssignedToUserId();

        if (oldUserId != null && oldUserId.equals(newUserId)) {
            log.warn("Task ID: {} is already assigned to user: {}", instanceTaskId, newUserId);
            throw new WorkflowException("Task is already assigned to this user", "ALREADY_ASSIGNED");
        }

        // Update assignment
        task.setAssignedToUserId(newUserId);
        task.setAssignedAt(LocalDateTime.now());
        task.setAssignedBy("system");

        // If task was in progress, move it back to assigned
        if (TaskStatus.RECU.equals(task.getTaskStatus())) {
            task.setTaskStatus(TaskStatus.ASSIGNED);
        }

        instanceTaskRepository.save(task);

        // Record history
        recordHistory(task, TaskActionType.REASSIGNED, newUserId, oldUserId, newUserId, reason);

        log.info("Successfully reassigned task ID: {} from user: {} to user: {}",
                instanceTaskId, oldUserId, newUserId);
    }

    /**
     * Calculate task load for a user
     *
     * @param userId User ID
     * @return Number of assigned active tasks
     */
    public int calculateTaskLoad(Long userId) {
        log.debug("Calculating task load for user: {}", userId);

        List<TaskStatus> activeStatuses = List.of(TaskStatus.ASSIGNED, TaskStatus.RECU);
        long taskCount = instanceTaskRepository.countByAssignedToUserIdAndTaskStatusIn(userId, activeStatuses);

        log.debug("User {} has {} active tasks", userId, taskCount);
        return (int) taskCount;
    }

    /**
     * Find the least busy user from a list of eligible users
     *
     * @param eligibleUsers List of eligible user IDs
     * @return User ID with least tasks, or null if list is empty
     */
    public Long findLeastBusyUser(List<Long> eligibleUsers) {
        log.debug("Finding least busy user from {} eligible users", eligibleUsers.size());

        if (eligibleUsers.isEmpty()) {
            log.warn("No eligible users to find least busy user");
            return null;
        }

        // Calculate task load for each user
        Map<Long, Integer> userLoads = new java.util.HashMap<>();
        for (Long userId : eligibleUsers) {
            userLoads.put(userId, calculateTaskLoad(userId));
        }

        // Find user with minimum load
        Long leastBusyUser = userLoads.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(eligibleUsers.get(0));

        log.debug("Selected least busy user: {} with {} tasks", leastBusyUser, userLoads.get(leastBusyUser));
        return leastBusyUser;
    }

    /**
     * Assign a task to a specific user
     */
    private void assignTaskToSpecificUser(WorkflowInstance instance, WorkflowStateTask stateTask, Long userId) {
        log.debug("Assigning task {} to specific user: {}", stateTask.getTaskCode(), userId);

        List<WorkflowInstanceTask> existingTasks = instanceTaskRepository
                .findByInstanceIdAndStateTaskId(instance.getId(), stateTask.getId());

        WorkflowInstanceTask instanceTask;
        if (existingTasks.isEmpty()) {
            // Create new instance task
            instanceTask = createInstanceTask(instance, stateTask);
        } else {
            instanceTask = existingTasks.get(0);
        }

        instanceTask.setAssignedToUserId(userId);
        instanceTask.setAssignedAt(LocalDateTime.now());
        instanceTask.setAssignedBy("system");
        instanceTask.setTaskStatus(TaskStatus.ASSIGNED);

        instanceTaskRepository.save(instanceTask);

        // Record history
        recordHistory(instanceTask, TaskActionType.ASSIGNED, userId, null, userId, "Auto-assigned to specific user");
    }

    /**
     * Assign a task to a user based on role
     */
    private void assignTaskToUser(WorkflowInstance instance, WorkflowStateTask stateTask, Long userId) {
        log.debug("Assigning task {} to user: {} based on role: {}",
                stateTask.getTaskCode(), userId, stateTask.getAssignedToRole());

        List<WorkflowInstanceTask> existingTasks = instanceTaskRepository
                .findByInstanceIdAndStateTaskId(instance.getId(), stateTask.getId());

        WorkflowInstanceTask instanceTask;
        if (existingTasks.isEmpty()) {
            // Create new instance task
            instanceTask = createInstanceTask(instance, stateTask);
        } else {
            instanceTask = existingTasks.get(0);
        }

        instanceTask.setAssignedToUserId(userId);
        instanceTask.setAssignedToRole(stateTask.getAssignedToRole());
        instanceTask.setAssignedAt(LocalDateTime.now());
        instanceTask.setAssignedBy("system");
        instanceTask.setTaskStatus(TaskStatus.ASSIGNED);

        // Set expected completion time if duration is defined
        if (stateTask.getExpectedDurationHours() != null) {
            instanceTask.setExpectedCompletionAt(
                    LocalDateTime.now().plusHours(stateTask.getExpectedDurationHours())
            );
        }

        instanceTaskRepository.save(instanceTask);

        // Record history
        recordHistory(instanceTask, TaskActionType.ASSIGNED, userId, null, userId,
                "Auto-assigned based on role: " + stateTask.getAssignedToRole());
    }

    /**
     * Create a new instance task from state task template
     */
    private WorkflowInstanceTask createInstanceTask(WorkflowInstance instance, WorkflowStateTask stateTask) {
        return WorkflowInstanceTask.builder()
                .instance(instance)
                .stateTask(stateTask)
                .taskCode(stateTask.getTaskCode())
                .taskName(stateTask.getTaskName())
                .taskStatus(TaskStatus.EN_ATTENTE)
                .isOverdue(false)
                .escalationLevel(0)
                .build();
    }

    /**
     * Check if user has role for specific affaire
     */
    private boolean hasRoleForAffaire(Long userId, String roleCode, String affaireCode) {
        return userRoleRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .anyMatch(ur -> ur.getRole().getRoleCode().equals(roleCode) &&
                        (ur.getAffaireCode() == null || ur.getAffaireCode().equals(affaireCode)) &&
                        ur.isCurrentlyValid());
    }

    /**
     * Record task history entry
     */
    private void recordHistory(WorkflowInstanceTask task, TaskActionType actionType, Long userId,
                                Long oldAssignee, Long newAssignee, String comments) {
        WorkflowTaskHistory history = WorkflowTaskHistory.builder()
                .instanceTask(task)
                .actionType(actionType)
                .actionByUserId(userId != null ? userId : 0L)
                .oldAssigneeUserId(oldAssignee)
                .newAssigneeUserId(newAssignee)
                .comments(comments)
                .build();

        taskHistoryRepository.save(history);
    }
}
