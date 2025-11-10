package com.eflo.user.service;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserRole;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.domain.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing user roles
 *
 * Handles role assignment, removal, and synchronization with Keycloak.
 */
@Slf4j
@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final KeycloakUserManagementService keycloakUserManagementService;
    private final UserActivityService userActivityService;

    public UserRoleService(UserRoleRepository userRoleRepository,
                           UserRepository userRepository,
                           BusinessUnitRepository businessUnitRepository,
                           KeycloakUserManagementService keycloakUserManagementService,
                           UserActivityService userActivityService) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.businessUnitRepository = businessUnitRepository;
        this.keycloakUserManagementService = keycloakUserManagementService;
        this.userActivityService = userActivityService;
    }

    /**
     * Assign a role to a user
     *
     * @param userId User ID
     * @param roleName Role name
     * @param businessUnitId Business unit ID (optional)
     * @param source Role source (MANUAL or KEYCLOAK)
     * @param assignedBy Who assigned the role
     * @return Created user role
     */
    @Transactional
    public UserRole assignRole(Long userId, UserRoleEnum roleName, Long businessUnitId,
                               RoleSource source, String assignedBy) {
        log.info("Assigning role {} to user {}", roleName, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = null;
        if (businessUnitId != null) {
            businessUnit = businessUnitRepository.findById(businessUnitId)
                    .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));
        }

        // Check if role already exists
        Optional<UserRole> existingRole = userRoleRepository
                .findByUserAndRoleNameAndBusinessUnit(user, roleName, businessUnit);

        if (existingRole.isPresent()) {
            UserRole role = existingRole.get();
            if (role.getIsActive()) {
                throw new IllegalArgumentException("User already has this role");
            } else {
                // Reactivate existing role
                role.reactivate(assignedBy);
                userRoleRepository.save(role);

                // Sync to Keycloak if source is MANUAL
                if (source == RoleSource.MANUAL) {
                    keycloakUserManagementService.assignRoleToUser(user.getKeycloakId(), roleName.name());
                }

                // Log activity
                userActivityService.logActivity(user, ActivityType.ROLE_ASSIGNMENT,
                        "Role reassigned: " + roleName.name(),
                        assignedBy, businessUnitId);

                return role;
            }
        }

        // Create new role
        UserRole userRole = UserRole.builder()
                .user(user)
                .roleName(roleName)
                .businessUnit(businessUnit)
                .source(source)
                .isActive(true)
                .assignedAt(LocalDate.now())
                .assignedBy(assignedBy)
                .build();

        UserRole savedRole = userRoleRepository.save(userRole);

        // Sync to Keycloak if source is MANUAL
        if (source == RoleSource.MANUAL) {
            keycloakUserManagementService.assignRoleToUser(user.getKeycloakId(), roleName.name());
        }

        // Log activity
        userActivityService.logActivity(user, ActivityType.ROLE_ASSIGNMENT,
                "Role assigned: " + roleName.name(),
                assignedBy, businessUnitId);

        log.info("Successfully assigned role {} to user {}", roleName, user.getEmail());
        return savedRole;
    }

    /**
     * Revoke a role from a user
     *
     * @param userId User ID
     * @param roleName Role name
     * @param businessUnitId Business unit ID (optional)
     * @param revokedBy Who revoked the role
     */
    @Transactional
    public void revokeRole(Long userId, UserRoleEnum roleName, Long businessUnitId, String revokedBy) {
        log.info("Revoking role {} from user {}", roleName, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = null;
        if (businessUnitId != null) {
            businessUnit = businessUnitRepository.findById(businessUnitId)
                    .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));
        }

        UserRole userRole = userRoleRepository.findByUserAndRoleNameAndBusinessUnit(user, roleName, businessUnit)
                .orElseThrow(() -> new IllegalArgumentException("Role assignment not found"));

        userRole.revoke(revokedBy);
        userRoleRepository.save(userRole);

        // Sync to Keycloak if source is MANUAL
        if (userRole.getSource() == RoleSource.MANUAL) {
            keycloakUserManagementService.removeRoleFromUser(user.getKeycloakId(), roleName.name());
        }

        // Log activity
        userActivityService.logActivity(user, ActivityType.ROLE_REVOCATION,
                "Role revoked: " + roleName.name(),
                revokedBy, businessUnitId);

        log.info("Successfully revoked role {} from user {}", roleName, user.getEmail());
    }

    /**
     * Revoke role by ID
     *
     * @param roleId Role ID
     * @param revokedBy Who revoked the role
     */
    @Transactional
    public void revokeRoleById(Long roleId, String revokedBy) {
        log.info("Revoking role by ID: {}", roleId);

        UserRole userRole = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        User user = userRole.getUser();
        userRole.revoke(revokedBy);
        userRoleRepository.save(userRole);

        // Sync to Keycloak if source is MANUAL
        if (userRole.getSource() == RoleSource.MANUAL) {
            keycloakUserManagementService.removeRoleFromUser(user.getKeycloakId(), userRole.getRoleName().name());
        }

        // Log activity
        userActivityService.logActivity(user, ActivityType.ROLE_REVOCATION,
                "Role revoked: " + userRole.getRoleName().name(),
                revokedBy, userRole.getBusinessUnit() != null ? userRole.getBusinessUnit().getId() : null);

        log.info("Successfully revoked role {} from user {}", userRole.getRoleName(), user.getEmail());
    }

    /**
     * Get all active roles for a user
     *
     * @param userId User ID
     * @return List of active user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userRoleRepository.findByUserAndIsActiveTrue(user);
    }

    /**
     * Get all roles (active and inactive) for a user
     *
     * @param userId User ID
     * @return List of all user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> getAllUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userRoleRepository.findByUser(user);
    }

    /**
     * Get roles by source for a user
     *
     * @param userId User ID
     * @param source Role source
     * @return List of user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUserRolesBySource(Long userId, RoleSource source) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userRoleRepository.findByUserAndSource(user, source);
    }

    /**
     * Check if user has a specific role
     *
     * @param userId User ID
     * @param roleName Role name
     * @return True if user has the role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, UserRoleEnum roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<UserRole> roles = userRoleRepository.findByUserAndIsActiveTrue(user);
        return roles.stream().anyMatch(r -> r.getRoleName() == roleName);
    }

    /**
     * Check if user has a specific role in a business unit
     *
     * @param userId User ID
     * @param roleName Role name
     * @param businessUnitId Business unit ID
     * @return True if user has the role in the business unit
     */
    @Transactional(readOnly = true)
    public boolean hasRoleInBusinessUnit(Long userId, UserRoleEnum roleName, Long businessUnitId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        Optional<UserRole> role = userRoleRepository.findByUserAndRoleNameAndBusinessUnit(
                user, roleName, businessUnit);

        return role.isPresent() && role.get().getIsActive();
    }

    /**
     * Get all users with a specific role
     *
     * @param roleName Role name
     * @return List of user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUsersWithRole(UserRoleEnum roleName) {
        return userRoleRepository.findByRoleNameAndIsActiveTrue(roleName);
    }

    /**
     * Get all users with a specific role in a business unit
     *
     * @param roleName Role name
     * @param businessUnitId Business unit ID
     * @return List of user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUsersWithRoleInBusinessUnit(UserRoleEnum roleName, Long businessUnitId) {
        BusinessUnit businessUnit = businessUnitRepository.findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        return userRoleRepository.findByRoleNameAndBusinessUnitAndIsActiveTrue(roleName, businessUnit);
    }

    /**
     * Sync all user roles to Keycloak
     *
     * @param userId User ID
     */
    @Transactional
    public void syncRolesToKeycloak(Long userId) {
        log.info("Syncing roles to Keycloak for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        keycloakUserManagementService.syncUserRolesToKeycloak(user);

        log.info("Successfully synced roles to Keycloak for user: {}", user.getEmail());
    }

    /**
     * Find role by ID
     *
     * @param roleId Role ID
     * @return Optional user role
     */
    @Transactional(readOnly = true)
    public Optional<UserRole> findById(Long roleId) {
        return userRoleRepository.findById(roleId);
    }
}
