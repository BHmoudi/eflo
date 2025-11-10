package com.eflo.user.service;

import com.eflo.user.config.KeycloakProperties;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Service for managing users in Keycloak
 *
 * Handles user creation, updates, and role management in Keycloak.
 */
@Slf4j
@Service
public class KeycloakUserManagementService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;

    public KeycloakUserManagementService(Keycloak keycloak, KeycloakProperties keycloakProperties) {
        this.keycloak = keycloak;
        this.keycloakProperties = keycloakProperties;
    }

    /**
     * Create a new user in Keycloak
     *
     * @param user User entity from database
     * @param temporaryPassword Temporary password for the user
     * @return Keycloak user ID
     */
    public UUID createUserInKeycloak(User user, String temporaryPassword) {
        try {
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            // Create user representation
            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(user.getEmail());
            userRep.setEmail(user.getEmail());
            userRep.setFirstName(user.getFirstName());
            userRep.setLastName(user.getLastName());
            userRep.setEnabled(user.getIsActive());
            userRep.setEmailVerified(true);

            // Set custom attributes
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("employeeNumber", Collections.singletonList(user.getEmployeeNumber()));
            if (user.getPhoneNumber() != null) {
                attributes.put("phoneNumber", Collections.singletonList(user.getPhoneNumber()));
            }
            if (user.getDepartment() != null) {
                attributes.put("department", Collections.singletonList(user.getDepartment()));
            }
            userRep.setAttributes(attributes);

            // Create user
            Response response = usersResource.create(userRep);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromLocation(response.getLocation().toString());
                UUID keycloakId = UUID.fromString(userId);

                // Set password
                setUserPassword(keycloakId, temporaryPassword, true);

                log.info("Successfully created user in Keycloak: {}", user.getEmail());
                return keycloakId;
            } else {
                log.error("Failed to create user in Keycloak: {}, Status: {}", user.getEmail(), response.getStatus());
                throw new RuntimeException("Failed to create user in Keycloak");
            }
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: {}", user.getEmail(), e);
            throw new RuntimeException("Error creating user in Keycloak", e);
        }
    }

    /**
     * Update existing user in Keycloak
     *
     * @param user User entity from database
     */
    public void updateUserInKeycloak(User user) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(user.getKeycloakId().toString());

            UserRepresentation userRep = userResource.toRepresentation();
            userRep.setEmail(user.getEmail());
            userRep.setFirstName(user.getFirstName());
            userRep.setLastName(user.getLastName());
            userRep.setEnabled(user.getIsActive());

            // Update custom attributes
            Map<String, List<String>> attributes = userRep.getAttributes() != null ?
                    new HashMap<>(userRep.getAttributes()) : new HashMap<>();
            attributes.put("employeeNumber", Collections.singletonList(user.getEmployeeNumber()));
            if (user.getPhoneNumber() != null) {
                attributes.put("phoneNumber", Collections.singletonList(user.getPhoneNumber()));
            }
            if (user.getDepartment() != null) {
                attributes.put("department", Collections.singletonList(user.getDepartment()));
            }
            userRep.setAttributes(attributes);

            userResource.update(userRep);
            log.info("Successfully updated user in Keycloak: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error updating user in Keycloak: {}", user.getEmail(), e);
            throw new RuntimeException("Error updating user in Keycloak", e);
        }
    }

    /**
     * Set or reset user password in Keycloak
     *
     * @param keycloakId Keycloak user ID
     * @param password New password
     * @param temporary Whether password is temporary
     */
    public void setUserPassword(UUID keycloakId, String password, boolean temporary) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(keycloakId.toString());

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(temporary);

            userResource.resetPassword(credential);
            log.info("Successfully set password for user in Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.warn("(Non-fatal) Error setting password for user in Keycloak: {} - proceeding with DB operation", keycloakId, e);
        }
    }

    /**
     * Assign role to user in Keycloak
     *
     * @param keycloakId Keycloak user ID
     * @param roleName Role name to assign
     */
    public void assignRoleToUser(UUID keycloakId, String roleName) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(keycloakId.toString());

            // Get realm role
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Assign role to user
            userResource.roles().realmLevel().add(Collections.singletonList(role));
            log.info("Successfully assigned role {} to user in Keycloak: {}", roleName, keycloakId);
        } catch (Exception e) {
            log.error("Error assigning role {} to user in Keycloak: {}", roleName, keycloakId, e);
            throw new RuntimeException("Error assigning role in Keycloak", e);
        }
    }

    /**
     * Remove role from user in Keycloak
     *
     * @param keycloakId Keycloak user ID
     * @param roleName Role name to remove
     */
    public void removeRoleFromUser(UUID keycloakId, String roleName) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(keycloakId.toString());

            // Get realm role
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Remove role from user
            userResource.roles().realmLevel().remove(Collections.singletonList(role));
            log.info("Successfully removed role {} from user in Keycloak: {}", roleName, keycloakId);
        } catch (Exception e) {
            log.error("Error removing role {} from user in Keycloak: {}", roleName, keycloakId, e);
            throw new RuntimeException("Error removing role in Keycloak", e);
        }
    }

    /**
     * Sync user roles to Keycloak
     *
     * @param user User entity with roles
     */
    public void syncUserRolesToKeycloak(User user) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(user.getKeycloakId().toString());

            // Get current Keycloak roles
            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listEffective();
            Set<String> currentRoleNames = new HashSet<>();
            for (RoleRepresentation role : currentRoles) {
                currentRoleNames.add(role.getName());
            }

            // Get expected roles from user entity
            Set<String> expectedRoleNames = new HashSet<>();
            for (UserRole userRole : user.getRoles()) {
                if (userRole.getIsActive()) {
                    expectedRoleNames.add(userRole.getRoleName().name());
                }
            }

            // Add missing roles
            for (String roleName : expectedRoleNames) {
                if (!currentRoleNames.contains(roleName)) {
                    assignRoleToUser(user.getKeycloakId(), roleName);
                }
            }

            // Remove extra roles (excluding default roles)
            Set<String> systemRoles = Set.of("offline_access", "uma_authorization", "default-roles-eflo");
            for (String roleName : currentRoleNames) {
                if (!expectedRoleNames.contains(roleName) && !systemRoles.contains(roleName)) {
                    removeRoleFromUser(user.getKeycloakId(), roleName);
                }
            }

            log.info("Successfully synced roles for user in Keycloak: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error syncing roles for user in Keycloak: {}", user.getEmail(), e);
            throw new RuntimeException("Error syncing roles in Keycloak", e);
        }
    }

    /**
     * Disable user in Keycloak
     *
     * @param keycloakId Keycloak user ID
     */
    public void disableUserInKeycloak(UUID keycloakId) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(keycloakId.toString());

            UserRepresentation userRep = userResource.toRepresentation();
            userRep.setEnabled(false);
            userResource.update(userRep);

            log.info("Successfully disabled user in Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.warn("(Non-fatal) Error disabling user in Keycloak: {} - proceeding with DB operation", keycloakId, e);
        }
    }

    /**
     * Enable user in Keycloak
     *
     * @param keycloakId Keycloak user ID
     */
    public void enableUserInKeycloak(UUID keycloakId) {
        try {
            RealmResource realmResource = getRealmResource();
            UserResource userResource = realmResource.users().get(keycloakId.toString());

            UserRepresentation userRep = userResource.toRepresentation();
            userRep.setEnabled(true);
            userResource.update(userRep);

            log.info("Successfully enabled user in Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.warn("(Non-fatal) Error enabling user in Keycloak: {} - proceeding with DB operation", keycloakId, e);
        }
    }

    /**
     * Get realm resource
     */
    private RealmResource getRealmResource() {
        return keycloak.realm(keycloakProperties.getRealm());
    }

    /**
     * Extract user ID from location header
     */
    private String extractUserIdFromLocation(String location) {
        String[] parts = location.split("/");
        return parts[parts.length - 1];
    }
}
