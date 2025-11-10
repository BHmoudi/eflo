package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.WorkflowProcess;
import com.eflo.workflow.domain.model.request.CreateProcessRequest;
import com.eflo.workflow.domain.model.request.UpdateProcessRequest;
import com.eflo.workflow.domain.model.response.ProcessResponse;
import com.eflo.workflow.domain.repository.WorkflowProcessRepository;
import com.eflo.workflow.exception.ProcessNotFoundException;
import com.eflo.workflow.mapper.WorkflowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Process Definition Service
 *
 * Manages workflow process definitions (templates).
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService {

    private final WorkflowProcessRepository processRepository;
    private final WorkflowMapper mapper;

    /**
     * Create a new workflow process definition
     */
    @Transactional
    public ProcessResponse createProcess(CreateProcessRequest request) {
        log.info("Creating workflow process: {} for order type: {}", request.getProcessCode(), request.getOrderType());

        // If workflow is active, check if there's already an active workflow for this order type
        if (request.getIsActive() != null && request.getIsActive()) {
            long activeCount = processRepository.countByOrderTypeAndIsActiveTrue(request.getOrderType());
            if (activeCount > 0) {
                throw new IllegalStateException(
                    "An active workflow already exists for order type '" + request.getOrderType() +
                    "'. Please deactivate the existing workflow before activating this one."
                );
            }
        }

        WorkflowProcess process = mapper.toProcess(request);
        WorkflowProcess savedProcess = processRepository.save(process);

        log.info("Workflow process created with ID: {} for order type: {}", savedProcess.getId(), savedProcess.getOrderType());
        return mapper.toProcessResponse(savedProcess);
    }

    /**
     * Get process by ID
     */
    @Transactional(readOnly = true)
    public ProcessResponse getProcessById(Long processId) {
        WorkflowProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        return mapper.toProcessResponse(process);
    }

    /**
     * Get process by code
     */
    @Transactional(readOnly = true)
    public ProcessResponse getProcessByCode(String processCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));
        return mapper.toProcessResponse(process);
    }

    /**
     * Get all active processes
     */
    @Transactional(readOnly = true)
    public List<ProcessResponse> getAllActiveProcesses() {
        List<WorkflowProcess> processes = processRepository.findByIsActiveTrue();
        return mapper.toProcessResponseList(processes);
    }

    /**
     * Get processes by order type
     */
    @Transactional(readOnly = true)
    public List<ProcessResponse> getProcessesByOrderType(String orderType) {
        List<WorkflowProcess> processes = processRepository.findByOrderTypeAndIsActiveTrue(orderType);
        return mapper.toProcessResponseList(processes);
    }

    /**
     * Activate process
     * Ensures only one active workflow per order type
     */
    @Transactional
    public void activateProcess(Long processId) {
        log.info("Activating process: {}", processId);
        WorkflowProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));

        // Check if there's already an active workflow for this order type
        long activeCount = processRepository.countByOrderTypeAndIsActiveTrue(process.getOrderType());
        if (activeCount > 0) {
            // Get the currently active workflow
            List<WorkflowProcess> activeWorkflows = processRepository.findByOrderTypeAndIsActiveTrue(process.getOrderType());
            String activeWorkflowNames = activeWorkflows.stream()
                .map(WorkflowProcess::getProcessName)
                .collect(java.util.stream.Collectors.joining(", "));

            throw new IllegalStateException(
                "Cannot activate workflow '" + process.getProcessName() +
                "'. An active workflow already exists for order type '" + process.getOrderType() +
                "': " + activeWorkflowNames +
                ". Please deactivate the existing workflow first."
            );
        }

        process.setIsActive(true);
        processRepository.save(process);
        log.info("Process activated: {} for order type: {}", process.getProcessName(), process.getOrderType());
    }

    /**
     * Deactivate process
     */
    @Transactional
    public void deactivateProcess(Long processId) {
        log.info("Deactivating process: {}", processId);
        WorkflowProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        process.setIsActive(false);
        processRepository.save(process);
    }

    /**
     * Update process
     */
    @Transactional
    public ProcessResponse updateProcess(String processCode, UpdateProcessRequest request) {
        log.info("Updating workflow process: {}", processCode);

        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        // Update only provided fields
        if (request.getProcessName() != null) {
            process.setProcessName(request.getProcessName());
        }
        if (request.getDescription() != null) {
            process.setDescription(request.getDescription());
        }
        if (request.getOrderType() != null) {
            process.setOrderType(request.getOrderType());
        }
        if (request.getMaxDurationDays() != null) {
            process.setMaxDurationDays(request.getMaxDurationDays());
        }
        if (request.getAutoProgressEnabled() != null) {
            process.setAutoProgressEnabled(request.getAutoProgressEnabled());
        }
        if (request.getParallelExecutionAllowed() != null) {
            process.setParallelExecutionAllowed(request.getParallelExecutionAllowed());
        }
        if (request.getConfiguration() != null) {
            process.setConfiguration(request.getConfiguration());
        }

        WorkflowProcess updatedProcess = processRepository.save(process);
        log.info("Workflow process updated: {}", processCode);

        return mapper.toProcessResponse(updatedProcess);
    }

    /**
     * Delete process by ID
     */
    @Transactional
    public void deleteProcess(Long processId) {
        log.info("Deleting process: {}", processId);
        if (!processRepository.existsById(processId)) {
            throw new ProcessNotFoundException(processId);
        }
        processRepository.deleteById(processId);
    }

    /**
     * Delete process by code
     */
    @Transactional
    public void deleteProcessByCode(String processCode) {
        log.info("Deleting process by code: {}", processCode);
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));
        processRepository.delete(process);
    }

    /**
     * Get process entity (for internal use)
     */
    @Transactional(readOnly = true)
    public WorkflowProcess getProcessEntity(Long processId) {
        return processRepository.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
    }
}
