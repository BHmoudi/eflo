package com.eflo.workflow.web;

import com.eflo.workflow.domain.model.request.CreateProcessRequest;
import com.eflo.workflow.domain.model.request.UpdateProcessRequest;
import com.eflo.workflow.domain.model.response.ProcessResponse;
import com.eflo.workflow.service.ProcessDefinitionService;
import com.eflo.workflow.service.StateManagementService;
import com.eflo.workflow.service.TransitionManagementService;
import com.eflo.workflow.web.dto.request.CreateStateRequest;
import com.eflo.workflow.web.dto.request.CreateTaskRequest;
import com.eflo.workflow.web.dto.request.CreateTransitionRequest;
import com.eflo.workflow.web.dto.response.StateResponse;
import com.eflo.workflow.web.dto.response.TaskDefinitionResponse;
import com.eflo.workflow.web.dto.response.TransitionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Process Controller
 *
 * Comprehensive REST API for managing workflow process definitions, states, tasks, and transitions.
 *
 * @author Workflow Service
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessDefinitionService processDefinitionService;
    private final StateManagementService stateManagementService;
    private final TransitionManagementService transitionManagementService;

    // ============================================
    // PROCESS MANAGEMENT
    // ============================================

    /**
     * Create a new workflow process
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessResponse> createProcess(@Valid @RequestBody CreateProcessRequest request) {
        log.info("REST request to create workflow process: {}", request.getProcessCode());
        ProcessResponse response = processDefinitionService.createProcess(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get process by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProcessResponse> getProcess(@PathVariable Long id) {
        log.info("REST request to get workflow process: {}", id);
        ProcessResponse response = processDefinitionService.getProcessById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get process by code
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProcessResponse> getProcessByCode(@PathVariable String code) {
        log.info("REST request to get workflow process by code: {}", code);
        ProcessResponse response = processDefinitionService.getProcessByCode(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active processes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ProcessResponse>> getAllActiveProcesses() {
        log.info("REST request to get all active workflow processes");
        List<ProcessResponse> responses = processDefinitionService.getAllActiveProcesses();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get processes by order type
     */
    @GetMapping("/order-type/{orderType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ProcessResponse>> getProcessesByOrderType(@PathVariable String orderType) {
        log.info("REST request to get processes for order type: {}", orderType);
        List<ProcessResponse> responses = processDefinitionService.getProcessesByOrderType(orderType);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a workflow process
     */
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessResponse> updateProcess(
            @PathVariable String code,
            @Valid @RequestBody UpdateProcessRequest request) {
        log.info("REST request to update workflow process: {}", code);
        ProcessResponse response = processDefinitionService.updateProcess(code, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update a workflow process
     */
    @PatchMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessResponse> partiallyUpdateProcess(
            @PathVariable String code,
            @Valid @RequestBody UpdateProcessRequest request) {
        log.info("REST request to partially update workflow process: {}", code);
        ProcessResponse response = processDefinitionService.updateProcess(code, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a process
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateProcess(@PathVariable Long id) {
        log.info("REST request to activate workflow process: {}", id);
        processDefinitionService.activateProcess(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Deactivate a process
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateProcess(@PathVariable Long id) {
        log.info("REST request to deactivate workflow process: {}", id);
        processDefinitionService.deactivateProcess(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a process by ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        log.info("REST request to delete workflow process: {}", id);
        processDefinitionService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a process by code
     */
    @DeleteMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProcessByCode(@PathVariable String code) {
        log.info("REST request to delete workflow process by code: {}", code);
        processDefinitionService.deleteProcessByCode(code);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // STATE MANAGEMENT
    // ============================================

    /**
     * Add a state to a process
     */
    @PostMapping("/{processCode}/states")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StateResponse> addState(
            @PathVariable String processCode,
            @Valid @RequestBody CreateStateRequest request) {
        log.info("REST request to add state {} to process {}", request.getStateCode(), processCode);
        StateResponse response = stateManagementService.addState(processCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all states for a process
     */
    @GetMapping("/{processCode}/states")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<StateResponse>> getProcessStates(@PathVariable String processCode) {
        log.info("REST request to get states for process: {}", processCode);
        List<StateResponse> responses = stateManagementService.getProcessStates(processCode);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific state
     */
    @GetMapping("/{processCode}/states/{stateCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<StateResponse> getState(
            @PathVariable String processCode,
            @PathVariable String stateCode) {
        log.info("REST request to get state {} for process {}", stateCode, processCode);
        StateResponse response = stateManagementService.getState(processCode, stateCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a state
     */
    @PutMapping("/{processCode}/states/{stateCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StateResponse> updateState(
            @PathVariable String processCode,
            @PathVariable String stateCode,
            @Valid @RequestBody CreateStateRequest request) {
        log.info("REST request to update state {} for process {}", stateCode, processCode);
        StateResponse response = stateManagementService.updateState(processCode, stateCode, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a state
     */
    @DeleteMapping("/{processCode}/states/{stateCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteState(
            @PathVariable String processCode,
            @PathVariable String stateCode) {
        log.info("REST request to delete state {} from process {}", stateCode, processCode);
        stateManagementService.deleteState(processCode, stateCode);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // TASK MANAGEMENT
    // ============================================

    /**
     * Add a task to a state
     */
    @PostMapping("/{processCode}/states/{stateCode}/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDefinitionResponse> addTask(
            @PathVariable String processCode,
            @PathVariable String stateCode,
            @Valid @RequestBody CreateTaskRequest request) {
        log.info("REST request to add task {} to state {} in process {}",
                request.getTaskCode(), stateCode, processCode);
        TaskDefinitionResponse response = stateManagementService.addTask(processCode, stateCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tasks for a state
     */
    @GetMapping("/{processCode}/states/{stateCode}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TaskDefinitionResponse>> getStateTasks(
            @PathVariable String processCode,
            @PathVariable String stateCode) {
        log.info("REST request to get tasks for state {} in process {}", stateCode, processCode);
        List<TaskDefinitionResponse> responses = stateManagementService.getStateTasks(processCode, stateCode);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a task
     */
    @PutMapping("/{processCode}/states/{stateCode}/tasks/{taskCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDefinitionResponse> updateTask(
            @PathVariable String processCode,
            @PathVariable String stateCode,
            @PathVariable String taskCode,
            @Valid @RequestBody CreateTaskRequest request) {
        log.info("REST request to update task {} in state {} for process {}", taskCode, stateCode, processCode);
        TaskDefinitionResponse response = stateManagementService.updateTask(processCode, stateCode, taskCode, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task
     */
    @DeleteMapping("/{processCode}/states/{stateCode}/tasks/{taskCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String processCode,
            @PathVariable String stateCode,
            @PathVariable String taskCode) {
        log.info("REST request to delete task {} from state {} in process {}", taskCode, stateCode, processCode);
        stateManagementService.deleteTask(processCode, stateCode, taskCode);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // TRANSITION MANAGEMENT
    // ============================================

    /**
     * Add a transition to a process
     */
    @PostMapping("/{processCode}/transitions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransitionResponse> addTransition(
            @PathVariable String processCode,
            @Valid @RequestBody CreateTransitionRequest request) {
        log.info("REST request to add transition from {} to {} in process {}",
                request.getFromStateCode(), request.getToStateCode(), processCode);
        TransitionResponse response = transitionManagementService.addTransition(processCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all transitions for a process
     */
    @GetMapping("/{processCode}/transitions")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TransitionResponse>> getProcessTransitions(@PathVariable String processCode) {
        log.info("REST request to get transitions for process: {}", processCode);
        List<TransitionResponse> responses = transitionManagementService.getProcessTransitions(processCode);
        return ResponseEntity.ok(responses);
    }

    /**
     * Delete a transition
     */
    @DeleteMapping("/{processCode}/transitions/{transitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTransition(
            @PathVariable String processCode,
            @PathVariable Long transitionId) {
        log.info("REST request to delete transition {} from process {}", transitionId, processCode);
        transitionManagementService.deleteTransition(processCode, transitionId);
        return ResponseEntity.noContent().build();
    }
}
