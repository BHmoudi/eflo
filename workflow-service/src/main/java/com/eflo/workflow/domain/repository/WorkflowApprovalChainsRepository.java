package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowApprovalChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowApprovalChainsRepository extends JpaRepository<WorkflowApprovalChain, Long> {
    Optional<WorkflowApprovalChain> findByChainCode(String chainCode);
}
