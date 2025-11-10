package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowTaskMetricsRepository
 *
 * Repository for managing workflow task metrics.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskMetricsRepository extends JpaRepository<WorkflowTaskMetrics, Long> {

    /**
     * Find metrics by instance task ID
     */
    @Query("SELECT m FROM WorkflowTaskMetrics m WHERE m.instanceTask.id = :instanceTaskId")
    Optional<WorkflowTaskMetrics> findByInstanceTaskId(@Param("instanceTaskId") Long instanceTaskId);

    /**
     * Find metrics where SLA is not met
     */
    List<WorkflowTaskMetrics> findBySlaMetFalse();

    /**
     * Calculate average execution time across all tasks
     */
    @Query("SELECT AVG(m.executionDurationSeconds) FROM WorkflowTaskMetrics m " +
           "WHERE m.executionDurationSeconds IS NOT NULL")
    Double calculateAverageExecutionTime();
}
