package com.eflo.workflow.service;

import com.eflo.workflow.domain.WorkflowApprovalChain;
import com.eflo.workflow.domain.WorkflowApprovalLevel;
import com.eflo.workflow.domain.repository.WorkflowApprovalChainRepository;
import com.eflo.workflow.domain.repository.WorkflowApprovalLevelRepository;
import com.eflo.workflow.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ApprovalChainService
 *
 * Manages approval chain configuration including chains and their levels.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalChainService {

    private final WorkflowApprovalChainRepository approvalChainRepository;
    private final WorkflowApprovalLevelRepository approvalLevelRepository;

    /**
     * Get all active approval chains
     *
     * @return List of active approval chains
     */
    public List<WorkflowApprovalChain> getAllActiveChains() {
        log.debug("Retrieving all active approval chains");
        List<WorkflowApprovalChain> chains = approvalChainRepository.findByIsActiveTrue();
        log.debug("Found {} active approval chains", chains.size());
        return chains;
    }

    /**
     * Get approval chain by ID
     *
     * @param id Chain ID
     * @return Optional containing the approval chain if found
     */
    public Optional<WorkflowApprovalChain> getChainById(Long id) {
        log.debug("Retrieving approval chain by ID: {}", id);
        return approvalChainRepository.findById(id);
    }

    /**
     * Get approval chain by code
     *
     * @param code Chain code
     * @return Optional containing the approval chain if found
     */
    public Optional<WorkflowApprovalChain> getChainByCode(String code) {
        log.debug("Retrieving approval chain by code: {}", code);
        return approvalChainRepository.findByChainCode(code);
    }

    /**
     * Create a new approval chain
     *
     * @param chain Approval chain to create
     * @return Created approval chain
     * @throws WorkflowException if chain code already exists
     */
    @Transactional
    public WorkflowApprovalChain createChain(WorkflowApprovalChain chain) {
        log.info("Creating new approval chain with code: {}", chain.getChainCode());

        if (approvalChainRepository.existsByChainCode(chain.getChainCode())) {
            log.error("Approval chain with code {} already exists", chain.getChainCode());
            throw new WorkflowException("Approval chain with code " + chain.getChainCode() + " already exists", "DUPLICATE_CHAIN_CODE");
        }

        WorkflowApprovalChain savedChain = approvalChainRepository.save(chain);
        log.info("Successfully created approval chain with ID: {} and code: {}", savedChain.getId(), savedChain.getChainCode());
        return savedChain;
    }

    /**
     * Update an existing approval chain
     *
     * @param id Chain ID
     * @param updated Updated chain data
     * @return Updated approval chain
     * @throws WorkflowException if chain not found
     */
    @Transactional
    public WorkflowApprovalChain updateChain(Long id, WorkflowApprovalChain updated) {
        log.info("Updating approval chain with ID: {}", id);

        WorkflowApprovalChain existingChain = approvalChainRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Approval chain not found with ID: {}", id);
                    return new WorkflowException("Approval chain not found with ID: " + id, "CHAIN_NOT_FOUND");
                });

        // Update fields
        existingChain.setChainName(updated.getChainName());
        existingChain.setDescription(updated.getDescription());
        existingChain.setIsActive(updated.getIsActive());
        existingChain.setUpdatedBy(updated.getUpdatedBy());

        WorkflowApprovalChain savedChain = approvalChainRepository.save(existingChain);
        log.info("Successfully updated approval chain with ID: {}", id);
        return savedChain;
    }

    /**
     * Add a new level to an approval chain
     *
     * @param chainId Chain ID
     * @param level Approval level to add
     * @return Added approval level
     * @throws WorkflowException if chain not found
     */
    @Transactional
    public WorkflowApprovalLevel addLevel(Long chainId, WorkflowApprovalLevel level) {
        log.info("Adding new level to approval chain ID: {}", chainId);

        WorkflowApprovalChain chain = approvalChainRepository.findById(chainId)
                .orElseThrow(() -> {
                    log.error("Approval chain not found with ID: {}", chainId);
                    return new WorkflowException("Approval chain not found with ID: " + chainId, "CHAIN_NOT_FOUND");
                });

        level.setChain(chain);
        WorkflowApprovalLevel savedLevel = approvalLevelRepository.save(level);

        log.info("Successfully added level with ID: {} to chain ID: {} at order: {}",
                savedLevel.getId(), chainId, savedLevel.getLevelOrder());
        return savedLevel;
    }

    /**
     * Remove a level from an approval chain
     *
     * @param levelId Level ID to remove
     * @throws WorkflowException if level not found
     */
    @Transactional
    public void removeLevel(Long levelId) {
        log.info("Removing approval level with ID: {}", levelId);

        if (!approvalLevelRepository.existsById(levelId)) {
            log.error("Approval level not found with ID: {}", levelId);
            throw new WorkflowException("Approval level not found with ID: " + levelId, "LEVEL_NOT_FOUND");
        }

        approvalLevelRepository.deleteById(levelId);
        log.info("Successfully removed approval level with ID: {}", levelId);
    }

    /**
     * Get all levels for an approval chain ordered by level order
     *
     * @param chainId Chain ID
     * @return List of approval levels ordered by levelOrder
     */
    public List<WorkflowApprovalLevel> getLevels(Long chainId) {
        log.debug("Retrieving approval levels for chain ID: {}", chainId);
        List<WorkflowApprovalLevel> levels = approvalLevelRepository.findByChainIdOrderByLevelOrderAsc(chainId);
        log.debug("Found {} approval levels for chain ID: {}", levels.size(), chainId);
        return levels;
    }

    /**
     * Get chain levels (alias for getLevels)
     *
     * @param chainId Chain ID
     * @return List of approval levels ordered by levelOrder
     */
    public List<WorkflowApprovalLevel> getChainLevels(Long chainId) {
        return getLevels(chainId);
    }
}
