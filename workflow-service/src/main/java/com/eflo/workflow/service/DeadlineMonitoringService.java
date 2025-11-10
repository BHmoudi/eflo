package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.WorkflowInstance;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.repository.WorkflowInstanceRepository;
import com.eflo.workflow.domain.repository.WorkflowInstanceTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deadline Monitoring Service
 *
 * Monitors and updates overdue status for instances and tasks.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineMonitoringService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowInstanceTaskRepository taskRepository;
    private final WorkflowEventPublisher eventPublisher;

    /**
     * Check and update overdue instances
     */
    @Transactional
    public void checkOverdueInstances() {
        log.debug("Checking for overdue instances");

        List<WorkflowInstance> instances = instanceRepository
                .findInstancesExceedingDeadline(LocalDateTime.now());

        for (WorkflowInstance instance : instances) {
            if (!instance.getIsOverdue()) {
                instance.setIsOverdue(true);
                instance.setOverdueSince(LocalDateTime.now());
                instanceRepository.save(instance);

                log.warn("Instance {} is now overdue", instance.getId());

                // Publish event
                eventPublisher.publishDeadlineEvent(
                        EventType.INSTANCE_OVERDUE,
                        instance.getId(),
                        instance.getOrderId(),
                        "Instance exceeded expected completion date"
                );
            }
        }

        log.debug("Checked {} overdue instances", instances.size());
    }

    /**
     * Check and update overdue tasks
     */
    @Transactional
    public void checkOverdueTasks() {
        log.debug("Checking for overdue tasks");

        List<WorkflowInstanceTask> tasks = taskRepository
                .findTasksExceedingDeadline(LocalDateTime.now());

        for (WorkflowInstanceTask task : tasks) {
            if (!task.getIsOverdue()) {
                task.setIsOverdue(true);
                task.setOverdueSince(LocalDateTime.now());
                taskRepository.save(task);

                log.warn("Task {} is now overdue", task.getId());

                // Publish event
                eventPublisher.publishDeadlineEvent(
                        EventType.TASK_OVERDUE,
                        task.getInstance().getId(),
                        task.getInstance().getOrderId(),
                        "Task " + task.getTaskName() + " is overdue"
                );
            }
        }

        log.debug("Checked {} overdue tasks", tasks.size());
    }

    /**
     * Get overdue instance count
     */
    @Transactional(readOnly = true)
    public long getOverdueInstanceCount() {
        return instanceRepository.countOverdueInstances();
    }

    /**
     * Get overdue task count
     */
    @Transactional(readOnly = true)
    public long getOverdueTaskCount() {
        return taskRepository.countOverdueTasks();
    }
}
