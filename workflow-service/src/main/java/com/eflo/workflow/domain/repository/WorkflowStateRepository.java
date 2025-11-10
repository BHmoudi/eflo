package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowState;
import com.eflo.workflow.domain.enums.StateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowStateRepository
 *
 * Repository for managing workflow state definitions.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {

    /**
     * Find states by process ID
     */
    @Query("SELECT s FROM WorkflowState s WHERE s.process.id = :processId ORDER BY s.stateOrder ASC")
    List<WorkflowState> findByProcessIdOrderByStateOrderAsc(@Param("processId") Long processId);

    /**
     * Find state by process ID and state code
     */
    @Query("SELECT s FROM WorkflowState s WHERE s.process.id = :processId AND s.stateCode = :stateCode")
    Optional<WorkflowState> findByProcessIdAndStateCode(@Param("processId") Long processId, @Param("stateCode") String stateCode);

    /**
     * Find states by type
     */
    List<WorkflowState> findByStateType(StateType stateType);

    /**
     * Find start state for a process
     */
    @Query("SELECT s FROM WorkflowState s " +
           "WHERE s.process.id = :processId " +
           "AND s.stateType = 'START'")
    Optional<WorkflowState> findStartStateByProcessId(@Param("processId") Long processId);

    /**
     * Find final states for a process
     */
    @Query("SELECT s FROM WorkflowState s " +
           "WHERE s.process.id = :processId " +
           "AND s.isFinalState = true")
    List<WorkflowState> findFinalStatesByProcessId(@Param("processId") Long processId);

    /**
     * Find states requiring approval
     */
    @Query("SELECT s FROM WorkflowState s WHERE s.process.id = :processId AND s.requiresApproval = true")
    List<WorkflowState> findByProcessIdAndRequiresApprovalTrue(@Param("processId") Long processId);

    /**
     * Check if state code exists for process
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM WorkflowState s WHERE s.process.id = :processId AND s.stateCode = :stateCode")
    boolean existsByProcessIdAndStateCode(@Param("processId") Long processId, @Param("stateCode") String stateCode);
}
