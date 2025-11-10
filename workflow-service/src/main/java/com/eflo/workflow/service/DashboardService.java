package com.eflo.workflow.service;

import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.enums.TaskStatus;
import com.eflo.workflow.domain.model.response.DashboardResponse;
import com.eflo.workflow.domain.repository.WorkflowInstanceRepository;
import com.eflo.workflow.domain.repository.WorkflowInstanceTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard Service
 *
 * Provides statistical data for workflow dashboards and monitoring.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowInstanceTaskRepository taskRepository;

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStatistics() {
        log.debug("Generating dashboard statistics");

        // Instance statistics
        DashboardResponse.InstanceStatistics instanceStats = DashboardResponse.InstanceStatistics.builder()
                .totalInstances(instanceRepository.count())
                .runningInstances(instanceRepository.countByInstanceStatus(InstanceStatus.RUNNING))
                .pausedInstances(instanceRepository.countByInstanceStatus(InstanceStatus.PAUSED))
                .completedInstances(instanceRepository.countByInstanceStatus(InstanceStatus.COMPLETED))
                .cancelledInstances(instanceRepository.countByInstanceStatus(InstanceStatus.CANCELLED))
                .errorInstances(instanceRepository.countByInstanceStatus(InstanceStatus.ERROR))
                .overdueInstances(instanceRepository.countOverdueInstances())
                .build();

        // Task statistics
        DashboardResponse.TaskStatistics taskStats = DashboardResponse.TaskStatistics.builder()
                .totalTasks(taskRepository.count())
                .pendingTasks(taskRepository.countByTaskStatus(TaskStatus.PENDING))
                .assignedTasks(taskRepository.countByTaskStatus(TaskStatus.ASSIGNED))
                .inProgressTasks(taskRepository.countByTaskStatus(TaskStatus.IN_PROGRESS))
                .completedTasks(taskRepository.countByTaskStatus(TaskStatus.COMPLETED))
                .overdueTasks(taskRepository.countOverdueTasks())
                .escalatedTasks((long) taskRepository.findEscalatedTasks().size())
                .build();

        // Process counts (simplified)
        Map<String, Long> processCounts = new HashMap<>();

        return DashboardResponse.builder()
                .instanceStatistics(instanceStats)
                .taskStatistics(taskStats)
                .processCounts(processCounts)
                .build();
    }
}
