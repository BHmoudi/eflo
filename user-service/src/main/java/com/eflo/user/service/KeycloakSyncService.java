package com.eflo.user.service;

import com.eflo.user.config.KeycloakProperties;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserRole;
import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.domain.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for syncing users and roles between Keycloak and the database
 *
 * This service handles bidirectional sync between Keycloak and the local database,
 * ensuring consistency between both systems.
 */
@Slf4j
@Service
public class KeycloakSyncService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public KeycloakSyncService(Keycloak keycloak,
                               KeycloakProperties keycloakProperties,
                               UserRepository userRepository,
                               UserRoleRepository userRoleRepository) {
        this.keycloak = keycloak;
        this.keycloakProperties = keycloakProperties;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Sync all users from Keycloak to database
     * Not transactional - each user sync has its own transaction (REQUIRES_NEW)
     *
     * @return Number of users synced
     */
    public int syncAllUsersFromKeycloak() {
        log.info("Starting sync of all users from Keycloak...");
        int syncedCount = 0;

        try {
            RealmResource realmResource = getRealmResource();
            List<UserRepresentation> keycloakUsers = realmResource.users().list();

            for (UserRepresentation keycloakUser : keycloakUsers) {
                try {
                    syncSingleUserFromKeycloak(UUID.fromString(keycloakUser.getId()));
                    syncedCount++;
                } catch (Exception e) {
                    log.error("Error syncing user: {}", keycloakUser.getUsername(), e);
                }
            }

            log.info("Successfully synced {} users from Keycloak", syncedCount);
        } catch (Exception e) {
            log.error("Error connecting to Keycloak for sync: {}", e.getMessage());
            log.debug("Keycloak sync connection error details", e);
        }

        return syncedCount;
    }

    /**
     * Sync a single user from Keycloak to database
     * Uses REQUIRES_NEW to avoid rolling back parent transaction on errors
     *
     * @param keycloakId Keycloak user ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncSingleUserFromKeycloak(UUID keycloakId) {
        log.debug("Syncing user from Keycloak: {}", keycloakId);

        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(keycloakId.toString());
            UserRepresentation keycloakUser = userResource.toRepresentation();

            // Find or create user in database
            Optional<User> existingUserOpt = userRepository.findByKeycloakId(keycloakId);
            User user;

            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                updateUserFromKeycloakRepresentation(user, keycloakUser);
            } else {
                user = createUserFromKeycloakRepresentation(keycloakUser);
            }

            user.markSyncedFromKeycloak();
            userRepository.save(user);

            // Sync roles
            syncUserRolesFromKeycloak(user, userResource);

            log.info("Successfully synced user from Keycloak: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error syncing user from Keycloak: {} - {}", keycloakId, e.getMessage());
            log.debug("Sync error details", e);
            // Don't throw - just log and continue to avoid rolling back parent transaction
        }
    }

    /**
     * Sync user roles from Keycloak to database
     *
     * @param user User entity
     * @param userResource Keycloak user resource
     */
    @Transactional
    public void syncUserRolesFromKeycloak(User user, UserResource userResource) {
        log.debug("Syncing roles for user: {}", user.getEmail());

        try {
            // Get current roles from Keycloak
            List<RoleRepresentation> keycloakRoles = userResource.roles().realmLevel().listEffective();
            Set<String> keycloakRoleNames = new HashSet<>();
            for (RoleRepresentation role : keycloakRoles) {
                keycloakRoleNames.add(role.getName());
            }

            // Get current roles from database
            List<UserRole> dbRoles = userRoleRepository.findByUserAndSource(user, RoleSource.KEYCLOAK);
            Set<String> dbRoleNames = new HashSet<>();
            Map<String, UserRole> dbRoleMap = new HashMap<>();
            for (UserRole dbRole : dbRoles) {
                dbRoleNames.add(dbRole.getRoleName().name());
                dbRoleMap.put(dbRole.getRoleName().name(), dbRole);
            }

            // Filter out system roles
            Set<String> systemRoles = Set.of("offline_access", "uma_authorization", "default-roles-eflo");
            keycloakRoleNames.removeAll(systemRoles);

            // Add new roles from Keycloak
            for (String roleName : keycloakRoleNames) {
                if (!dbRoleNames.contains(roleName)) {
                    try {
                        UserRoleEnum roleEnum = UserRoleEnum.valueOf(roleName);
                        UserRole newRole = UserRole.builder()
                                .user(user)
                                .roleName(roleEnum)
                                .source(RoleSource.KEYCLOAK)
                                .isActive(true)
                                .assignedAt(java.time.LocalDate.now())
                                .build();
                        newRole.markSyncedFromKeycloak();
                        userRoleRepository.save(newRole);
                        log.debug("Added role {} to user {}", roleName, user.getEmail());
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown role in Keycloak: {}", roleName);
                    }
                }
            }

            // Deactivate roles that are no longer in Keycloak
            for (String roleName : dbRoleNames) {
                if (!keycloakRoleNames.contains(roleName)) {
                    UserRole roleToDeactivate = dbRoleMap.get(roleName);
                    roleToDeactivate.revoke("KEYCLOAK_SYNC");
                    userRoleRepository.save(roleToDeactivate);
                    log.debug("Revoked role {} from user {}", roleName, user.getEmail());
                }
            }

            log.info("Successfully synced roles for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error syncing roles for user: {}", user.getEmail(), e);
            throw new RuntimeException("Error syncing user roles from Keycloak", e);
        }
    }

    /**
     * Create a new user from Keycloak representation
     *
     * @param keycloakUser Keycloak user representation
     * @return User entity
     */
    private User createUserFromKeycloakRepresentation(UserRepresentation keycloakUser) {
        Map<String, List<String>> attributes = keycloakUser.getAttributes();

        User user = User.builder()
                .keycloakId(UUID.fromString(keycloakUser.getId()))
                .keycloakUsername(keycloakUser.getUsername())
                .email(keycloakUser.getEmail())
                .firstName(keycloakUser.getFirstName() != null ? keycloakUser.getFirstName() : "")
                .lastName(keycloakUser.getLastName() != null ? keycloakUser.getLastName() : "")
                .isActive(keycloakUser.isEnabled())
                .isDeleted(false)
                .build();

        // Set employee number (required field)
        if (attributes != null && attributes.containsKey("employeeNumber")) {
            user.setEmployeeNumber(attributes.get("employeeNumber").get(0));
        } else {
            // Generate temporary employee number if not set
            user.setEmployeeNumber("EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Set optional attributes
        if (attributes != null) {
            if (attributes.containsKey("phoneNumber")) {
                user.setPhoneNumber(attributes.get("phoneNumber").get(0));
            }
            if (attributes.containsKey("department")) {
                user.setDepartment(attributes.get("department").get(0));
            }
        }

        return user;
    }

    /**
     * Update existing user from Keycloak representation
     *
     * @param user User entity to update
     * @param keycloakUser Keycloak user representation
     */
    private void updateUserFromKeycloakRepresentation(User user, UserRepresentation keycloakUser) {
        Map<String, List<String>> attributes = keycloakUser.getAttributes();

        user.setKeycloakUsername(keycloakUser.getUsername());
        user.setEmail(keycloakUser.getEmail());
        user.setFirstName(keycloakUser.getFirstName() != null ? keycloakUser.getFirstName() : user.getFirstName());
        user.setLastName(keycloakUser.getLastName() != null ? keycloakUser.getLastName() : user.getLastName());
        user.setIsActive(keycloakUser.isEnabled());

        // Update optional attributes
        if (attributes != null) {
            if (attributes.containsKey("employeeNumber")) {
                user.setEmployeeNumber(attributes.get("employeeNumber").get(0));
            }
            if (attributes.containsKey("phoneNumber")) {
                user.setPhoneNumber(attributes.get("phoneNumber").get(0));
            }
            if (attributes.containsKey("department")) {
                user.setDepartment(attributes.get("department").get(0));
            }
        }
    }

    /**
     * Get realm resource
     */
    private RealmResource getRealmResource() {
        return keycloak.realm(keycloakProperties.getRealm());
    }

    /**
     * Sync status information
     */
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            RealmResource realmResource = getRealmResource();
            List<UserRepresentation> keycloakUsers = realmResource.users().list();
            long dbUserCount = userRepository.count();

            status.put("keycloakUserCount", keycloakUsers.size());
            status.put("databaseUserCount", dbUserCount);
            status.put("lastCheck", LocalDateTime.now());
            status.put("status", "healthy");
        } catch (Exception e) {
            log.error("Error getting sync status", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }

        return status;
    }
}
