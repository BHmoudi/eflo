package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowInstance;
import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * WorkflowInstanceRepository
 *
 * Repository for managing workflow instances with extensive query methods.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    /**
     * Find instance by order ID
     */
    List<WorkflowInstance> findByOrderId(Long orderId);

    /**
     * Find all instances for an order
     */
    List<WorkflowInstance> findAllByOrderId(Long orderId);

    /**
     * Find instances by process ID
     */
    List<WorkflowInstance> findByProcessId(Long processId);

    /**
     * Find instances by status
     */
    List<WorkflowInstance> findByInstanceStatus(InstanceStatus status);

    /**
     * Find instances by priority
     */
    List<WorkflowInstance> findByPriority(Priority priority);

    /**
     * Find active instances (running or paused)
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.instanceStatus IN ('RUNNING', 'PAUSED')")
    List<WorkflowInstance> findActiveInstances();

    /**
     * Find overdue instances
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.isOverdue = true " +
           "AND i.instanceStatus NOT IN ('COMPLETED', 'CANCELLED', 'ERROR')")
    List<WorkflowInstance> findOverdueInstances();

    /**
     * Find instances exceeding expected completion date
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.expectedCompletionDate < :now " +
           "AND i.instanceStatus IN ('RUNNING', 'PAUSED')")
    List<WorkflowInstance> findInstancesExceedingDeadline(@Param("now") LocalDateTime now);

    /**
     * Find instances by current state
     */
    List<WorkflowInstance> findByCurrentStateId(Long stateId);

    /**
     * Find instances in state for process
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.process.id = :processId " +
           "AND i.currentState.id = :stateId")
    List<WorkflowInstance> findByProcessIdAndCurrentStateId(@Param("processId") Long processId,
                                                             @Param("stateId") Long stateId);

    /**
     * Find instances created in date range
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<WorkflowInstance> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Count instances by status
     */
    long countByInstanceStatus(InstanceStatus status);

    /**
     * Count overdue instances
     */
    @Query("SELECT COUNT(i) FROM WorkflowInstance i " +
           "WHERE i.isOverdue = true " +
           "AND i.instanceStatus NOT IN ('COMPLETED', 'CANCELLED', 'ERROR')")
    long countOverdueInstances();

    /**
     * Count active instances
     */
    @Query("SELECT COUNT(i) FROM WorkflowInstance i " +
           "WHERE i.instanceStatus IN ('RUNNING', 'PAUSED')")
    long countActiveInstances();

    /**
     * Count instances by process
     */
    long countByProcessId(Long processId);

    /**
     * Find high priority running instances
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.instanceStatus = 'RUNNING' " +
           "AND i.priority IN ('HIGH', 'URGENT') " +
           "ORDER BY i.priority DESC, i.createdAt ASC")
    List<WorkflowInstance> findHighPriorityRunningInstances();

    /**
     * Find instances approaching deadline
     */
    @Query("SELECT i FROM WorkflowInstance i " +
           "WHERE i.expectedCompletionDate BETWEEN :now AND :threshold " +
           "AND i.instanceStatus = 'RUNNING' " +
           "AND i.isOverdue = false")
    List<WorkflowInstance> findInstancesApproachingDeadline(@Param("now") LocalDateTime now,
                                                            @Param("threshold") LocalDateTime threshold);

    /**
     * Find instances by process and status with pagination
     */
    Page<WorkflowInstance> findByProcessIdAndInstanceStatus(Long processId,
                                                            InstanceStatus status,
                                                            Pageable pageable);

    /**
     * Get instance statistics
     */
    @Query("SELECT i.instanceStatus as status, COUNT(i) as count " +
           "FROM WorkflowInstance i " +
           "GROUP BY i.instanceStatus")
    List<Object[]> getInstanceStatusStatistics();

    /**
     * Get instance statistics by process
     */
    @Query("SELECT i.process.processCode as processCode, i.instanceStatus as status, COUNT(i) as count " +
           "FROM WorkflowInstance i " +
           "WHERE i.process.id = :processId " +
           "GROUP BY i.process.processCode, i.instanceStatus")
    List<Object[]> getInstanceStatisticsByProcess(@Param("processId") Long processId);
}
