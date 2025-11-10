package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WorkflowTaskDelegationRepository
 *
 * Repository for managing workflow task delegations.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskDelegationRepository extends JpaRepository<WorkflowTaskDelegation, Long> {

    /**
     * Find delegations by delegated to user ID
     */
    List<WorkflowTaskDelegation> findByDelegatedToUserId(Long delegatedToUserId);

    /**
     * Find delegations by delegated from user ID
     */
    List<WorkflowTaskDelegation> findByDelegatedFromUserId(Long delegatedFromUserId);

    /**
     * Find delegations by instance task ID
     */
    @Query("SELECT d FROM WorkflowTaskDelegation d WHERE d.instanceTask.id = :instanceTaskId")
    List<WorkflowTaskDelegation> findByInstanceTaskId(@Param("instanceTaskId") Long instanceTaskId);

    /**
     * Find pending delegations by user
     */
    @Query("SELECT d FROM WorkflowTaskDelegation d " +
           "WHERE d.delegatedToUserId = :userId " +
           "AND d.delegationStatus = 'PENDING'")
    List<WorkflowTaskDelegation> findPendingDelegationsByUser(@Param("userId") Long userId);
}
