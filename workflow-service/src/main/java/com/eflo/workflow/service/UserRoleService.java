package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowRole;
import com.eflo.workflow.domain.WorkflowUserRole;
import com.eflo.workflow.domain.repository.WorkflowRoleRepository;
import com.eflo.workflow.domain.repository.WorkflowUserRoleRepository;
import com.eflo.workflow.exception.RoleNotFoundException;
import com.eflo.workflow.exception.UserRoleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Role Service
 *
 * Manages user role assignments and their operations.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleService {

    private final WorkflowUserRoleRepository userRoleRepository;
    private final WorkflowRoleRepository roleRepository;

    /**
     * Get all roles assigned to a user
     *
     * @param userId User ID
     * @return List of user roles
     */
    public List<WorkflowUserRole> getUserRoles(Long userId) {
        log.debug("Retrieving all roles for user: {}", userId);
        return userRoleRepository.findByUserId(userId);
    }

    /**
     * Get active roles for a user
     *
     * @param userId User ID
     * @return List of active user roles
     */
    public List<WorkflowUserRole> getActiveUserRoles(Long userId) {
        log.debug("Retrieving active roles for user: {}", userId);
        return userRoleRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get valid user roles at a specific date/time
     *
     * @param userId User ID
     * @param dateTime Date/time to check validity
     * @return List of valid user roles at the specified time
     */
    public List<WorkflowUserRole> getValidUserRolesAt(Long userId, LocalDateTime dateTime) {
        log.debug("Retrieving valid roles for user {} at {}", userId, dateTime);
        return userRoleRepository.findValidUserRolesAt(userId, dateTime);
    }

    /**
     * Assign a role to a user
     *
     * @param userId User ID
     * @param roleId Role ID
     * @param affaireCode Affaire code (optional)
     * @param branchCode Branch code (optional)
     * @return Created user role assignment
     * @throws RoleNotFoundException if role not found
     */
    @Transactional
    public WorkflowUserRole assignRoleToUser(Long userId, Long roleId, String affaireCode, String branchCode) {
        log.info("Assigning role {} to user {} (affaire: {}, branch: {})",
                roleId, userId, affaireCode, branchCode);

        WorkflowRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        WorkflowUserRole userRole = WorkflowUserRole.builder()
                .userId(userId)
                .role(role)
                .affaireCode(affaireCode)
                .branchCode(branchCode)
                .isActive(true)
                .validFrom(LocalDateTime.now())
                .build();

        WorkflowUserRole savedUserRole = userRoleRepository.save(userRole);
        log.info("Role {} assigned to user {} successfully with ID: {}",
                role.getRoleCode(), userId, savedUserRole.getId());
        return savedUserRole;
    }

    /**
     * Revoke a user role assignment
     *
     * @param userRoleId User role ID
     * @throws UserRoleNotFoundException if user role not found
     */
    @Transactional
    public void revokeUserRole(Long userRoleId) {
        log.info("Revoking user role with ID: {}", userRoleId);

        WorkflowUserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new UserRoleNotFoundException(userRoleId));

        userRole.setIsActive(false);
        userRole.setValidTo(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("User role {} revoked successfully for user: {}",
                userRole.getRole().getRoleCode(), userRole.getUserId());
    }

    /**
     * Check if a user has a specific role
     *
     * @param userId User ID
     * @param roleCode Role code
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Long userId, String roleCode) {
        log.debug("Checking if user {} has role: {}", userId, roleCode);

        List<WorkflowUserRole> userRoles = userRoleRepository.findByUserIdAndIsActiveTrue(userId);
        boolean hasRole = userRoles.stream()
                .anyMatch(ur -> ur.getRole().getRoleCode().equals(roleCode) && ur.isCurrentlyValid());

        log.debug("User {} {} role {}", userId, hasRole ? "has" : "does not have", roleCode);
        return hasRole;
    }

    /**
     * Get all users with a specific role
     *
     * @param roleCode Role code
     * @return List of user IDs
     */
    public List<Long> getUsersByRole(String roleCode) {
        log.debug("Retrieving users with role: {}", roleCode);

        WorkflowRole role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new RoleNotFoundException(roleCode));

        return userRoleRepository.findUserIdsByRoleId(role.getId());
    }

    /**
     * Assign a role to a user (alias for assignRoleToUser)
     *
     * @param userId User ID
     * @param roleId Role ID
     * @param affaireCode Affaire code (optional)
     * @param branchCode Branch code (optional)
     * @return Created user role assignment
     */
    @Transactional
    public WorkflowUserRole assignRole(Long userId, Long roleId, String affaireCode, String branchCode) {
        return assignRoleToUser(userId, roleId, affaireCode, branchCode);
    }

    /**
     * Revoke a user role assignment (alias for revokeUserRole)
     *
     * @param userRoleId User role ID
     */
    @Transactional
    public void revokeRole(Long userRoleId) {
        revokeUserRole(userRoleId);
    }

    /**
     * Get all users with a specific role by role ID
     *
     * @param roleId Role ID
     * @return List of user IDs
     */
    public List<Long> getUsersByRoleId(Long roleId) {
        log.debug("Retrieving users with role ID: {}", roleId);
        return userRoleRepository.findUserIdsByRoleId(roleId);
    }
}
