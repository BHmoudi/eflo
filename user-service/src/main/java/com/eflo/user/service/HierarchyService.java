package com.eflo.user.service;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.Hierarchy;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.HierarchyRepository;
import com.eflo.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing organizational hierarchies
 *
 * Handles creation, updates, and queries for employee-manager relationships.
 */
@Slf4j
@Service
public class HierarchyService {

    private final HierarchyRepository hierarchyRepository;
    private final UserRepository userRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserActivityService userActivityService;

    public HierarchyService(HierarchyRepository hierarchyRepository,
                            UserRepository userRepository,
                            BusinessUnitRepository businessUnitRepository,
                            UserActivityService userActivityService) {
        this.hierarchyRepository = hierarchyRepository;
        this.userRepository = userRepository;
        this.businessUnitRepository = businessUnitRepository;
        this.userActivityService = userActivityService;
    }

    /**
     * Create a new hierarchy relationship
     *
     * @param employeeId Employee user ID
     * @param managerId Manager user ID
     * @param businessUnitId Business unit ID (optional)
     * @param level Hierarchy level
     * @param assignedBy Who performed this action
     * @return Created hierarchy
     */
    @Transactional
    public Hierarchy createHierarchy(Long employeeId, Long managerId, Long businessUnitId,
                                     Integer level, String assignedBy) {
        log.info("Creating hierarchy: employee {} -> manager {}", employeeId, managerId);

        if (employeeId.equals(managerId)) {
            throw new IllegalArgumentException("Employee cannot be their own manager");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        BusinessUnit businessUnit = null;
        if (businessUnitId != null) {
            businessUnit = businessUnitRepository.findById(businessUnitId)
                    .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));
        }

        // Check for existing active hierarchy
        List<Hierarchy> existingHierarchies = hierarchyRepository
                .findByEmployeeAndIsActiveTrue(employee);

        if (!existingHierarchies.isEmpty()) {
            throw new IllegalArgumentException("Employee already has an active manager. Remove the existing hierarchy first.");
        }

        // Check for circular reference
        if (wouldCreateCircularReference(employeeId, managerId)) {
            throw new IllegalArgumentException("This hierarchy would create a circular reference");
        }

        Hierarchy hierarchy = Hierarchy.builder()
                .employee(employee)
                .manager(manager)
                .businessUnit(businessUnit)
                .level(level != null ? level : 1)
                .isActive(true)
                .effectiveFrom(LocalDate.now())
                .assignedBy(assignedBy)
                .build();

        Hierarchy savedHierarchy = hierarchyRepository.save(hierarchy);

        // Log activity
        userActivityService.logActivity(employee, ActivityType.HIERARCHY_CHANGE,
                "Manager assigned: " + manager.getFullName(),
                assignedBy, businessUnitId);

