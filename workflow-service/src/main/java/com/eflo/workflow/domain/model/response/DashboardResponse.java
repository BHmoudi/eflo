package com.eflo.workflow.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for workflow dashboard statistics
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private InstanceStatistics instanceStatistics;
    private TaskStatistics taskStatistics;
    private Map<String, Long> processCounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstanceStatistics {
        private Long totalInstances;
        private Long runningInstances;
        private Long pausedInstances;
        private Long completedInstances;
        private Long cancelledInstances;
        private Long errorInstances;
        private Long overdueInstances;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatistics {
        private Long totalTasks;
        private Long pendingTasks;
        private Long assignedTasks;
        private Long inProgressTasks;
        private Long completedTasks;
        private Long overdueTasks;
        private Long escalatedTasks;
    }
}
