package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowNotification;
import com.eflo.workflow.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WorkflowNotificationRepository
 *
 * Repository for managing workflow notifications.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowNotificationRepository extends JpaRepository<WorkflowNotification, Long> {

    /**
     * Find notifications by recipient user ID and status
     */
    List<WorkflowNotification> findByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status);

    /**
     * Find pending notifications scheduled for now or earlier
     */
    @Query("SELECT n FROM WorkflowNotification n " +
           "WHERE n.status = 'PENDING' " +
           "AND n.scheduledFor <= :now")
    List<WorkflowNotification> findPendingNotifications(@Param("now") LocalDateTime now);

    /**
     * Find unread in-app notifications for a user
     */
    @Query("SELECT n FROM WorkflowNotification n " +
           "WHERE n.recipientUserId = :userId " +
           "AND n.status = 'SENT' " +
           "AND n.readAt IS NULL " +
           "AND n.notificationChannel = 'IN_APP' " +
           "ORDER BY n.createdAt DESC")
    List<WorkflowNotification> findUnreadInAppNotifications(@Param("userId") Long userId);

    /**
     * Count unread notifications by recipient user ID
     */
    @Query("SELECT COUNT(n) FROM WorkflowNotification n " +
           "WHERE n.recipientUserId = :userId " +
           "AND n.status = 'SENT' " +
           "AND n.readAt IS NULL")
    long countUnreadByRecipientUserId(@Param("userId") Long userId);
}
