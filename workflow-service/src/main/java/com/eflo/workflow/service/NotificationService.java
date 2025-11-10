package com.eflo.workflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notification Service
 *
 * Sends notifications for workflow events (email, in-app, etc.).
 * This is a placeholder that can be integrated with actual notification systems.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    // In-memory notification log (for development/testing)
    // In production, this should be persisted to database
    private final Map<String, Map<String, Object>> notificationLog = new ConcurrentHashMap<>();

    /**
     * Send task assignment notification
     */
    public void sendTaskAssignmentNotification(Long userId, String taskName, Long taskId) {
        log.info("Sending task assignment notification to user {} for task: {}", userId, taskName);
        // TODO: Implement actual notification sending (email, push, etc.)
    }

    /**
     * Send overdue notification
     */
    public void sendOverdueNotification(Long userId, String taskName, Long taskId) {
        log.info("Sending overdue notification to user {} for task: {}", userId, taskName);
        // TODO: Implement actual notification sending
    }

    /**
     * Send escalation notification
     */
    public void sendEscalationNotification(Long userId, String instanceName, String reason) {
        log.info("Sending escalation notification to user {} for instance: {}", userId, instanceName);
        // TODO: Implement actual notification sending
    }

    /**
     * Send instance completion notification
     */
    public void sendCompletionNotification(Long userId, String instanceName) {
        log.info("Sending completion notification to user {} for instance: {}", userId, instanceName);
        // TODO: Implement actual notification sending
    }

    /**
     * Log notification status (called by n8n webhook callback)
     *
     * @param taskId     Task ID
     * @param userId     User ID
     * @param channel    Notification channel (WHATSAPP, EMAIL, SMS)
     * @param status     Delivery status (SENT, FAILED, DELIVERED)
     * @param messageSid External message ID (e.g., Twilio message SID)
     */
    public void logNotificationStatus(String taskId, String userId, String channel,
                                      String status, String messageSid) {
        log.info("Logging notification status - Task: {}, User: {}, Channel: {}, Status: {}, MessageSid: {}",
                taskId, userId, channel, status, messageSid);

        // Create notification log entry
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("taskId", taskId);
        logEntry.put("userId", userId);
        logEntry.put("channel", channel);
        logEntry.put("status", status);
        logEntry.put("messageSid", messageSid);
        logEntry.put("timestamp", LocalDateTime.now().toString());

        // Store in in-memory log (use task+channel as key)
        String logKey = taskId + ":" + channel;
        notificationLog.put(logKey, logEntry);

        // TODO: In production, persist to database table (workflow_notifications)
        // Example:
        // WorkflowNotification notification = new WorkflowNotification();
        // notification.setTaskId(Long.parseLong(taskId));
        // notification.setUserId(Long.parseLong(userId));
        // notification.setChannel(NotificationChannel.valueOf(channel));
        // notification.setStatus(NotificationStatus.valueOf(status));
        // notification.setExternalMessageId(messageSid);
        // notification.setSentAt(LocalDateTime.now());
        // notificationRepository.save(notification);

        log.debug("Notification log entry created: {}", logEntry);
    }

    /**
     * Get notification log for task (for testing/debugging)
     */
    public Map<String, Map<String, Object>> getNotificationLog() {
        return new HashMap<>(notificationLog);
    }

    /**
     * Clear notification log (for testing)
     */
    public void clearNotificationLog() {
        notificationLog.clear();
        log.info("Notification log cleared");
    }
}
