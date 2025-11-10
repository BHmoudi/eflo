package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WorkflowInstanceTaskRepository
 *
 * Repository for managing workflow instance tasks with custom queries
 * for task assignment, tracking, and escalation.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowInstanceTaskRepository extends JpaRepository<WorkflowInstanceTask, Long> {

    /**
     * Find tasks by instance ID
     */
    List<WorkflowInstanceTask> findByInstanceId(Long instanceId);

    /**
     * Find tasks by instance ID ordered by ID
     */
    List<WorkflowInstanceTask> findByInstanceIdOrderByIdAsc(Long instanceId);

    /**
     * Find tasks by instance ID and status
     */
    List<WorkflowInstanceTask> findByInstanceIdAndTaskStatus(Long instanceId, TaskStatus status);

    /**
     * Find tasks assigned to user
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.assignedToUserId = :userId " +
           "AND t.taskStatus IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY t.expectedCompletionAt ASC")
    List<WorkflowInstanceTask> findActiveTasksByUserId(@Param("userId") Long userId);

    /**
     * Find tasks assigned to user with pagination
     */
    Page<WorkflowInstanceTask> findByAssignedToUserIdAndTaskStatusIn(Long userId,
                                                                     List<TaskStatus> statuses,
                                                                     Pageable pageable);

    /**
     * Find tasks by role
     */
    List<WorkflowInstanceTask> findByAssignedToRoleAndTaskStatusIn(String role, List<TaskStatus> statuses);

    /**
     * Find overdue tasks
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.isOverdue = true " +
           "AND t.taskStatus NOT IN ('COMPLETED', 'SKIPPED', 'FAILED', 'CANCELLED')")
    List<WorkflowInstanceTask> findOverdueTasks();

    /**
     * Find overdue tasks for user
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.assignedToUserId = :userId " +
           "AND t.isOverdue = true " +
           "AND t.taskStatus NOT IN ('COMPLETED', 'SKIPPED', 'FAILED', 'CANCELLED')")
    List<WorkflowInstanceTask> findOverdueTasksByUserId(@Param("userId") Long userId);

    /**
     * Find tasks exceeding expected completion time
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.expectedCompletionAt < :now " +
           "AND t.taskStatus IN ('ASSIGNED', 'IN_PROGRESS')")
    List<WorkflowInstanceTask> findTasksExceedingDeadline(@Param("now") LocalDateTime now);

    /**
     * Find tasks by status
     */
    List<WorkflowInstanceTask> findByTaskStatus(TaskStatus status);

    /**
     * Find pending tasks for instance
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.instance.id = :instanceId " +
           "AND t.taskStatus IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS')")
    List<WorkflowInstanceTask> findPendingTasksByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * Find completed tasks for instance
     */
    List<WorkflowInstanceTask> findByInstanceIdAndTaskStatus(Long instanceId,
                                                             TaskStatus status,
                                                             Pageable pageable);

    /**
     * Count tasks by status
     */
    long countByTaskStatus(TaskStatus status);

    /**
     * Count tasks for user
     */
    long countByAssignedToUserIdAndTaskStatusIn(Long userId, List<TaskStatus> statuses);

    /**
     * Count overdue tasks
     */
    @Query("SELECT COUNT(t) FROM WorkflowInstanceTask t " +
           "WHERE t.isOverdue = true " +
           "AND t.taskStatus NOT IN ('COMPLETED', 'SKIPPED', 'FAILED', 'CANCELLED')")
    long countOverdueTasks();

    /**
     * Find escalated tasks
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.escalationLevel > 0 " +
           "AND t.taskStatus NOT IN ('COMPLETED', 'SKIPPED', 'FAILED', 'CANCELLED')")
    List<WorkflowInstanceTask> findEscalatedTasks();

    /**
     * Find tasks for escalation
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.expectedCompletionAt < :threshold " +
           "AND t.taskStatus IN ('ASSIGNED', 'IN_PROGRESS') " +
           "AND t.escalationLevel < :maxEscalationLevel")
    List<WorkflowInstanceTask> findTasksForEscalation(@Param("threshold") LocalDateTime threshold,
                                                      @Param("maxEscalationLevel") int maxEscalationLevel);

    /**
     * Find tasks approaching deadline
     */
    @Query("SELECT t FROM WorkflowInstanceTask t " +
           "WHERE t.expectedCompletionAt BETWEEN :now AND :threshold " +
           "AND t.taskStatus IN ('ASSIGNED', 'IN_PROGRESS') " +
           "AND t.isOverdue = false")
    List<WorkflowInstanceTask> findTasksApproachingDeadline(@Param("now") LocalDateTime now,
                                                            @Param("threshold") LocalDateTime threshold);

    /**
     * Get task statistics by status
     */
    @Query("SELECT t.taskStatus as status, COUNT(t) as count " +
           "FROM WorkflowInstanceTask t " +
           "GROUP BY t.taskStatus")
    List<Object[]> getTaskStatistics();

    /**
     * Get task statistics for user
     */
    @Query("SELECT t.taskStatus as status, COUNT(t) as count " +
           "FROM WorkflowInstanceTask t " +
           "WHERE t.assignedToUserId = :userId " +
           "GROUP BY t.taskStatus")
    List<Object[]> getTaskStatisticsByUser(@Param("userId") Long userId);

    /**
     * Find tasks by instance and state task
     */
    List<WorkflowInstanceTask> findByInstanceIdAndStateTaskId(Long instanceId, Long stateTaskId);
}
