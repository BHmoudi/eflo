package com.eflo.workflow.web;

import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.model.request.CreateInstanceRequest;
import com.eflo.workflow.domain.model.request.TransitionRequest;
import com.eflo.workflow.domain.model.response.InstanceResponse;
import com.eflo.workflow.service.InstanceManagementService;
import com.eflo.workflow.service.TransitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Instance Controller
 *
 * REST API for managing workflow instances.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceManagementService instanceManagementService;
    private final TransitionService transitionService;

    /**
     * Create a new workflow instance
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InstanceResponse> createInstance(
            @Valid @RequestBody CreateInstanceRequest request,
            Authentication authentication) {
        log.info("REST request to create workflow instance for order: {}", request.getOrderId());
        String userName = authentication.getName();
        InstanceResponse response = instanceManagementService.createInstance(request, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get instance by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InstanceResponse> getInstance(@PathVariable Long id) {
        log.info("REST request to get workflow instance: {}", id);
        InstanceResponse response = instanceManagementService.getInstance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get instance by order ID
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<InstanceResponse>> getInstanceByOrderId(@PathVariable Long orderId) {
        log.info("REST request to get workflow instances for order: {}", orderId);
        List<InstanceResponse> responses = instanceManagementService.getInstanceByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get instances by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<InstanceResponse>> getInstancesByStatus(@PathVariable InstanceStatus status) {
        log.info("REST request to get instances with status: {}", status);
        List<InstanceResponse> responses = instanceManagementService.getInstancesByStatus(status);
        return ResponseEntity.ok(responses);
    }

    /**
     * Start an instance
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InstanceResponse> startInstance(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("REST request to start workflow instance: {}", id);
        String userName = authentication.getName();
        InstanceResponse response = instanceManagementService.startInstance(id, userName);
        return ResponseEntity.ok(response);
    }

    /**
     * Execute a transition
     */
    @PostMapping("/{id}/transition")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> executeTransition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionRequest request,
            Authentication authentication) {
        log.info("REST request to execute transition for instance: {}", id);
        String userName = authentication.getName();
        transitionService.executeTransition(id, request, null, userName);
        return ResponseEntity.ok().build();
    }

    /**
     * Pause an instance
     */
    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> pauseInstance(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("REST request to pause workflow instance: {}", id);
        String userName = authentication.getName();
        instanceManagementService.pauseInstance(id, userName);
        return ResponseEntity.ok().build();
    }

    /**
     * Resume an instance
     */
    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> resumeInstance(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("REST request to resume workflow instance: {}", id);
        String userName = authentication.getName();
        instanceManagementService.resumeInstance(id, userName);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel an instance
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> cancelInstance(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("REST request to cancel workflow instance: {}", id);
        String userName = authentication.getName();
        instanceManagementService.cancelInstance(id, reason, userName);
        return ResponseEntity.ok().build();
    }
}
