package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskApproval;
import com.eflo.workflow.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowTaskApprovalRepository
 *
 * Repository for managing workflow task approvals.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskApprovalRepository extends JpaRepository<WorkflowTaskApproval, Long> {

    /**
     * Find all approvals by instance task ID
     */
    @Query("SELECT a FROM WorkflowTaskApproval a WHERE a.instanceTask.id = :instanceTaskId")
    List<WorkflowTaskApproval> findByInstanceTaskId(@Param("instanceTaskId") Long instanceTaskId);

    /**
     * Find approvals by approver user ID and approval status
     */
    List<WorkflowTaskApproval> findByApproverUserIdAndApprovalStatus(Long approverUserId, ApprovalStatus approvalStatus);

    /**
     * Find pending approvals by user
     */
    @Query("SELECT a FROM WorkflowTaskApproval a " +
           "WHERE a.approverUserId = :userId " +
           "AND a.approvalStatus = 'PENDING' " +
           "ORDER BY a.createdAt ASC")
    List<WorkflowTaskApproval> findPendingApprovalsByUser(@Param("userId") Long userId);

    /**
     * Find approval by instance task ID and level order
     */
    @Query("SELECT a FROM WorkflowTaskApproval a WHERE a.instanceTask.id = :instanceTaskId AND a.levelOrder = :levelOrder")
    Optional<WorkflowTaskApproval> findByInstanceTaskIdAndLevelOrder(@Param("instanceTaskId") Long instanceTaskId, @Param("levelOrder") Integer levelOrder);
}
