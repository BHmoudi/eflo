package com.eflo.workflow.scheduler;

import com.eflo.workflow.service.DeadlineMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Deadline Monitoring Scheduler
 *
 * Periodically checks for overdue instances and tasks.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineMonitoringScheduler {

    private final DeadlineMonitoringService deadlineMonitoringService;

    /**
     * Check for overdue instances
     * Runs every 30 minutes
     */
    @Scheduled(fixedDelayString = "${eflo.workflow.scheduling.deadline-check-interval:1800000}")
    public void checkOverdueInstances() {
        log.debug("Running scheduled deadline check for instances");
        try {
            deadlineMonitoringService.checkOverdueInstances();
        } catch (Exception e) {
            log.error("Error during overdue instance check", e);
        }
    }

    /**
     * Check for overdue tasks
     * Runs every 30 minutes
     */
    @Scheduled(fixedDelayString = "${eflo.workflow.scheduling.deadline-check-interval:1800000}")
    public void checkOverdueTasks() {
        log.debug("Running scheduled deadline check for tasks");
        try {
            deadlineMonitoringService.checkOverdueTasks();
        } catch (Exception e) {
            log.error("Error during overdue task check", e);
        }
    }
}
