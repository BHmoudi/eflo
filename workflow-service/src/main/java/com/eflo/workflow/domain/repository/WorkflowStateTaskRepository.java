package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowStateTask;
import com.eflo.workflow.domain.enums.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowStateTaskRepository
 *
 * Repository for managing workflow state task templates.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowStateTaskRepository extends JpaRepository<WorkflowStateTask, Long> {

    /**
     * Find tasks by state ID ordered by task order
     */
    @Query("SELECT st FROM WorkflowStateTask st WHERE st.state.id = :stateId ORDER BY st.taskOrder ASC")
    List<WorkflowStateTask> findByStateIdOrderByTaskOrderAsc(@Param("stateId") Long stateId);

    /**
     * Find task by state ID and task code
     */
    @Query("SELECT st FROM WorkflowStateTask st WHERE st.state.id = :stateId AND st.taskCode = :taskCode")
    Optional<WorkflowStateTask> findByStateIdAndTaskCode(@Param("stateId") Long stateId, @Param("taskCode") String taskCode);

    /**
     * Find mandatory tasks by state ID
     */
    @Query("SELECT st FROM WorkflowStateTask st WHERE st.state.id = :stateId AND st.isMandatory = true")
    List<WorkflowStateTask> findByStateIdAndIsMandatoryTrue(@Param("stateId") Long stateId);

    /**
     * Find tasks by type
     */
    List<WorkflowStateTask> findByTaskType(TaskType taskType);

    /**
     * Find tasks assigned to a role
     */
    List<WorkflowStateTask> findByAssignedToRole(String role);

    /**
     * Find tasks assigned to a user
     */
    List<WorkflowStateTask> findByAssignedToUserId(Long userId);

    /**
     * Find tasks requiring approval
     */
    @Query("SELECT st FROM WorkflowStateTask st WHERE st.state.id = :stateId AND st.requiresApproval = true")
    List<WorkflowStateTask> findByStateIdAndRequiresApprovalTrue(@Param("stateId") Long stateId);

    /**
     * Find auto-assign tasks for a state
     */
    @Query("SELECT st FROM WorkflowStateTask st WHERE st.state.id = :stateId AND st.autoAssign = true")
    List<WorkflowStateTask> findByStateIdAndAutoAssignTrue(@Param("stateId") Long stateId);

    /**
     * Count mandatory tasks for a state
     */
    @Query("SELECT COUNT(st) FROM WorkflowStateTask st WHERE st.state.id = :stateId AND st.isMandatory = true")
    long countByStateIdAndIsMandatoryTrue(@Param("stateId") Long stateId);

    /**
     * Check if task code exists for state
     */
    @Query("SELECT CASE WHEN COUNT(st) > 0 THEN true ELSE false END FROM WorkflowStateTask st WHERE st.state.id = :stateId AND st.taskCode = :taskCode")
    boolean existsByStateIdAndTaskCode(@Param("stateId") Long stateId, @Param("taskCode") String taskCode);
}
