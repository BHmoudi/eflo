package com.eflo.user.service;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserActivityLog;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.repository.UserActivityLogRepository;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.domain.repository.UserRoleRepository;
import com.eflo.user.exception.DuplicateUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing User entities
 *
 * Provides CRUD operations, search, login tracking, and user lifecycle management.
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserActivityLogRepository userActivityLogRepository;
    private final KeycloakUserManagementService keycloakUserManagementService;
    private final UserActivityService userActivityService;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository,
                       UserActivityLogRepository userActivityLogRepository,
                       KeycloakUserManagementService keycloakUserManagementService,
                       UserActivityService userActivityService,
                       UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userActivityLogRepository = userActivityLogRepository;
        this.keycloakUserManagementService = keycloakUserManagementService;
        this.userActivityService = userActivityService;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Create a new user
     *
     * @param user User entity to create
     * @param temporaryPassword Temporary password for Keycloak
     * @param performedBy Who performed this action
     * @return Created user
     */
    @Transactional
    public User createUser(User user, String temporaryPassword, String performedBy) {
        log.info("Creating new user: {}", user.getEmail());

        // Validate unique constraints
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateUserException(user.getEmail(), "User with email already exists: " + user.getEmail());
        }

        if (userRepository.findByEmployeeNumber(user.getEmployeeNumber()).isPresent()) {
            throw new DuplicateUserException(user.getEmployeeNumber(), "User with employee number already exists: " + user.getEmployeeNumber());
        }

        // Create user in Keycloak first (if keycloakId not provided)
        if (user.getKeycloakId() == null) {
            try {
                UUID keycloakId = keycloakUserManagementService.createUserInKeycloak(user, temporaryPassword);
                user.setKeycloakId(keycloakId);
            } catch (Exception e) {
                log.warn("Failed to create user in Keycloak, using generated UUID: {}", e.getMessage());
                user.setKeycloakId(UUID.randomUUID());
            }
        }

        if (user.getKeycloakUsername() == null) {
            user.setKeycloakUsername(user.getEmail());
        }

        // Save to database
        User savedUser = userRepository.save(user);

        // Log activity
        userActivityService.logActivity(savedUser, ActivityType.USER_CREATED,
                "User account created", performedBy, null);

        log.info("Successfully created user: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Update an existing user
     *
     * @param userId User ID
     * @param updatedUser Updated user data
     * @param performedBy Who performed this action
     * @return Updated user
     */
    @Transactional
    public User updateUser(Long userId, User updatedUser, String performedBy) {
        log.info("Updating user: {}", userId);

        User existingUser = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update fields
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setMobileNumber(updatedUser.getMobileNumber());
        existingUser.setOfficeExtension(updatedUser.getOfficeExtension());
        existingUser.setJobTitle(updatedUser.getJobTitle());
        existingUser.setDepartment(updatedUser.getDepartment());
        existingUser.setAddressLine1(updatedUser.getAddressLine1());
        existingUser.setAddressLine2(updatedUser.getAddressLine2());
        existingUser.setPostalCode(updatedUser.getPostalCode());
        existingUser.setCity(updatedUser.getCity());
        existingUser.setCountry(updatedUser.getCountry());
        existingUser.setHireDate(updatedUser.getHireDate());
        existingUser.setProfilePictureUrl(updatedUser.getProfilePictureUrl());
        existingUser.setBio(updatedUser.getBio());
        existingUser.setPreferences(updatedUser.getPreferences());

        // Update in Keycloak
        keycloakUserManagementService.updateUserInKeycloak(existingUser);

        // Save to database
        User savedUser = userRepository.save(existingUser);

        // Log activity
        userActivityService.logActivity(savedUser, ActivityType.PROFILE_UPDATE,
                "User profile updated", performedBy, null);

        log.info("Successfully updated user: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Find user by ID
     *
     * @param userId User ID
     * @return Optional user
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Find user by Keycloak ID
     *
     * @param keycloakId Keycloak user ID
     * @return Optional user
     */
    @Transactional(readOnly = true)
    public Optional<User> findByKeycloakId(UUID keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    /**
     * Find user by email
     *
     * @param email User email
     * @return Optional user
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by employee number
     *
     * @param employeeNumber Employee number
     * @return Optional user
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmployeeNumber(String employeeNumber) {
        return userRepository.findByEmployeeNumber(employeeNumber);
    }

    /**
     * Get all users
     *
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Get all active users
     *
     * @return List of active users
     */
    @Transactional(readOnly = true)
    public List<User> findAllActive() {
        return userRepository.findAllActiveAndNotDeleted();
    }

    /**
     * Search users
     *
     * @param searchTerm Search term
     * @return List of matching users
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }

    /**
     * Find users by department
     *
     * @param department Department name
     * @return List of users in department
     */
    @Transactional(readOnly = true)
    public List<User> findByDepartment(String department) {
        return userRepository.findActiveByDepartment(department);
    }

    /**
     * Record user login
     *
     * @param userId User ID
     * @param ipAddress IP address
     * @param userAgent User agent
     */
    @Transactional
    public void recordLogin(Long userId, String ipAddress, String userAgent) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.recordLogin();
        userRepository.save(user);

        // Log activity
        UserActivityLog activityLog = UserActivityLog.builder()
                .user(user)
                .activityType(ActivityType.LOGIN)
                .description("User logged in")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .performedAt(LocalDateTime.now())
                .performedBy(user.getEmail())
                .build();
        userActivityLogRepository.save(activityLog);

        log.info("Recorded login for user: {}", user.getEmail());
    }

    /**
     * Deactivate user
     *
     * @param userId User ID
     * @param performedBy Who performed this action
     */
    @Transactional
    public void deactivateUser(Long userId, String performedBy) {
        log.info("Deactivating user: {}", userId);

        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.deactivate();
        keycloakUserManagementService.disableUserInKeycloak(user.getKeycloakId());
        userRepository.save(user);

        // Log activity
        userActivityService.logActivity(user, ActivityType.STATUS_CHANGE,
                "User deactivated", performedBy, null);

        log.info("Successfully deactivated user: {}", user.getEmail());
    }

    /**
     * Reactivate user
     *
     * @param userId User ID
     * @param performedBy Who performed this action
     */
    @Transactional
    public void reactivateUser(Long userId, String performedBy) {
        log.info("Reactivating user: {}", userId);

        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.reactivate();
        keycloakUserManagementService.enableUserInKeycloak(user.getKeycloakId());
        userRepository.save(user);

        // Log activity
        userActivityService.logActivity(user, ActivityType.STATUS_CHANGE,
                "User reactivated", performedBy, null);

        log.info("Successfully reactivated user: {}", user.getEmail());
    }

    /**
     * Soft delete user
     *
     * @param userId User ID
     * @param performedBy Who performed this action
     */
    @Transactional
    public void deleteUser(Long userId, String performedBy) {
        log.info("Deleting user: {}", userId);

        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsDeleted(true);
        user.deactivate();
        keycloakUserManagementService.disableUserInKeycloak(user.getKeycloakId());
        userRepository.save(user);

        // Log activity
        userActivityService.logActivity(user, ActivityType.USER_DELETED,
                "User deleted", performedBy, null);

        log.info("Successfully deleted user: {}", user.getEmail());
    }

    /**
     * Reset user password
     *
     * @param userId User ID
     * @param newPassword New password
     * @param temporary Whether password is temporary
     * @param performedBy Who performed this action
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword, boolean temporary, String performedBy) {
        log.info("Resetting password for user: {}", userId);

        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        keycloakUserManagementService.setUserPassword(user.getKeycloakId(), newPassword, temporary);

        // Log activity
        userActivityService.logActivity(user, ActivityType.PASSWORD_CHANGE,
                "Password reset", performedBy, null);

        log.info("Successfully reset password for user: {}", user.getEmail());
    }

    /**
     * Count total users
     *
     * @return Total user count
     */
    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    /**
     * Count active users
     *
     * @return Active user count
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return userRepository.findAllActiveAndNotDeleted().size();
    }

    /**
     * Find user by Keycloak username
     *
     * @param keycloakUsername Keycloak username
     * @return Optional user
     */
    @Transactional(readOnly = true)
    public Optional<User> findByKeycloakUsername(String keycloakUsername) {
        return userRepository.findByKeycloakUsername(keycloakUsername);
    }

    /**
     * Get user's roles as string list
     *
     * @param userId User ID
     * @return List of role names
     */
    @Transactional(readOnly = true)
    public List<String> getUserRoles(Long userId) {
        return userRoleRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(userRole -> userRole.getRoleName().name())
                .toList();
    }
}
