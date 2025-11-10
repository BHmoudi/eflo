package com.eflo.user.service;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserActivityLog;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.repository.UserActivityLogRepository;
import com.eflo.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user activity logs
 *
 * Provides methods to log and query user activities.
 */
@Slf4j
@Service
public class UserActivityService {

    private final UserActivityLogRepository userActivityLogRepository;
    private final UserRepository userRepository;

    public UserActivityService(UserActivityLogRepository userActivityLogRepository,
                               UserRepository userRepository) {
        this.userActivityLogRepository = userActivityLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Log a user activity
     *
     * @param user User entity
     * @param activityType Type of activity
     * @param description Activity description
     * @param performedBy Who performed the activity
     * @param businessUnitId Related business unit ID (optional)
     */
    @Transactional
    public void logActivity(User user, ActivityType activityType, String description,
                            String performedBy, Long businessUnitId) {
        UserActivityLog activityLog = UserActivityLog.builder()
                .user(user)
                .activityType(activityType)
                .description(description)
                .performedAt(LocalDateTime.now())
                .performedBy(performedBy)
                .businessUnitId(businessUnitId)
                .build();

        userActivityLogRepository.save(activityLog);
        log.debug("Logged activity for user {}: {}", user.getEmail(), activityType);
    }

    /**
     * Log a user activity with IP and user agent
     *
     * @param user User entity
     * @param activityType Type of activity
     * @param description Activity description
     * @param performedBy Who performed the activity
     * @param ipAddress IP address
     * @param userAgent User agent
     */
    @Transactional
    public void logActivity(User user, ActivityType activityType, String description,
                            String performedBy, String ipAddress, String userAgent) {
        UserActivityLog activityLog = UserActivityLog.builder()
                .user(user)
                .activityType(activityType)
                .description(description)
                .performedAt(LocalDateTime.now())
                .performedBy(performedBy)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        userActivityLogRepository.save(activityLog);
        log.debug("Logged activity for user {}: {}", user.getEmail(), activityType);
    }

    /**
     * Log a user activity with metadata
     *
     * @param user User entity
     * @param activityType Type of activity
     * @param description Activity description
     * @param performedBy Who performed the activity
     * @param businessUnitId Related business unit ID (optional)
     * @param metadata Additional metadata in JSON format
     */
    @Transactional
    public void logActivityWithMetadata(User user, ActivityType activityType, String description,
                                        String performedBy, Long businessUnitId, String metadata) {
        UserActivityLog activityLog = UserActivityLog.builder()
                .user(user)
                .activityType(activityType)
                .description(description)
                .performedAt(LocalDateTime.now())
                .performedBy(performedBy)
                .businessUnitId(businessUnitId)
                .metadata(metadata)
                .build();

        userActivityLogRepository.save(activityLog);
        log.debug("Logged activity for user {}: {}", user.getEmail(), activityType);
    }

    /**
     * Get all activities for a user
     *
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getUserActivities(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userActivityLogRepository.findByUserOrderByPerformedAtDesc(user, pageable);
    }

    /**
     * Get activities by type for a user
     *
     * @param userId User ID
     * @param activityType Activity type
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getUserActivitiesByType(Long userId, ActivityType activityType,
                                                          Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userActivityLogRepository.findByUserAndActivityTypeOrderByPerformedAtDesc(
                user, activityType, pageable);
    }

    /**
     * Get recent activities for a user
     *
     * @param userId User ID
     * @param limit Number of recent activities to return
     * @return List of recent activity logs
     */
    @Transactional(readOnly = true)
    public List<UserActivityLog> getRecentActivities(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Pageable pageable = PageRequest.of(0, limit);
        return userActivityLogRepository.findByUserOrderByPerformedAtDesc(user, pageable).getContent();
    }

    /**
     * Get activities within a date range for a user
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getUserActivitiesByDateRange(Long userId, LocalDateTime startDate,
                                                               LocalDateTime endDate, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userActivityLogRepository.findByUserAndPerformedAtBetweenOrderByPerformedAtDesc(
                user, startDate, endDate, pageable);
    }

    /**
     * Get all login activities for a user
     *
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of login activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getLoginActivities(Long userId, Pageable pageable) {
        return getUserActivitiesByType(userId, ActivityType.LOGIN, pageable);
    }

    /**
     * Get activities by business unit
     *
     * @param businessUnitId Business unit ID
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getActivitiesByBusinessUnit(Long businessUnitId, Pageable pageable) {
        return userActivityLogRepository.findByBusinessUnitIdOrderByPerformedAtDesc(businessUnitId, pageable);
    }

    /**
     * Get all activities performed by a specific user
     *
     * @param performedBy User who performed the activities
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getActivitiesByPerformer(String performedBy, Pageable pageable) {
        return userActivityLogRepository.findByPerformedByOrderByPerformedAtDesc(performedBy, pageable);
    }

    /**
     * Get all activities of a specific type
     *
     * @param activityType Activity type
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<UserActivityLog> getActivitiesByType(ActivityType activityType, Pageable pageable) {
        return userActivityLogRepository.findByActivityTypeOrderByPerformedAtDesc(activityType, pageable);
    }

    /**
     * Get activity count for a user
     *
     * @param userId User ID
     * @return Total activity count
     */
    @Transactional(readOnly = true)
    public long getUserActivityCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userActivityLogRepository.countByUser(user);
    }

    /**
     * Get activity count by type for a user
     *
     * @param userId User ID
     * @param activityType Activity type
     * @return Activity count
     */
    @Transactional(readOnly = true)
    public long getUserActivityCountByType(Long userId, ActivityType activityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userActivityLogRepository.countByUserAndActivityType(user, activityType);
    }
}
