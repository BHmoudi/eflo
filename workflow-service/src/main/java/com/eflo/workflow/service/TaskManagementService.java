package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.*;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.enums.TaskStatus;
import com.eflo.workflow.domain.model.event.TaskEvent;
import com.eflo.workflow.domain.model.request.CompleteTaskRequest;
import com.eflo.workflow.domain.model.request.ReassignTaskRequest;
import com.eflo.workflow.domain.model.response.TaskResponse;
import com.eflo.workflow.domain.repository.*;
import com.eflo.workflow.exception.TaskNotFoundException;
import com.eflo.workflow.exception.TaskValidationException;
import com.eflo.workflow.mapper.WorkflowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Task Management Service
 *
 * Manages workflow task lifecycle: creation, assignment, execution, and completion.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskManagementService {

    private final WorkflowInstanceTaskRepository taskRepository;
    private final WorkflowStateTaskRepository stateTaskRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final WorkflowMapper mapper;
    private final WorkflowEventPublisher eventPublisher;

    /**
     * Create tasks for a state
     */
    @Transactional
    public void createTasksForState(WorkflowInstance instance, WorkflowState state, String createdBy) {
        log.info("Creating tasks for instance: {} state: {}", instance.getId(), state.getStateName());

        List<WorkflowStateTask> stateTasks = stateTaskRepository.findByStateIdOrderByTaskOrderAsc(state.getId());

        for (WorkflowStateTask stateTask : stateTasks) {
            WorkflowInstanceTask task = WorkflowInstanceTask.builder()
                    .instance(instance)
                    .stateTask(stateTask)
                    .taskCode(stateTask.getTaskCode())
                    .taskName(stateTask.getTaskName())
                    .taskStatus(TaskStatus.PENDING)
                    .assignedToRole(stateTask.getAssignedToRole())
                    .taskData(stateTask.getFormDefinition())
                    .build();

            // Calculate expected completion time
            if (stateTask.getExpectedDurationHours() != null) {
                task.setExpectedCompletionAt(
                        LocalDateTime.now().plusHours(stateTask.getExpectedDurationHours())
                );
            }

            // Auto-assign if configured
            if (stateTask.getAutoAssign() && stateTask.getAssignedToUserId() != null) {
                task.assignTo(stateTask.getAssignedToUserId(), createdBy);
            }

            taskRepository.save(task);

            // Create history
            WorkflowHistory history = WorkflowHistory.forTaskEvent(
                    instance,
                    task,
                    EventType.TASK_CREATED,
                    "Task created: " + task.getTaskName(),
                    null,
                    createdBy
            );
            historyRepository.save(history);

            // Publish event
            publishTaskEvent(task, EventType.TASK_CREATED, null, createdBy);
        }

        log.info("Created {} tasks for instance: {}", stateTasks.size(), instance.getId());
    }

    /**
     * Get user's active tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getUserActiveTasks(Long userId) {
        List<WorkflowInstanceTask> tasks = taskRepository.findActiveTasksByUserId(userId);
        return mapper.toTaskResponseList(tasks);
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        WorkflowInstanceTask task = getTaskEntity(taskId);
        return mapper.toTaskResponse(task);
    }

    /**
     * Start a task
     */
    @Transactional
    public TaskResponse startTask(Long taskId, Long userId, String userName) {
        log.info("Starting task: {} by user: {}", taskId, userId);

        WorkflowInstanceTask task = getTaskEntity(taskId);

        if (task.getTaskStatus() != TaskStatus.ASSIGNED) {
            throw new TaskValidationException("Task must be in ASSIGNED status to start");
        }

        if (!task.getAssignedToUserId().equals(userId)) {
            throw new TaskValidationException("Task can only be started by assigned user");
        }

        task.start();
        taskRepository.save(task);

        // Create history
        WorkflowHistory history = WorkflowHistory.forTaskEvent(
                task.getInstance(),
                task,
                EventType.TASK_STARTED,
                "Task started",
                userId,
                userName
        );
        historyRepository.save(history);

        // Publish event
        publishTaskEvent(task, EventType.TASK_STARTED, userId, userName);

        return mapper.toTaskResponse(task);
    }

    /**
     * Complete a task
     */
    @Transactional
    public TaskResponse completeTask(Long taskId, CompleteTaskRequest request,
                                    Long userId, String userName) {
        log.info("Completing task: {} by user: {}", taskId, userId);

        WorkflowInstanceTask task = getTaskEntity(taskId);

        if (!task.getTaskStatus().canWork()) {
            throw new TaskValidationException("Task cannot be completed in current status");
        }

        if (!task.getAssignedToUserId().equals(userId)) {
            throw new TaskValidationException("Task can only be completed by assigned user");
        }

        task.complete(request.getCompletionData());
        if (request.getComments() != null) {
            task.setComments(request.getComments());
        }
        taskRepository.save(task);

        // Create history
        WorkflowHistory history = WorkflowHistory.forTaskEvent(
                task.getInstance(),
                task,
                EventType.TASK_COMPLETED,
                "Task completed",
                userId,
                userName
        );
        historyRepository.save(history);

        // Publish event
        publishTaskEvent(task, EventType.TASK_COMPLETED, userId, userName);

        log.info("Task completed: {}", taskId);
        return mapper.toTaskResponse(task);
    }

    /**
     * Reassign a task
     */
    @Transactional
    public TaskResponse reassignTask(Long taskId, ReassignTaskRequest request,
                                    Long userId, String userName) {
        log.info("Reassigning task: {} to user: {}", taskId, request.getNewAssigneeUserId());

        WorkflowInstanceTask task = getTaskEntity(taskId);

        if (task.getTaskStatus().isTerminal()) {
            throw new TaskValidationException("Cannot reassign completed task");
        }

        Long previousAssignee = task.getAssignedToUserId();
        task.assignTo(request.getNewAssigneeUserId(), userName);
        taskRepository.save(task);

        // Create history
        WorkflowHistory history = WorkflowHistory.forTaskEvent(
                task.getInstance(),
                task,
                EventType.TASK_REASSIGNED,
                String.format("Task reassigned from user %d to user %d. Reason: %s",
                            previousAssignee, request.getNewAssigneeUserId(), request.getReason()),
                userId,
                userName
        );
        historyRepository.save(history);

        // Publish event
        publishTaskEvent(task, EventType.TASK_REASSIGNED, userId, userName);

        return mapper.toTaskResponse(task);
    }

    /**
     * Skip a task
     */
    @Transactional
    public TaskResponse skipTask(Long taskId, Long userId, String userName) {
        log.info("Skipping task: {}", taskId);

        WorkflowInstanceTask task = getTaskEntity(taskId);

        // Check if task can be skipped
        if (!task.getStateTask().getIsMandatory()) {
            task.skip();
            taskRepository.save(task);

            // Create history
            WorkflowHistory history = WorkflowHistory.forTaskEvent(
                    task.getInstance(),
                    task,
                    EventType.TASK_SKIPPED,
                    "Task skipped",
                    userId,
                    userName
            );
            historyRepository.save(history);

            // Publish event
            publishTaskEvent(task, EventType.TASK_SKIPPED, userId, userName);
        } else {
            throw new TaskValidationException("Mandatory tasks cannot be skipped");
        }

        return mapper.toTaskResponse(task);
    }

    /**
     * Get tasks for instance (all or filtered by status)
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getInstanceTasks(Long instanceId, String statusFilter) {
        log.info("Getting tasks for instance: {} with status filter: {}", instanceId, statusFilter);

        List<WorkflowInstanceTask> tasks;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            try {
                TaskStatus status = TaskStatus.valueOf(statusFilter.toUpperCase());
                tasks = taskRepository.findByInstanceIdAndTaskStatus(instanceId, status);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}, returning all tasks", statusFilter);
                tasks = taskRepository.findByInstanceIdOrderByIdAsc(instanceId);
            }
        } else {
            tasks = taskRepository.findByInstanceIdOrderByIdAsc(instanceId);
        }

        return mapper.toTaskResponseList(tasks);
    }

    /**
     * Get pending tasks for instance
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getInstancePendingTasks(Long instanceId) {
        List<WorkflowInstanceTask> tasks = taskRepository.findPendingTasksByInstanceId(instanceId);
        return mapper.toTaskResponseList(tasks);
    }

    /**
     * Get task entity (for internal use)
     */
    @Transactional(readOnly = true)
    public WorkflowInstanceTask getTaskEntity(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    /**
     * Update task with AI document analysis result (n8n callback)
     */
    @Transactional
    public void updateTaskWithAnalysisResult(String taskId, Map<String, Object> analysisResult) {
        log.info("Updating task {} with AI analysis result", taskId);

        try {
            Long id = Long.parseLong(taskId);
            WorkflowInstanceTask task = getTaskEntity(id);

            // Store analysis result in task data
            task.setCompletionData(analysisResult);

            // If task is pending/assigned, auto-complete it with analysis result
            if (task.getTaskStatus() == TaskStatus.PENDING ||
                task.getTaskStatus() == TaskStatus.ASSIGNED ||
                task.getTaskStatus() == TaskStatus.IN_PROGRESS) {
                task.complete(analysisResult);

                // Create history
                WorkflowHistory history = WorkflowHistory.forTaskEvent(
                        task.getInstance(),
                        task,
                        EventType.TASK_COMPLETED,
                        "Task completed automatically by AI analysis",
                        null,
                        "n8n-ai-agent"
                );
                historyRepository.save(history);

                // Publish event
                publishTaskEvent(task, EventType.TASK_COMPLETED, null, "n8n-ai-agent");
            }

            taskRepository.save(task);
            log.info("Successfully updated task {} with analysis result", taskId);

        } catch (NumberFormatException e) {
            log.error("Invalid task ID format: {}", taskId);
        } catch (Exception e) {
            log.error("Error updating task with analysis result", e);
        }
    }

    /**
     * Complete task validation (n8n callback - all validations passed)
     */
    @Transactional
    public void completeTaskValidation(String taskId, Map<String, Object> validationResult) {
        log.info("Completing task validation for task: {}", taskId);

        try {
            Long id = Long.parseLong(taskId);
            WorkflowInstanceTask task = getTaskEntity(id);

            // Store validation results
            task.setCompletionData(validationResult);

            // Mark task as validated and completed
            if (task.getTaskStatus().canWork()) {
                task.complete(validationResult);

                // Create history
                WorkflowHistory history = WorkflowHistory.forTaskEvent(
                        task.getInstance(),
                        task,
                        EventType.TASK_COMPLETED,
                        "Task validated and completed: All validations passed",
                        null,
                        "n8n-validator"
                );
                historyRepository.save(history);

                // Publish event
                publishTaskEvent(task, EventType.TASK_COMPLETED, null, "n8n-validator");
            }

            taskRepository.save(task);
            log.info("Task validation completed successfully for task: {}", taskId);

        } catch (NumberFormatException e) {
            log.error("Invalid task ID format: {}", taskId);
        } catch (Exception e) {
            log.error("Error completing task validation", e);
        }
    }

    /**
     * Reject task with validation errors (n8n callback - validation failed)
     */
    @Transactional
    public void rejectTaskWithValidationErrors(String taskId, Map<String, Object> validationResult) {
        log.info("Rejecting task due to validation errors: {}", taskId);

        try {
            Long id = Long.parseLong(taskId);
            WorkflowInstanceTask task = getTaskEntity(id);

            // Store validation errors
            task.setCompletionData(validationResult);

            // Add comments about validation errors
            Object errors = validationResult.get("errors");
            if (errors != null) {
                task.setComments("Validation errors: " + errors.toString());
            }

            // Set task status back to ASSIGNED so user can correct errors
            if (task.getTaskStatus() == TaskStatus.IN_PROGRESS) {
                task.setTaskStatus(TaskStatus.ASSIGNED);
            }

            // Create history
            WorkflowHistory history = WorkflowHistory.forTaskEvent(
                    task.getInstance(),
                    task,
                    EventType.TASK_FAILED,
                    "Task validation failed: " + validationResult.get("errorCount") + " error(s) found",
                    null,
                    "n8n-validator"
            );
            historyRepository.save(history);

            taskRepository.save(task);
            log.info("Task validation failed for task: {}", taskId);

        } catch (NumberFormatException e) {
            log.error("Invalid task ID format: {}", taskId);
        } catch (Exception e) {
            log.error("Error rejecting task with validation errors", e);
        }
    }

    /**
     * Publish task event
     */
    private void publishTaskEvent(WorkflowInstanceTask task, EventType eventType,
                                  Long userId, String userName) {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .taskId(task.getId())
                .instanceId(task.getInstance().getId())
                .orderId(task.getInstance().getOrderId())
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .taskStatus(task.getTaskStatus())
                .assignedToUserId(task.getAssignedToUserId())
                .timestamp(LocalDateTime.now())
                .actionByUserId(userId)
                .actionByUserName(userName)
                .description(eventType.getDescription())
                .build();

        eventPublisher.publishTaskEvent(event);
    }
}
