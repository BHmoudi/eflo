package com.eflo.user.domain.repository;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserActivityLog;
import com.eflo.user.domain.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    Page<UserActivityLog> findByUserId(Long userId, Pageable pageable);

    Page<UserActivityLog> findByUser(User user, Pageable pageable);

    List<UserActivityLog> findByActivityType(ActivityType activityType);

    Page<UserActivityLog> findByActivityType(ActivityType activityType, Pageable pageable);

    Page<UserActivityLog> findByActivityTypeOrderByPerformedAtDesc(ActivityType activityType, Pageable pageable);

    Page<UserActivityLog> findByUserAndActivityTypeOrderByPerformedAtDesc(User user, ActivityType activityType, Pageable pageable);

    // Use Pageable instead of Top N - more flexible
    Page<UserActivityLog> findByUserOrderByPerformedAtDesc(User user, Pageable pageable);

    Page<UserActivityLog> findByUserAndPerformedAtBetweenOrderByPerformedAtDesc(User user, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<UserActivityLog> findByPerformedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<UserActivityLog> findByPerformedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Method removed: findByAction - entity has activityType, not action
    // Use findByActivityType instead

    // Method removed: findByCreatedAtBetween - should use performedAt for querying activity logs by date
    // Use findByPerformedAtBetween instead

    // Method removed: findByEntityTypeAndEntityId - entity doesn't have entityType or entityId properties
    // If needed, this data should be stored in the metadata JSONB field

    Page<UserActivityLog> findByBusinessUnitIdOrderByPerformedAtDesc(Long businessUnitId, Pageable pageable);

    Page<UserActivityLog> findByPerformedByOrderByPerformedAtDesc(String performedBy, Pageable pageable);

    long countByUserId(long userId);

    long countByUser(User user);

    // Method removed: countByAction - entity has activityType, not action
    // Use countByActivityType instead (if needed)

    long countByUserAndActivityType(User user, ActivityType activityType);

    @Query("SELECT ual FROM UserActivityLog ual WHERE " +
           "ual.user.id = :userId AND ual.activityType = :activityType")
    Page<UserActivityLog> findByUserIdAndActivityType(
            @Param("userId") Long userId,
            @Param("activityType") ActivityType activityType,
            Pageable pageable
    );

    @Query("SELECT ual FROM UserActivityLog ual WHERE " +
           "ual.user.id = :userId AND ual.performedAt BETWEEN :startDate AND :endDate")
    Page<UserActivityLog> findByUserIdAndPerformedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.businessUnitId = :businessUnitId")
    Page<UserActivityLog> findByBusinessUnitId(
            @Param("businessUnitId") Long businessUnitId,
            Pageable pageable
    );

    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.performedBy = :performedBy")
    Page<UserActivityLog> findByPerformedBy(
            @Param("performedBy") String performedBy,
            Pageable pageable
    );

    @Query("SELECT ual FROM UserActivityLog ual WHERE " +
           "ual.user.id = :userId AND ual.activityType = :activityType AND " +
           "ual.performedAt BETWEEN :startDate AND :endDate")
    List<UserActivityLog> findByUserIdAndActivityTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("activityType") ActivityType activityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(ual) FROM UserActivityLog ual WHERE " +
           "ual.user.id = :userId AND ual.activityType = :activityType")
    long countByUserIdAndActivityType(
            @Param("userId") Long userId,
            @Param("activityType") ActivityType activityType
    );

    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.user.id = :userId ORDER BY ual.performedAt DESC")
    Page<UserActivityLog> findByUserIdOrderByPerformedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
