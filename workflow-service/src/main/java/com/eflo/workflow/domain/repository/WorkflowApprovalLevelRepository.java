package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowApprovalLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkflowApprovalLevelRepository
 *
 * Repository for managing workflow approval levels.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowApprovalLevelRepository extends JpaRepository<WorkflowApprovalLevel, Long> {

    /**
     * Find all approval levels by chain ID
     */
    @Query("SELECT al FROM WorkflowApprovalLevel al WHERE al.chain.id = :chainId")
    List<WorkflowApprovalLevel> findByChainId(@Param("chainId") Long chainId);

    /**
     * Find all approval levels by chain ID ordered by level order
     */
    @Query("SELECT al FROM WorkflowApprovalLevel al WHERE al.chain.id = :chainId ORDER BY al.levelOrder ASC")
    List<WorkflowApprovalLevel> findByChainIdOrderByLevelOrderAsc(@Param("chainId") Long chainId);

    /**
     * Find approval levels by required role ID
     */
    @Query("SELECT al FROM WorkflowApprovalLevel al WHERE al.requiredRole.id = :requiredRoleId")
    List<WorkflowApprovalLevel> findByRequiredRoleId(@Param("requiredRoleId") Long requiredRoleId);
}
