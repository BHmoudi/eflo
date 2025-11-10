package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkflowTaskHistoryRepository
 *
 * Repository for managing workflow task history.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskHistoryRepository extends JpaRepository<WorkflowTaskHistory, Long> {

    /**
     * Find history by instance task ID ordered by creation date descending
     */
    @Query("SELECT h FROM WorkflowTaskHistory h WHERE h.instanceTask.id = :instanceTaskId ORDER BY h.createdAt DESC")
    List<WorkflowTaskHistory> findByInstanceTaskIdOrderByCreatedAtDesc(@Param("instanceTaskId") Long instanceTaskId);

    /**
     * Find history by action performed by user ID
     */
    List<WorkflowTaskHistory> findByActionByUserId(Long actionByUserId);

    /**
     * Find history by action type
     */
    List<WorkflowTaskHistory> findByActionType(String actionType);

    /**
     * Find recent history with pagination
     */
    @Query("SELECT h FROM WorkflowTaskHistory h " +
           "ORDER BY h.createdAt DESC")
    Page<WorkflowTaskHistory> findRecentHistory(Pageable pageable);
}
