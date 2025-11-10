package com.eflo.workflow.web;

import com.eflo.workflow.domain.WorkflowRole;
import com.eflo.workflow.domain.WorkflowUserRole;
import com.eflo.workflow.service.RoleService;
import com.eflo.workflow.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Controller
 *
 * REST API for managing workflow roles.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin
public class RoleController {

    private final RoleService roleService;
    private final UserRoleService userRoleService;

    /**
     * Get all active roles
     *
     * @return List of all active workflow roles
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowRole>> getAllRoles() {
        log.debug("REST request to get all active roles");
        List<WorkflowRole> roles = roleService.getAllActiveRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get role by ID
     *
     * @param id Role ID
     * @return Workflow role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowRole> getRoleById(@PathVariable Long id) {
        log.debug("REST request to get role by ID: {}", id);
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get role by code
     *
     * @param code Role code
     * @return Workflow role
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<WorkflowRole> getRoleByCode(@PathVariable String code) {
        log.debug("REST request to get role by code: {}", code);
        return roleService.getRoleByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new role
     *
     * @param role Role to create
     * @return Created role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowRole> createRole(@Valid @RequestBody WorkflowRole role) {
        log.debug("REST request to create role: {}", role.getRoleCode());
        WorkflowRole createdRole = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    /**
     * Update an existing role
     *
     * @param id Role ID
     * @param role Updated role data
     * @return Updated role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowRole> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowRole role) {
        log.debug("REST request to update role: {}", id);
        WorkflowRole updatedRole = roleService.updateRole(id, role);
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Deactivate a role (soft delete)
     *
     * @param id Role ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateRole(@PathVariable Long id) {
        log.debug("REST request to deactivate role: {}", id);
        roleService.deactivateRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users assigned to a specific role
     *
     * @param id Role ID
     * @return List of user IDs
     */
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Long>> getUsersWithRole(@PathVariable Long id) {
        log.debug("REST request to get users with role ID: {}", id);
        List<Long> userIds = userRoleService.getUsersByRoleId(id);
        return ResponseEntity.ok(userIds);
    }
}
