package com.eflo.workflow.web;

import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.TaskStatus;
import com.eflo.workflow.service.ApprovalExecutionService;
import com.eflo.workflow.service.TaskExecutionService;
import com.eflo.workflow.web.dto.request.DocumentInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Task Execution Controller
 *
 * REST API for task execution and management.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@CrossOrigin
public class TaskExecutionController {

    private final TaskExecutionService taskExecutionService;
    private final ApprovalExecutionService approvalExecutionService;

    /**
     * Start a task
     *
     * @param id Task execution ID
     * @param userId User ID from header
     * @return Updated task execution
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowInstanceTask> startTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.debug("REST request to start task {} by user {}", id, userId);
        WorkflowInstanceTask taskExecution = taskExecutionService.startTask(id, userId);
        return ResponseEntity.ok(taskExecution);
    }

    /**
     * Complete a task
     *
     * @param id Task execution ID
     * @param userId User ID from header
     * @param inputData Task input data
     * @return Updated task execution
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowInstanceTask> completeTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> inputData) {
        log.debug("REST request to complete task {} by user {}", id, userId);
        WorkflowInstanceTask taskExecution = taskExecutionService.completeTask(id, userId, inputData);
        return ResponseEntity.ok(taskExecution);
    }

    /**
     * Upload a document to a task
     *
     * @param id Task execution ID
     * @param userId User ID from header
     * @param docInfo Document information
     * @return Updated task execution
     */
    @PostMapping("/{id}/upload-document")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowInstanceTask> uploadDocument(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DocumentInfo docInfo) {
        log.debug("REST request to upload document to task {} by user {}", id, userId);
        WorkflowInstanceTask taskExecution = taskExecutionService.uploadDocument(id, userId, docInfo);
        return ResponseEntity.ok(taskExecution);
    }

    /**
     * Get my tasks
     *
     * @param userId User ID from header
     * @param status Optional task status filter
     * @return List of task executions
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowInstanceTask>> getMyTasks(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) TaskStatus status) {
        log.debug("REST request to get tasks for user {} with status {}", userId, status);
        List<WorkflowInstanceTask> tasks;
        if (status != null) {
            tasks = taskExecutionService.getUserTasksByStatus(userId, status);
        } else {
            tasks = taskExecutionService.getUserTasks(userId);
        }
        return ResponseEntity.ok(tasks);
    }

    /**
     * Check if user can start a task
     *
     * @param id Task execution ID
     * @param userId User ID from header
     * @return True if user can start the task, false otherwise
     */
    @GetMapping("/{id}/can-start")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Boolean> canStartTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.debug("REST request to check if user {} can start task {}", userId, id);
        boolean canStart = taskExecutionService.canUserStartTask(id, userId);
        return ResponseEntity.ok(canStart);
    }

    /**
     * Get task dependencies
     *
     * @param id Task execution ID
     * @return List of dependent task executions
     */
    @GetMapping("/{id}/dependencies")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowInstanceTask>> getDependencies(@PathVariable Long id) {
        log.debug("REST request to get dependencies for task {}", id);
        List<WorkflowInstanceTask> dependencies = taskExecutionService.getTaskDependencies(id);
        return ResponseEntity.ok(dependencies);
    }
}
