package com.eflo.workflow.web;

import com.eflo.workflow.domain.WorkflowApprovalChain;
import com.eflo.workflow.domain.WorkflowApprovalLevel;
import com.eflo.workflow.service.ApprovalChainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Approval Chain Controller
 *
 * REST API for managing workflow approval chains.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/approval-chains")
@RequiredArgsConstructor
@CrossOrigin
public class ApprovalChainController {

    private final ApprovalChainService approvalChainService;

    /**
     * Get all active approval chains
     *
     * @return List of all active approval chains
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowApprovalChain>> getAllChains() {
        log.debug("REST request to get all active approval chains");
        List<WorkflowApprovalChain> chains = approvalChainService.getAllActiveChains();
        return ResponseEntity.ok(chains);
    }

    /**
     * Get approval chain by ID
     *
     * @param id Chain ID
     * @return Approval chain
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowApprovalChain> getChainById(@PathVariable Long id) {
        log.debug("REST request to get approval chain by ID: {}", id);
        return approvalChainService.getChainById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get approval chain by code
     *
     * @param code Chain code
     * @return Approval chain
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowApprovalChain> getChainByCode(@PathVariable String code) {
        log.debug("REST request to get approval chain by code: {}", code);
        return approvalChainService.getChainByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new approval chain
     *
     * @param chain Approval chain to create
     * @return Created approval chain
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowApprovalChain> createChain(@Valid @RequestBody WorkflowApprovalChain chain) {
        log.debug("REST request to create approval chain: {}", chain.getChainCode());
        WorkflowApprovalChain createdChain = approvalChainService.createChain(chain);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChain);
    }

    /**
     * Update an existing approval chain
     *
     * @param id Chain ID
     * @param chain Updated chain data
     * @return Updated approval chain
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowApprovalChain> updateChain(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowApprovalChain chain) {
        log.debug("REST request to update approval chain: {}", id);
        WorkflowApprovalChain updatedChain = approvalChainService.updateChain(id, chain);
        return ResponseEntity.ok(updatedChain);
    }

    /**
     * Get all levels for an approval chain (ordered)
     *
     * @param id Chain ID
     * @return List of approval levels
     */
    @GetMapping("/{id}/levels")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowApprovalLevel>> getLevels(@PathVariable Long id) {
        log.debug("REST request to get levels for approval chain: {}", id);
        List<WorkflowApprovalLevel> levels = approvalChainService.getChainLevels(id);
        return ResponseEntity.ok(levels);
    }

    /**
     * Add a new level to an approval chain
     *
     * @param id Chain ID
     * @param level Approval level to add
     * @return Created approval level
     */
    @PostMapping("/{id}/levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowApprovalLevel> addLevel(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowApprovalLevel level) {
        log.debug("REST request to add level to approval chain: {}", id);
        WorkflowApprovalLevel createdLevel = approvalChainService.addLevel(id, level);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLevel);
    }

    /**
     * Remove a level from an approval chain
     *
     * @param levelId Level ID
     * @return No content
     */
    @DeleteMapping("/levels/{levelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeLevel(@PathVariable Long levelId) {
        log.debug("REST request to remove approval level: {}", levelId);
        approvalChainService.removeLevel(levelId);
        return ResponseEntity.noContent().build();
    }
}
