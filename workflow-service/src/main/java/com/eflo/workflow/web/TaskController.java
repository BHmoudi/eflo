package com.eflo.workflow.web;

import com.eflo.workflow.domain.model.request.CompleteTaskRequest;
import com.eflo.workflow.domain.model.request.ReassignTaskRequest;
import com.eflo.workflow.domain.model.response.TaskResponse;
import com.eflo.workflow.service.TaskManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task Controller
 *
 * REST API for managing workflow tasks.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskManagementService taskManagementService;

    /**
     * Get tasks by filters
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) Long workflowInstanceId,
            @RequestParam(required = false) Long instanceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedUserId) {
        log.info("REST request to get tasks - instanceId: {}, status: {}, userId: {}",
                workflowInstanceId != null ? workflowInstanceId : instanceId, status, assignedUserId);

        Long id = workflowInstanceId != null ? workflowInstanceId : instanceId;

        if (id != null) {
            List<TaskResponse> responses = taskManagementService.getInstanceTasks(id, status);
            return ResponseEntity.ok(responses);
        }

        if (assignedUserId != null) {
            List<TaskResponse> responses = taskManagementService.getUserActiveTasks(assignedUserId);
            return ResponseEntity.ok(responses);
        }

        // Return all tasks (or could throw exception)
        return ResponseEntity.ok(List.of());
    }

    /**
     * Get my active tasks
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TaskResponse>> getMyTasks(Authentication authentication) {
        log.info("REST request to get tasks for user: {}", authentication.getName());
        // In real implementation, extract user ID from authentication
        Long userId = 1L; // Placeholder
        List<TaskResponse> responses = taskManagementService.getUserActiveTasks(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get task by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        log.info("REST request to get task: {}", id);
        TaskResponse response = taskManagementService.getTask(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Start a task
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TaskResponse> startTask(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("REST request to start task: {}", id);
        String userName = authentication.getName();
        Long userId = 1L; // Placeholder
        TaskResponse response = taskManagementService.startTask(id, userId, userName);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete a task
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long id,
            @Valid @RequestBody CompleteTaskRequest request,
            Authentication authentication) {
        log.info("REST request to complete task: {}", id);
        String userName = authentication.getName();
        Long userId = 1L; // Placeholder
        TaskResponse response = taskManagementService.completeTask(id, request, userId, userName);
        return ResponseEntity.ok(response);
    }

    /**
     * Reassign a task
     */
    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TaskResponse> reassignTask(
            @PathVariable Long id,
            @Valid @RequestBody ReassignTaskRequest request,
            Authentication authentication) {
        log.info("REST request to reassign task: {}", id);
        String userName = authentication.getName();
        Long userId = 1L; // Placeholder
        TaskResponse response = taskManagementService.reassignTask(id, request, userId, userName);
        return ResponseEntity.ok(response);
    }

    /**
     * Skip a task
     */
    @PostMapping("/{id}/skip")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TaskResponse> skipTask(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("REST request to skip task: {}", id);
        String userName = authentication.getName();
        Long userId = 1L; // Placeholder
        TaskResponse response = taskManagementService.skipTask(id, userId, userName);
        return ResponseEntity.ok(response);
    }
}
