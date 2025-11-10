package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowNotification;
import com.eflo.workflow.domain.WorkflowTaskCondition;
import com.eflo.workflow.domain.WorkflowTaskDependency;
import com.eflo.workflow.domain.WorkflowTaskHistory;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.NotificationChannel;
import com.eflo.workflow.domain.enums.TaskActionType;
import com.eflo.workflow.domain.enums.TaskStatus;
import com.eflo.workflow.domain.repository.WorkflowInstanceTaskRepository;
import com.eflo.workflow.domain.repository.WorkflowNotificationRepository;
import com.eflo.workflow.domain.repository.WorkflowTaskConditionRepository;
import com.eflo.workflow.domain.repository.WorkflowTaskDependencyRepository;
import com.eflo.workflow.domain.repository.WorkflowTaskHistoryRepository;
import com.eflo.workflow.exception.TaskNotFoundException;
import com.eflo.workflow.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TaskExecutionService
 *
 * Manages task execution lifecycle including starting, completing tasks,
 * handling documents, and evaluating dependencies and conditions.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskExecutionService {

    private final WorkflowInstanceTaskRepository instanceTaskRepository;
    private final WorkflowTaskHistoryRepository taskHistoryRepository;
    private final WorkflowTaskDependencyRepository taskDependencyRepository;
    private final WorkflowTaskConditionRepository taskConditionRepository;
    private final ApprovalExecutionService approvalExecutionService;
    private final WorkflowNotificationRepository notificationRepository;

    /**
     * Start a task (mark as RECU)
     *
     * @param instanceTaskId Instance task ID
     * @param userId User ID starting the task
     * @return The started task
     * @throws TaskNotFoundException if task not found
     * @throws WorkflowException if task cannot be started
     */
    @Transactional
    public WorkflowInstanceTask startTask(Long instanceTaskId, Long userId) {
        log.info("Starting task ID: {} by user: {}", instanceTaskId, userId);

        WorkflowInstanceTask task = instanceTaskRepository.findById(instanceTaskId)
                .orElseThrow(() -> new TaskNotFoundException(instanceTaskId));

        // Validate user can start task
        if (!canStartTask(task, userId)) {
            log.error("User {} cannot start task ID: {}", userId, instanceTaskId);
            throw new WorkflowException("User not authorized or dependencies not met to start this task", "CANNOT_START_TASK");
        }

        // Check current status
        if (!TaskStatus.EN_ATTENTE.equals(task.getTaskStatus()) && !TaskStatus.ASSIGNED.equals(task.getTaskStatus())) {
            log.error("Task ID: {} is in status {} and cannot be started", instanceTaskId, task.getTaskStatus());
            throw new WorkflowException("Task is in status " + task.getTaskStatus() + " and cannot be started", "INVALID_TASK_STATUS");
        }

        // Update task status to RECU
        String oldStatus = task.getTaskStatus().name();
        task.setTaskStatus(TaskStatus.RECU);
        task.setStartedAt(LocalDateTime.now());
        if (task.getAssignedToUserId() == null) {
            task.setAssignedToUserId(userId);
            task.setAssignedAt(LocalDateTime.now());
        }

        WorkflowInstanceTask savedTask = instanceTaskRepository.save(task);

        // Record history
        recordHistory(task, TaskActionType.STARTED, userId, oldStatus, TaskStatus.RECU.name(), "Task started");

        // Send notification if needed
        sendTaskNotification(task, userId, "TASK_STARTED", "Task started: " + task.getTaskName());

        log.info("Successfully started task ID: {} by user: {}", instanceTaskId, userId);
        return savedTask;
    }

    /**
     * Complete a task (mark as REALISE)
     *
     * @param instanceTaskId Instance task ID
     * @param userId User ID completing the task
     * @param inputData Task completion data
     * @return The completed task
     * @throws TaskNotFoundException if task not found
     * @throws WorkflowException if task cannot be completed
     */
    @Transactional
    public WorkflowInstanceTask completeTask(Long instanceTaskId, Long userId, Map<String, Object> inputData) {
        log.info("Completing task ID: {} by user: {}", instanceTaskId, userId);

        WorkflowInstanceTask task = instanceTaskRepository.findById(instanceTaskId)
                .orElseThrow(() -> new TaskNotFoundException(instanceTaskId));

        // Validate user assignment
        if (task.getAssignedToUserId() != null && !task.getAssignedToUserId().equals(userId)) {
            log.error("User {} is not assigned to task ID: {}", userId, instanceTaskId);
            throw new WorkflowException("User not assigned to this task", "UNAUTHORIZED_USER");
        }

        // Check current status
        if (!TaskStatus.RECU.equals(task.getTaskStatus())) {
            log.error("Task ID: {} is in status {} and cannot be completed", instanceTaskId, task.getTaskStatus());
            throw new WorkflowException("Task must be in RECU status to be completed", "INVALID_TASK_STATUS");
        }

        // If task requires approval, check approval status
        if (Boolean.TRUE.equals(task.getStateTask().getRequiresApproval())) {
            if (!approvalExecutionService.isApprovalComplete(task)) {
                log.error("Task ID: {} requires approval which is not complete", instanceTaskId);
                throw new WorkflowException("Task requires approval which is not yet complete", "APPROVAL_NOT_COMPLETE");
            }
        }

        // Update task status to REALISE
        String oldStatus = task.getTaskStatus().name();
        task.setTaskStatus(TaskStatus.REALISE);
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletionData(inputData);

        WorkflowInstanceTask savedTask = instanceTaskRepository.save(task);

        // Record history
        recordHistory(task, TaskActionType.COMPLETED, userId, oldStatus, TaskStatus.REALISE.name(),
                "Task completed" + (inputData != null ? " with " + inputData.size() + " data fields" : ""));

        // Send notification if needed
        sendTaskNotification(task, userId, "TASK_COMPLETED", "Task completed: " + task.getTaskName());

        log.info("Successfully completed task ID: {} by user: {}", instanceTaskId, userId);
        return savedTask;
    }

    /**
     * Upload document to a task
     *
     * @param instanceTaskId Instance task ID
     * @param userId User ID uploading the document
     * @param docInfo Document information
     * @return The updated task
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public WorkflowInstanceTask uploadDocument(Long instanceTaskId, Long userId, DocumentInfo docInfo) {
        log.info("Uploading document to task ID: {} by user: {}", instanceTaskId, userId);

        WorkflowInstanceTask task = instanceTaskRepository.findById(instanceTaskId)
                .orElseThrow(() -> new TaskNotFoundException(instanceTaskId));

        // Add document info to task data
        Map<String, Object> taskData = task.getTaskData();
        if (taskData == null) {
            taskData = new HashMap<>();
        }

        // Add to documents list
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> documents = (List<Map<String, Object>>) taskData.get("documents");
        if (documents == null) {
            documents = new java.util.ArrayList<>();
            taskData.put("documents", documents);
        }

        Map<String, Object> docMap = new HashMap<>();
        docMap.put("fileName", docInfo.fileName);
        docMap.put("fileType", docInfo.fileType);
        docMap.put("fileSize", docInfo.fileSize);
        docMap.put("filePath", docInfo.filePath);
        docMap.put("uploadedBy", userId);
        docMap.put("uploadedAt", LocalDateTime.now().toString());

        documents.add(docMap);
        task.setTaskData(taskData);

        WorkflowInstanceTask savedTask = instanceTaskRepository.save(task);

        // Record history
        recordHistory(task, TaskActionType.DOCUMENT_UPLOADED, userId, null, null,
                "Document uploaded: " + docInfo.fileName);

        log.info("Successfully uploaded document {} to task ID: {}", docInfo.fileName, instanceTaskId);
        return savedTask;
    }

    /**
     * Check if a task can be started by a user
     *
     * @param task Task to check
     * @param userId User ID
     * @return true if task can be started
     */
    public boolean canStartTask(WorkflowInstanceTask task, Long userId) {
        log.debug("Checking if user {} can start task ID: {}", userId, task.getId());

        // Check if user is assigned or has the required role
        if (task.getAssignedToUserId() != null && !task.getAssignedToUserId().equals(userId)) {
            log.debug("User {} is not assigned to task ID: {}", userId, task.getId());
            return false;
        }

        // Check dependencies
        Map<Long, Boolean> dependencies = getDependenciesStatus(task);
        boolean dependenciesMet = dependencies.values().stream().allMatch(met -> met);
        if (!dependenciesMet) {
            log.debug("Dependencies not met for task ID: {}", task.getId());
            return false;
        }

        // Check conditions
        boolean conditionsMet = evaluateConditions(task);
        if (!conditionsMet) {
            log.debug("Conditions not met for task ID: {}", task.getId());
            return false;
        }

        log.debug("User {} can start task ID: {}", userId, task.getId());
        return true;
    }

    /**
     * Get dependencies status for a task
     *
     * @param task Task to check
     * @return Map of dependency ID to satisfaction status
     */
    public Map<Long, Boolean> getDependenciesStatus(WorkflowInstanceTask task) {
        log.debug("Getting dependencies status for task ID: {}", task.getId());

        List<WorkflowTaskDependency> dependencies = taskDependencyRepository.findByDependentTaskId(task.getStateTask().getId());
        Map<Long, Boolean> statusMap = new HashMap<>();

        for (WorkflowTaskDependency dependency : dependencies) {
            // Find the required task instance in the same workflow instance
            List<WorkflowInstanceTask> requiredTasks = instanceTaskRepository
                    .findByInstanceIdAndStateTaskId(task.getInstance().getId(), dependency.getRequiredTask().getId());

            boolean satisfied = false;
            if (!requiredTasks.isEmpty()) {
                WorkflowInstanceTask requiredTask = requiredTasks.get(0);
                String requiredStatus = dependency.getRequiredStatus();

                if (requiredStatus != null) {
                    satisfied = requiredTask.getTaskStatus().name().equals(requiredStatus);
                } else {
                    // Default: require completion
                    satisfied = requiredTask.getTaskStatus().isCompleted();
                }
            }

            statusMap.put(dependency.getId(), satisfied);
        }

        log.debug("Found {} dependencies for task ID: {}, {} satisfied",
                dependencies.size(), task.getId(), statusMap.values().stream().filter(v -> v).count());
        return statusMap;
    }

    /**
     * Evaluate all conditions for a task
     *
     * @param task Task to check
     * @return true if all conditions are met
     */
    public boolean evaluateConditions(WorkflowInstanceTask task) {
        log.debug("Evaluating conditions for task ID: {}", task.getId());

        List<WorkflowTaskCondition> conditions = taskConditionRepository.findByTaskIdAndIsActiveTrue(task.getStateTask().getId());

        if (conditions.isEmpty()) {
            log.debug("No conditions defined for task ID: {}", task.getId());
            return true;
        }

        // Simple evaluation - in a real implementation, this would use a condition engine
        for (WorkflowTaskCondition condition : conditions) {
            boolean conditionMet = evaluateCondition(condition, task);
            if (!conditionMet) {
                log.debug("Condition {} not met for task ID: {}", condition.getId(), task.getId());
                return false;
            }
        }

        log.debug("All conditions met for task ID: {}", task.getId());
        return true;
    }

    /**
     * Get tasks assigned to a user with specific status
     *
     * @param userId User ID
     * @param status Task status
     * @return List of tasks
     */
    public List<WorkflowInstanceTask> getTasksForUser(Long userId, TaskStatus status) {
        log.debug("Getting tasks for user {} with status: {}", userId, status);

        List<WorkflowInstanceTask> tasks;
        if (status != null) {
            tasks = instanceTaskRepository.findByAssignedToUserIdAndTaskStatusIn(
                    userId,
                    List.of(status),
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
        } else {
            tasks = instanceTaskRepository.findActiveTasksByUserId(userId);
        }

        log.debug("Found {} tasks for user {} with status: {}", tasks.size(), userId, status);
        return tasks;
    }

    /**
     * Assign a task to a user
     *
     * @param instanceTaskId Instance task ID
     * @param userId User ID to assign to
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public void assignTask(Long instanceTaskId, Long userId) {
        log.info("Assigning task ID: {} to user: {}", instanceTaskId, userId);

        WorkflowInstanceTask task = instanceTaskRepository.findById(instanceTaskId)
                .orElseThrow(() -> new TaskNotFoundException(instanceTaskId));

        Long oldAssignee = task.getAssignedToUserId();
        task.assignTo(userId, "system");

        instanceTaskRepository.save(task);

        // Record history
        recordHistory(task, TaskActionType.ASSIGNED, userId, null, null,
                "Task assigned to user: " + userId + (oldAssignee != null ? " (previously: " + oldAssignee + ")" : ""));

        // Send notification
        sendTaskNotification(task, userId, "TASK_ASSIGNED", "Task assigned to you: " + task.getTaskName());

        log.info("Successfully assigned task ID: {} to user: {}", instanceTaskId, userId);
    }

    /**
     * Simple condition evaluator
     */
    private boolean evaluateCondition(WorkflowTaskCondition condition, WorkflowInstanceTask task) {
        // This is a simplified implementation
        // In a real system, you would use a proper expression evaluator
        // For now, we'll just return true
        log.debug("Evaluating condition type: {} for task ID: {}", condition.getConditionType(), task.getId());
        return true;
    }

    /**
     * Record task history entry
     */
    private void recordHistory(WorkflowInstanceTask task, TaskActionType actionType, Long userId,
                                String oldStatus, String newStatus, String comments) {
        WorkflowTaskHistory history = WorkflowTaskHistory.builder()
                .instanceTask(task)
                .actionType(actionType)
                .actionByUserId(userId != null ? userId : 0L)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .comments(comments)
                .build();

        taskHistoryRepository.save(history);
    }

    /**
     * Send task notification to user
     */
    private void sendTaskNotification(WorkflowInstanceTask task, Long userId, String notificationType, String message) {
        WorkflowNotification notification = WorkflowNotification.builder()
                .instanceTask(task)
                .recipientUserId(userId)
                .notificationType(notificationType)
                .notificationChannel(NotificationChannel.IN_APP)
                .subject(task.getTaskName())
                .message(message)
                .build();

        notificationRepository.save(notification);
        log.debug("Sent notification to user: {} for task: {}", userId, task.getId());
    }

    /**
     * Get all tasks for a user
     *
     * @param userId User ID
     * @return List of tasks
     */
    public List<WorkflowInstanceTask> getUserTasks(Long userId) {
        log.debug("Getting all tasks for user: {}", userId);
        return instanceTaskRepository.findActiveTasksByUserId(userId);
    }

    /**
     * Get user tasks by status
     *
     * @param userId User ID
     * @param status Task status
     * @return List of tasks
     */
    public List<WorkflowInstanceTask> getUserTasksByStatus(Long userId, TaskStatus status) {
        log.debug("Getting tasks for user {} with status: {}", userId, status);
        return instanceTaskRepository.findByAssignedToUserIdAndTaskStatusIn(
                userId,
                List.of(status),
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
    }

    /**
     * Get task dependencies
     *
     * @param taskId Task ID
     * @return List of dependent tasks
     */
    public List<WorkflowInstanceTask> getTaskDependencies(Long taskId) {
        log.debug("Getting dependencies for task: {}", taskId);

        WorkflowInstanceTask task = instanceTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        List<WorkflowTaskDependency> dependencies = taskDependencyRepository
                .findByDependentTaskId(task.getStateTask().getId());

        return dependencies.stream()
                .flatMap(dep -> instanceTaskRepository
                        .findByInstanceIdAndStateTaskId(task.getInstance().getId(), dep.getRequiredTask().getId())
                        .stream())
                .collect(Collectors.toList());
    }

    /**
     * Check if a user can start a specific task
     *
     * @param taskId Task ID
     * @param userId User ID
     * @return true if user can start the task
     */
    public boolean canUserStartTask(Long taskId, Long userId) {
        log.debug("Checking if user {} can start task: {}", userId, taskId);
        WorkflowInstanceTask task = instanceTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        return canStartTask(task, userId);
    }

    /**
     * Upload document using controller's DocumentInfo
     *
     * @param instanceTaskId Instance task ID
     * @param userId User ID uploading the document
     * @param docInfo Document information from controller
     * @return The updated task
     */
    @Transactional
    public WorkflowInstanceTask uploadDocument(Long instanceTaskId, Long userId,
            com.eflo.workflow.web.dto.request.DocumentInfo docInfo) {
        DocumentInfo serviceDocInfo = new DocumentInfo(
                docInfo.getDocumentName(),
                docInfo.getDocumentType(),
                docInfo.getDocumentSize(),
                docInfo.getDocumentPath()
        );
        return uploadDocument(instanceTaskId, userId, serviceDocInfo);
    }

    /**
     * Document information holder
     */
    public static class DocumentInfo {
        public String fileName;
        public String fileType;
        public Long fileSize;
        public String filePath;

        public DocumentInfo(String fileName, String fileType, Long fileSize, String filePath) {
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.filePath = filePath;
        }
    }
}
