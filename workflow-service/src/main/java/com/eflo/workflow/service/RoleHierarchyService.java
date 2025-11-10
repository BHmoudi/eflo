package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowRole;
import com.eflo.workflow.domain.WorkflowRoleHierarchy;
import com.eflo.workflow.domain.repository.WorkflowRoleHierarchyRepository;
import com.eflo.workflow.domain.repository.WorkflowRoleRepository;
import com.eflo.workflow.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role Hierarchy Service
 *
 * Manages role hierarchies and their relationships.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleHierarchyService {

    private final WorkflowRoleHierarchyRepository roleHierarchyRepository;
    private final WorkflowRoleRepository roleRepository;

    /**
     * Get child roles for a parent role
     *
     * @param parentRoleId Parent role ID
     * @return List of role hierarchies where this role is the parent
     */
    public List<WorkflowRoleHierarchy> getChildRoles(Long parentRoleId) {
        log.debug("Retrieving child roles for parent role: {}", parentRoleId);
        return roleHierarchyRepository.findByParentRoleId(parentRoleId);
    }

    /**
     * Get parent roles for a child role
     *
     * @param childRoleId Child role ID
     * @return List of role hierarchies where this role is the child
     */
    public List<WorkflowRoleHierarchy> getParentRoles(Long childRoleId) {
        log.debug("Retrieving parent roles for child role: {}", childRoleId);
        return roleHierarchyRepository.findByChildRoleId(childRoleId);
    }

    /**
     * Check if an approver role can approve for a target role
     *
     * @param approverRoleId Approver role ID
     * @param targetRoleId Target role ID
     * @return true if approver can approve for target, false otherwise
     */
    public boolean canApproveFor(Long approverRoleId, Long targetRoleId) {
        log.debug("Checking if role {} can approve for role {}", approverRoleId, targetRoleId);

        // Check if approver is parent of target with approval permission
        List<WorkflowRoleHierarchy> hierarchies = roleHierarchyRepository.findByParentRoleId(approverRoleId);
        boolean canApprove = hierarchies.stream()
                .anyMatch(h -> h.getChildRole().getId().equals(targetRoleId) &&
                              h.getCanApproveForChild());

        log.debug("Role {} {} approve for role {}",
                approverRoleId, canApprove ? "can" : "cannot", targetRoleId);
        return canApprove;
    }

    /**
     * Get all roles that can be approved by a given role
     *
     * @param roleId Role ID
     * @return List of roles that can be approved
     */
    public List<WorkflowRole> getApprovableRoles(Long roleId) {
        log.debug("Retrieving approvable roles for role: {}", roleId);

        List<WorkflowRoleHierarchy> hierarchies = roleHierarchyRepository.findApprovableChildRoles(roleId);
        List<WorkflowRole> approvableRoles = hierarchies.stream()
                .map(WorkflowRoleHierarchy::getChildRole)
                .collect(Collectors.toList());

        log.debug("Found {} approvable roles for role {}", approvableRoles.size(), roleId);
        return approvableRoles;
    }

    /**
     * Create a new role hierarchy relationship
     *
     * @param parentRoleId Parent role ID
     * @param childRoleId Child role ID
     * @param canApprove Whether parent can approve for child
     * @param canDelegate Whether parent can delegate to child
     * @return Created role hierarchy
     * @throws RoleNotFoundException if parent or child role not found
     */
    @Transactional
    public WorkflowRoleHierarchy createHierarchy(Long parentRoleId, Long childRoleId,
                                                  boolean canApprove, boolean canDelegate) {
        log.info("Creating role hierarchy: parent={}, child={}, approve={}, delegate={}",
                parentRoleId, childRoleId, canApprove, canDelegate);

        WorkflowRole parentRole = roleRepository.findById(parentRoleId)
                .orElseThrow(() -> new RoleNotFoundException(parentRoleId));

        WorkflowRole childRole = roleRepository.findById(childRoleId)
                .orElseThrow(() -> new RoleNotFoundException(childRoleId));

        WorkflowRoleHierarchy hierarchy = WorkflowRoleHierarchy.builder()
                .parentRole(parentRole)
                .childRole(childRole)
                .canApproveForChild(canApprove)
                .canDelegateToChild(canDelegate)
                .canViewChildTasks(true)
                .build();

        WorkflowRoleHierarchy savedHierarchy = roleHierarchyRepository.save(hierarchy);
        log.info("Role hierarchy created successfully with ID: {} ({} -> {})",
                savedHierarchy.getId(), parentRole.getRoleCode(), childRole.getRoleCode());
        return savedHierarchy;
    }
}
