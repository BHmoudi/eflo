package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskApprovalChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkflowTaskApprovalChainRepository
 *
 * Repository for managing workflow task approval chains.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskApprovalChainRepository extends JpaRepository<WorkflowTaskApprovalChain, Long> {

    /**
     * Find task approval chains by task ID
     */
    @Query("SELECT tac FROM WorkflowTaskApprovalChain tac WHERE tac.task.id = :taskId")
    List<WorkflowTaskApprovalChain> findByTaskId(@Param("taskId") Long taskId);

    /**
     * Find task approval chains by approval chain ID
     */
    @Query("SELECT tac FROM WorkflowTaskApprovalChain tac WHERE tac.approvalChain.id = :approvalChainId")
    List<WorkflowTaskApprovalChain> findByApprovalChainId(@Param("approvalChainId") Long approvalChainId);

    /**
     * Find mandatory approval chains by task ID
     */
    @Query("SELECT tac FROM WorkflowTaskApprovalChain tac " +
           "WHERE tac.task.id = :taskId " +
           "AND tac.isMandatory = true")
    List<WorkflowTaskApprovalChain> findMandatoryByTaskId(@Param("taskId") Long taskId);
}
