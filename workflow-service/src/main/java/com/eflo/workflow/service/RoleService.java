package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowRole;
import com.eflo.workflow.domain.repository.WorkflowRoleRepository;
import com.eflo.workflow.exception.DuplicateRoleException;
import com.eflo.workflow.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Role Service
 *
 * Manages workflow roles and their operations.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final WorkflowRoleRepository roleRepository;

    /**
     * Get all active roles
     *
     * @return List of active workflow roles
     */
    public List<WorkflowRole> getAllActiveRoles() {
        log.debug("Retrieving all active roles");
        return roleRepository.findByIsActiveTrue();
    }

    /**
     * Get role by ID
     *
     * @param id Role ID
     * @return Optional containing the role if found
     */
    public Optional<WorkflowRole> getRoleById(Long id) {
        log.debug("Retrieving role by ID: {}", id);
        return roleRepository.findById(id);
    }

    /**
     * Get role by code
     *
     * @param roleCode Role code
     * @return Optional containing the role if found
     */
    public Optional<WorkflowRole> getRoleByCode(String roleCode) {
        log.debug("Retrieving role by code: {}", roleCode);
        return roleRepository.findByRoleCode(roleCode);
    }

    /**
     * Create a new role
     *
     * @param role Role to create
     * @return Created role
     * @throws DuplicateRoleException if role code already exists
     */
    @Transactional
    public WorkflowRole createRole(WorkflowRole role) {
        log.info("Creating new role with code: {}", role.getRoleCode());

        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            log.error("Role with code {} already exists", role.getRoleCode());
            throw new DuplicateRoleException(role.getRoleCode());
        }

        WorkflowRole savedRole = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", savedRole.getId());
        return savedRole;
    }

    /**
     * Update an existing role
     *
     * @param id Role ID
     * @param updatedRole Updated role data
     * @return Updated role
     * @throws RoleNotFoundException if role not found
     * @throws DuplicateRoleException if new role code already exists
     */
    @Transactional
    public WorkflowRole updateRole(Long id, WorkflowRole updatedRole) {
        log.info("Updating role with ID: {}", id);

        WorkflowRole existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        // Check if role code is being changed and if new code already exists
        if (!existingRole.getRoleCode().equals(updatedRole.getRoleCode()) &&
                roleRepository.existsByRoleCode(updatedRole.getRoleCode())) {
            log.error("Role with code {} already exists", updatedRole.getRoleCode());
            throw new DuplicateRoleException(updatedRole.getRoleCode());
        }

        // Update fields
        existingRole.setRoleCode(updatedRole.getRoleCode());
        existingRole.setRoleName(updatedRole.getRoleName());
        existingRole.setDescription(updatedRole.getDescription());
        existingRole.setLevel(updatedRole.getLevel());
        existingRole.setUpdatedBy(updatedRole.getUpdatedBy());

        WorkflowRole savedRole = roleRepository.save(existingRole);
        log.info("Role updated successfully: {}", savedRole.getRoleCode());
        return savedRole;
    }

    /**
     * Deactivate a role (soft delete)
     *
     * @param id Role ID
     * @throws RoleNotFoundException if role not found
     */
    @Transactional
    public void deactivateRole(Long id) {
        log.info("Deactivating role with ID: {}", id);

        WorkflowRole role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        role.setIsActive(false);
        roleRepository.save(role);
        log.info("Role deactivated successfully: {}", role.getRoleCode());
    }

    /**
     * Get roles higher than specified level
     * Note: Lower level numbers indicate higher authority (1 is highest)
     *
     * @param level Level threshold
     * @return List of roles with higher authority (lower level numbers)
     */
    public List<WorkflowRole> getRolesHigherThan(Integer level) {
        log.debug("Retrieving roles higher than level: {}", level);
        return roleRepository.findHigherOrEqualRoles(level);
    }

    /**
     * Get roles lower than specified level
     * Note: Higher level numbers indicate lower authority
     *
     * @param level Level threshold
     * @return List of roles with lower authority (higher level numbers)
     */
    public List<WorkflowRole> getRolesLowerThan(Integer level) {
        log.debug("Retrieving roles lower than level: {}", level);
        return roleRepository.findLowerOrEqualRoles(level);
    }
}
