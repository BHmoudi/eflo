package com.eflo.user.service;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserBusinessUnit;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.UserBusinessUnitRepository;
import com.eflo.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing User-Business Unit assignments
 *
 * Handles assignment, removal, and primary business unit management.
 */
@Slf4j
@Service
public class UserBusinessUnitService {

    private final UserBusinessUnitRepository userBusinessUnitRepository;
    private final UserRepository userRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserActivityService userActivityService;

    public UserBusinessUnitService(UserBusinessUnitRepository userBusinessUnitRepository,
                                   UserRepository userRepository,
                                   BusinessUnitRepository businessUnitRepository,
                                   UserActivityService userActivityService) {
        this.userBusinessUnitRepository = userBusinessUnitRepository;
        this.userRepository = userRepository;
        this.businessUnitRepository = businessUnitRepository;
        this.userActivityService = userActivityService;
    }

    /**
     * Assign user to business unit
     *
     * @param userId User ID
     * @param businessUnitId Business unit ID
     * @param isPrimary Whether this is the primary business unit
     * @param assignedBy Who performed this action
     * @return Created assignment
     */
    @Transactional
    public UserBusinessUnit assignUserToBusinessUnit(Long userId, Long businessUnitId,
                                                      boolean isPrimary, String assignedBy) {
        log.info("Assigning user {} to business unit {}", userId, businessUnitId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        // Check if assignment already exists
        Optional<UserBusinessUnit> existingAssignment =
                userBusinessUnitRepository.findByUserAndBusinessUnit(user, businessUnit);

        if (existingAssignment.isPresent()) {
            UserBusinessUnit assignment = existingAssignment.get();
            if (assignment.getIsActive()) {
                throw new IllegalArgumentException("User is already assigned to this business unit");
            } else {
                // Reactivate existing assignment
                assignment.reactivate(assignedBy);
                if (isPrimary) {
                    setPrimaryBusinessUnit(userId, businessUnitId, assignedBy);
                }
                userBusinessUnitRepository.save(assignment);

                // Log activity
                userActivityService.logActivity(user, ActivityType.BUSINESS_UNIT_ASSIGNMENT,
                        "User reassigned to business unit: " + businessUnit.getName(),
                        assignedBy, businessUnitId);

                return assignment;
            }
        }

        // If this is primary, clear other primary flags
        if (isPrimary) {
            clearPrimaryFlags(userId);
        }

        // Create new assignment
        UserBusinessUnit assignment = UserBusinessUnit.builder()
                .user(user)
                .businessUnit(businessUnit)
                .isPrimary(isPrimary)
                .isActive(true)
                .assignedAt(LocalDate.now())
                .assignedBy(assignedBy)
                .build();

        UserBusinessUnit savedAssignment = userBusinessUnitRepository.save(assignment);

        // Log activity
        userActivityService.logActivity(user, ActivityType.BUSINESS_UNIT_ASSIGNMENT,
                "User assigned to business unit: " + businessUnit.getName(),
                assignedBy, businessUnitId);

        log.info("Successfully assigned user {} to business unit {}", user.getEmail(), businessUnit.getName());
        return savedAssignment;
    }

    /**
     * Remove user from business unit
     *
     * @param userId User ID
     * @param businessUnitId Business unit ID
     * @param removedBy Who performed this action
     */
    @Transactional
    public void removeUserFromBusinessUnit(Long userId, Long businessUnitId, String removedBy) {
        log.info("Removing user {} from business unit {}", userId, businessUnitId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        UserBusinessUnit assignment = userBusinessUnitRepository
                .findByUserAndBusinessUnit(user, businessUnit)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        assignment.deactivate(removedBy);
        userBusinessUnitRepository.save(assignment);

        // Log activity
        userActivityService.logActivity(user, ActivityType.BUSINESS_UNIT_REMOVAL,
                "User removed from business unit: " + businessUnit.getName(),
                removedBy, businessUnitId);

        log.info("Successfully removed user {} from business unit {}", user.getEmail(), businessUnit.getName());
    }

    /**
     * Set primary business unit for user
     *
     * @param userId User ID
     * @param businessUnitId Business unit ID
     * @param performedBy Who performed this action
     */
    @Transactional
    public void setPrimaryBusinessUnit(Long userId, Long businessUnitId, String performedBy) {
        log.info("Setting primary business unit {} for user {}", businessUnitId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        // Clear all primary flags for this user
        clearPrimaryFlags(userId);

        // Set new primary
        UserBusinessUnit assignment = userBusinessUnitRepository
                .findByUserAndBusinessUnit(user, businessUnit)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found. User must be assigned to business unit first."));

        assignment.markAsPrimary();
        userBusinessUnitRepository.save(assignment);

        // Log activity
        userActivityService.logActivity(user, ActivityType.PRIMARY_BUSINESS_UNIT_CHANGE,
                "Primary business unit set to: " + businessUnit.getName(),
                performedBy, businessUnitId);

        log.info("Successfully set primary business unit {} for user {}", businessUnit.getName(), user.getEmail());
    }

    /**
     * Clear primary flags for all business units of a user
     *
     * @param userId User ID
     */
    private void clearPrimaryFlags(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<UserBusinessUnit> assignments = userBusinessUnitRepository.findByUserAndIsActiveTrue(user);
        for (UserBusinessUnit assignment : assignments) {
            if (assignment.getIsPrimary()) {
                assignment.markAsNonPrimary();
                userBusinessUnitRepository.save(assignment);
            }
        }
    }

    /**
     * Get all active business units for a user
     *
     * @param userId User ID
     * @return List of active business unit assignments
     */
    @Transactional(readOnly = true)
    public List<UserBusinessUnit> getUserBusinessUnits(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userBusinessUnitRepository.findByUserAndIsActiveTrue(user);
    }

    /**
     * Get primary business unit for a user
     *
     * @param userId User ID
     * @return Optional primary business unit assignment
     */
    @Transactional(readOnly = true)
    public Optional<UserBusinessUnit> getPrimaryBusinessUnit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userBusinessUnitRepository.findByUserAndIsPrimaryTrueAndIsActiveTrue(user);
    }

    /**
     * Get all users assigned to a business unit
     *
     * @param businessUnitId Business unit ID
     * @return List of user assignments
     */
    @Transactional(readOnly = true)
    public List<UserBusinessUnit> getBusinessUnitUsers(Long businessUnitId) {
        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        return userBusinessUnitRepository.findByBusinessUnitAndIsActiveTrue(businessUnit);
    }

    /**
     * Check if user is assigned to business unit
     *
     * @param userId User ID
     * @param businessUnitId Business unit ID
     * @return True if user is assigned
     */
    @Transactional(readOnly = true)
    public boolean isUserAssignedToBusinessUnit(Long userId, Long businessUnitId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        Optional<UserBusinessUnit> assignment = userBusinessUnitRepository
                .findByUserAndBusinessUnit(user, businessUnit);

        return assignment.isPresent() && assignment.get().getIsActive();
    }

    /**
     * Get assignment by ID
     *
     * @param assignmentId Assignment ID
     * @return Optional assignment
     */
    @Transactional(readOnly = true)
    public Optional<UserBusinessUnit> findById(Long assignmentId) {
        return userBusinessUnitRepository.findById(assignmentId);
    }
}