        log.info("Successfully created hierarchy: employee {} -> manager {}",
                employee.getEmail(), manager.getEmail());
        return savedHierarchy;
    }

    /**
     * Update an existing hierarchy relationship
     *
     * @param hierarchyId Hierarchy ID
     * @param newManagerId New manager ID
     * @param businessUnitId Business unit ID (optional)
     * @param level Hierarchy level
     * @param performedBy Who performed this action
     * @return Updated hierarchy
     */
    @Transactional
    public Hierarchy updateHierarchy(Long hierarchyId, Long newManagerId, Long businessUnitId,
                                     Integer level, String performedBy) {
        log.info("Updating hierarchy: {}", hierarchyId);

        Hierarchy hierarchy = hierarchyRepository.findById(hierarchyId)
                .orElseThrow(() -> new IllegalArgumentException("Hierarchy not found: " + hierarchyId));

        if (hierarchy.getEmployee().getId().equals(newManagerId)) {
            throw new IllegalArgumentException("Employee cannot be their own manager");
        }

        User newManager = userRepository.findById(newManagerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + newManagerId));

        // Check for circular reference
        if (wouldCreateCircularReference(hierarchy.getEmployee().getId(), newManagerId)) {
            throw new IllegalArgumentException("This hierarchy would create a circular reference");
        }

        hierarchy.setManager(newManager);

        if (businessUnitId != null) {
            BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                    .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));
            hierarchy.setBusinessUnit(businessUnit);
        }

        if (level != null) {
            hierarchy.setLevel(level);
        }

        Hierarchy savedHierarchy = hierarchyRepository.save(hierarchy);

        // Log activity
        userActivityService.logActivity(hierarchy.getEmployee(), ActivityType.HIERARCHY_CHANGE,
                "Manager changed to: " + newManager.getFullName(),
                performedBy, businessUnitId);

        log.info("Successfully updated hierarchy: {}", hierarchyId);
        return savedHierarchy;
    }

    /**
     * Deactivate a hierarchy relationship
     *
     * @param hierarchyId Hierarchy ID
     * @param removedBy Who performed this action
     */
    @Transactional
    public void deactivateHierarchy(Long hierarchyId, String removedBy) {
        log.info("Deactivating hierarchy: {}", hierarchyId);

        Hierarchy hierarchy = hierarchyRepository.findById(hierarchyId)
                .orElseThrow(() -> new IllegalArgumentException("Hierarchy not found: " + hierarchyId));

        hierarchy.deactivate(removedBy);
        hierarchyRepository.save(hierarchy);

        // Log activity
        userActivityService.logActivity(hierarchy.getEmployee(), ActivityType.HIERARCHY_CHANGE,
                "Manager removed: " + hierarchy.getManager().getFullName(),
                removedBy, hierarchy.getBusinessUnit() != null ? hierarchy.getBusinessUnit().getId() : null);

        log.info("Successfully deactivated hierarchy: {}", hierarchyId);
    }

    /**
     * Get active hierarchy for an employee
     *
     * @param employeeId Employee ID
     * @return Optional hierarchy
     */
    @Transactional(readOnly = true)
    public Optional<Hierarchy> getActiveHierarchyForEmployee(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        List<Hierarchy> hierarchies = hierarchyRepository.findByEmployeeAndIsActiveTrue(employee);
        return hierarchies.isEmpty() ? Optional.empty() : Optional.of(hierarchies.get(0));
    }

    /**
     * Get all direct reports for a manager
     *
     * @param managerId Manager ID
     * @return List of hierarchies
     */
    @Transactional(readOnly = true)
    public List<Hierarchy> getDirectReports(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        return hierarchyRepository.findByManagerAndIsActiveTrue(manager);
    }

    /**
     * Get all direct reports for a manager in a specific business unit
     *
     * @param managerId Manager ID
     * @param businessUnitId Business unit ID
     * @return List of hierarchies
     */
    @Transactional(readOnly = true)
    public List<Hierarchy> getDirectReportsInBusinessUnit(Long managerId, Long businessUnitId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        return hierarchyRepository.findByManagerAndBusinessUnitAndIsActiveTrue(manager, businessUnit);
    }

    /**
     * Get complete management chain for an employee
     *
     * @param employeeId Employee ID
     * @return List of users in management chain (from direct manager to top)
     */
    @Transactional(readOnly = true)
    public List<User> getManagementChain(Long employeeId) {
        List<User> chain = new ArrayList<>();
        User currentEmployee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        int maxDepth = 20; // Prevent infinite loops
        int depth = 0;

        while (depth < maxDepth) {
            List<Hierarchy> hierarchies = hierarchyRepository.findByEmployeeAndIsActiveTrue(currentEmployee);
            if (hierarchies.isEmpty()) {
                break;
            }

            User manager = hierarchies.get(0).getManager();
            chain.add(manager);

            // Check if we've reached the top or found a circular reference
            if (chain.stream().filter(u -> u.getId().equals(manager.getId())).count() > 1) {
                log.warn("Circular reference detected in management chain for employee: {}", employeeId);
                break;
            }

            currentEmployee = manager;
            depth++;
        }

        return chain;
    }

    /**
     * Get all subordinates (direct and indirect) for a manager
     *
     * @param managerId Manager ID
     * @return List of all subordinate users
     */
    @Transactional(readOnly = true)
    public List<User> getAllSubordinates(Long managerId) {
        List<User> subordinates = new ArrayList<>();
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        collectSubordinates(manager, subordinates, 0, 20);
        return subordinates;
    }

    /**
     * Recursively collect all subordinates
     */
    private void collectSubordinates(User manager, List<User> subordinates, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            log.warn("Maximum hierarchy depth reached for manager: {}", manager.getId());
            return;
        }

        List<Hierarchy> directReports = hierarchyRepository.findByManagerAndIsActiveTrue(manager);
        for (Hierarchy hierarchy : directReports) {
            User employee = hierarchy.getEmployee();
            if (!subordinates.contains(employee)) {
                subordinates.add(employee);
                collectSubordinates(employee, subordinates, depth + 1, maxDepth);
            }
        }
    }

    /**
     * Check if creating a hierarchy would create a circular reference
     *
     * @param employeeId Employee ID
     * @param managerId Manager ID
     * @return True if circular reference would be created
     */
    private boolean wouldCreateCircularReference(Long employeeId, Long managerId) {
        try {
            List<User> managerChain = getManagementChain(managerId);
            return managerChain.stream().anyMatch(u -> u.getId().equals(employeeId));
        } catch (Exception e) {
            log.error("Error checking circular reference", e);
            return false;
        }
    }

    /**
     * Find hierarchy by ID
     *
     * @param hierarchyId Hierarchy ID
     * @return Optional hierarchy
     */
    @Transactional(readOnly = true)
    public Optional<Hierarchy> findById(Long hierarchyId) {
        return hierarchyRepository.findById(hierarchyId);
    }

    /**
     * Get all hierarchies in a business unit
     *
     * @param businessUnitId Business unit ID
     * @return List of hierarchies
     */
    @Transactional(readOnly = true)
    public List<Hierarchy> getBusinessUnitHierarchies(Long businessUnitId) {
        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        return hierarchyRepository.findByBusinessUnitAndIsActiveTrue(businessUnit);
    }
}
