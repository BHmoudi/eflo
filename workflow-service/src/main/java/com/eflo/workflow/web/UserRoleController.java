package com.eflo.workflow.web;

import com.eflo.workflow.domain.WorkflowUserRole;
import com.eflo.workflow.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Role Controller
 *
 * REST API for managing user role assignments.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-roles")
@RequiredArgsConstructor
@CrossOrigin
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * Get all roles for a specific user
     *
     * @param userId User ID
     * @return List of user roles
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<WorkflowUserRole>> getUserRoles(@PathVariable Long userId) {
        log.debug("REST request to get roles for user: {}", userId);
        List<WorkflowUserRole> userRoles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(userRoles);
    }

    /**
     * Assign a role to a user
     *
     * @param userId User ID
     * @param roleId Role ID
     * @param affaireCode Optional affaire code for context-specific role
     * @param branchCode Optional branch code for context-specific role
     * @return Created user role assignment
     */
    @PostMapping("/user/{userId}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowUserRole> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @RequestParam(required = false) String affaireCode,
            @RequestParam(required = false) String branchCode) {
        log.debug("REST request to assign role {} to user {} (affaire: {}, branch: {})",
                roleId, userId, affaireCode, branchCode);
        WorkflowUserRole userRole = userRoleService.assignRole(userId, roleId, affaireCode, branchCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRole);
    }

    /**
     * Revoke a user role assignment
     *
     * @param userRoleId User role assignment ID
     * @return No content
     */
    @DeleteMapping("/{userRoleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeRole(@PathVariable Long userRoleId) {
        log.debug("REST request to revoke user role: {}", userRoleId);
        userRoleService.revokeRole(userRoleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a user has a specific role
     *
     * @param userId User ID
     * @param roleCode Role code to check
     * @return True if user has the role, false otherwise
     */
    @GetMapping("/user/{userId}/has-role/{roleCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Boolean> hasRole(
            @PathVariable Long userId,
            @PathVariable String roleCode) {
        log.debug("REST request to check if user {} has role: {}", userId, roleCode);
        boolean hasRole = userRoleService.hasRole(userId, roleCode);
        return ResponseEntity.ok(hasRole);
    }
}
