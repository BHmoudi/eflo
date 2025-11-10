package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowHistory;
import com.eflo.workflow.domain.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WorkflowHistoryRepository
 *
 * Repository for managing workflow history and audit trail.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {

    /**
     * Find history by instance ID ordered by creation time
     */
    List<WorkflowHistory> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);

    /**
     * Find history by instance ID with pagination
     */
    Page<WorkflowHistory> findByInstanceId(Long instanceId, Pageable pageable);

    /**
     * Find history by event type
     */
    List<WorkflowHistory> findByEventType(EventType eventType);

    /**
     * Find history by instance and event type
     */
    List<WorkflowHistory> findByInstanceIdAndEventTypeOrderByCreatedAtDesc(Long instanceId,
                                                                           EventType eventType);

    /**
     * Find history by user
     */
    List<WorkflowHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find history by user with pagination
     */
    Page<WorkflowHistory> findByUserId(Long userId, Pageable pageable);

    /**
     * Find history in date range
     */
    @Query("SELECT h FROM WorkflowHistory h " +
           "WHERE h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<WorkflowHistory> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find history for instance in date range
     */
    @Query("SELECT h FROM WorkflowHistory h " +
           "WHERE h.instance.id = :instanceId " +
           "AND h.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.createdAt DESC")
    List<WorkflowHistory> findByInstanceIdAndDateRange(@Param("instanceId") Long instanceId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find state change history for instance
     */
    @Query("SELECT h FROM WorkflowHistory h " +
           "WHERE h.instance.id = :instanceId " +
           "AND h.eventType = 'STATE_CHANGED' " +
           "ORDER BY h.createdAt ASC")
    List<WorkflowHistory> findStateChangeHistory(@Param("instanceId") Long instanceId);

    /**
     * Find task history for instance
     */
    @Query("SELECT h FROM WorkflowHistory h " +
           "WHERE h.instance.id = :instanceId " +
           "AND h.eventType IN ('TASK_CREATED', 'TASK_ASSIGNED', 'TASK_STARTED', 'TASK_COMPLETED', 'TASK_FAILED') " +
           "ORDER BY h.createdAt DESC")
    List<WorkflowHistory> findTaskHistory(@Param("instanceId") Long instanceId);

    /**
     * Find history by task
     */
    List<WorkflowHistory> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    /**
     * Count events by type for instance
     */
    long countByInstanceIdAndEventType(Long instanceId, EventType eventType);

    /**
     * Find recent history entries
     */
    @Query("SELECT h FROM WorkflowHistory h " +
           "WHERE h.createdAt >= :since " +
           "ORDER BY h.createdAt DESC")
    List<WorkflowHistory> findRecentHistory(@Param("since") LocalDateTime since);

    /**
     * Find escalation events
     */
    @Query("SELECT h FROM WorkflowHistory h " +
           "WHERE h.eventType = 'ESCALATION_TRIGGERED' " +
           "ORDER BY h.createdAt DESC")
    List<WorkflowHistory> findEscalationEvents();
}
