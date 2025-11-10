package com.eflo.workflow.scheduler;

import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.repository.WorkflowInstanceTaskRepository;
import com.eflo.workflow.service.NotificationService;
import com.eflo.workflow.service.WorkflowEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Escalation Scheduler
 *
 * Periodically checks for tasks that need escalation based on configured rules.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationScheduler {

    private final WorkflowInstanceTaskRepository taskRepository;
    private final WorkflowEventPublisher eventPublisher;
    private final NotificationService notificationService;

    /**
     * Check and escalate overdue tasks
     * Runs every hour
     */
    @Scheduled(fixedDelayString = "${eflo.workflow.scheduling.escalation-check-interval:3600000}")
    public void checkForEscalation() {
        log.debug("Running scheduled escalation check");
        try {
            // Find tasks that need escalation (overdue by more than 24 hours)
            LocalDateTime escalationThreshold = LocalDateTime.now().minusHours(24);
            List<WorkflowInstanceTask> tasksForEscalation =
                    taskRepository.findTasksForEscalation(escalationThreshold, 3);

            for (WorkflowInstanceTask task : tasksForEscalation) {
                escalateTask(task);
            }

            log.debug("Escalation check completed. Escalated {} tasks", tasksForEscalation.size());
        } catch (Exception e) {
            log.error("Error during escalation check", e);
        }
    }

    /**
     * Escalate a task
     */
    private void escalateTask(WorkflowInstanceTask task) {
        log.info("Escalating task: {} (current level: {})", task.getId(), task.getEscalationLevel());

        // TODO: Implement actual escalation logic (find manager, reassign, etc.)
        // For now, just increment escalation level and send notification
        task.setEscalationLevel(task.getEscalationLevel() + 1);
        task.setEscalatedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Publish escalation event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId());
        eventData.put("taskName", task.getTaskName());
        eventData.put("escalationLevel", task.getEscalationLevel());
        eventData.put("assignedToUserId", task.getAssignedToUserId());

        eventPublisher.publishEscalationEvent(
                EventType.ESCALATION_TRIGGERED,
                task.getInstance().getId(),
                task.getId(),
                "Task escalated to level " + task.getEscalationLevel(),
                eventData
        );

        // Send notification
        if (task.getAssignedToUserId() != null) {
            notificationService.sendEscalationNotification(
                    task.getAssignedToUserId(),
                    task.getTaskName(),
                    "Task is overdue and has been escalated"
            );
        }
    }
}
