package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowApprovalChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowApprovalChainRepository
 *
 * Repository for managing workflow approval chains.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowApprovalChainRepository extends JpaRepository<WorkflowApprovalChain, Long> {

    /**
     * Find approval chain by chain code
     */
    Optional<WorkflowApprovalChain> findByChainCode(String chainCode);

    /**
     * Find all active approval chains
     */
    List<WorkflowApprovalChain> findByIsActiveTrue();

    /**
     * Check if approval chain exists by chain code
     */
    boolean existsByChainCode(String chainCode);
}
