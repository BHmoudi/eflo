package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkflowTaskConditionRepository
 *
 * Repository for managing workflow task conditions.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskConditionRepository extends JpaRepository<WorkflowTaskCondition, Long> {

    /**
     * Find conditions by task ID
     */
    @Query("SELECT c FROM WorkflowTaskCondition c WHERE c.task.id = :taskId")
    List<WorkflowTaskCondition> findByTaskId(@Param("taskId") Long taskId);

    /**
     * Find active conditions by task ID
     */
    @Query("SELECT c FROM WorkflowTaskCondition c WHERE c.task.id = :taskId AND c.isActive = true")
    List<WorkflowTaskCondition> findByTaskIdAndIsActiveTrue(@Param("taskId") Long taskId);

    /**
     * Find conditions by condition type
     */
    List<WorkflowTaskCondition> findByConditionType(String conditionType);
}
