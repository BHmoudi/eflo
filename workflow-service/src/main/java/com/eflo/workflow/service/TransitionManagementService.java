package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.WorkflowProcess;
import com.eflo.workflow.domain.entity.WorkflowState;
import com.eflo.workflow.domain.entity.WorkflowTransition;
import com.eflo.workflow.domain.repository.WorkflowProcessRepository;
import com.eflo.workflow.domain.repository.WorkflowStateRepository;
import com.eflo.workflow.domain.repository.WorkflowTransitionRepository;
import com.eflo.workflow.exception.ProcessNotFoundException;
import com.eflo.workflow.exception.InvalidWorkflowStateException;
import com.eflo.workflow.web.dto.request.CreateTransitionRequest;
import com.eflo.workflow.web.dto.response.TransitionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transition Management Service
 * Handles workflow transitions between states
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransitionManagementService {

    private final WorkflowProcessRepository processRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowTransitionRepository transitionRepository;

    /**
     * Add a transition to a process
     */
    @Transactional
    public TransitionResponse addTransition(String processCode, CreateTransitionRequest request) {
        log.info("Adding transition from {} to {} in process {}",
                request.getFromStateCode(), request.getToStateCode(), processCode);

        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState fromState = stateRepository.findByProcessIdAndStateCode(process.getId(), request.getFromStateCode())
                .orElseThrow(() -> new InvalidWorkflowStateException("From state not found: " + request.getFromStateCode()));

        WorkflowState toState = stateRepository.findByProcessIdAndStateCode(process.getId(), request.getToStateCode())
                .orElseThrow(() -> new InvalidWorkflowStateException("To state not found: " + request.getToStateCode()));

        WorkflowTransition transition = WorkflowTransition.builder()
                .process(process)
                .fromState(fromState)
                .toState(toState)
                .transitionName(request.getTransitionName())
                .transitionType(request.getTransitionType())
                .requiresApproval(request.getRequiresApproval())
                .autoTransition(request.getAutoTransition())
                .conditionExpression(request.getConditionExpression())
                .configuration(request.getConfiguration())
                .build();

        WorkflowTransition savedTransition = transitionRepository.save(transition);
        return mapToTransitionResponse(savedTransition);
    }

    /**
     * Get all transitions for a process
     */
    @Transactional(readOnly = true)
    public List<TransitionResponse> getProcessTransitions(String processCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        List<WorkflowTransition> transitions = transitionRepository.findByProcessId(process.getId());
        return transitions.stream()
                .map(this::mapToTransitionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a transition
     */
    @Transactional
    public void deleteTransition(String processCode, Long transitionId) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowTransition transition = transitionRepository.findById(transitionId)
                .orElseThrow(() -> new InvalidWorkflowStateException("Transition not found: " + transitionId));

        if (!transition.getProcess().getId().equals(process.getId())) {
            throw new InvalidWorkflowStateException("Transition does not belong to process: " + processCode);
        }

        transitionRepository.delete(transition);
    }

    // Helper method
    private TransitionResponse mapToTransitionResponse(WorkflowTransition transition) {
        return TransitionResponse.builder()
                .id(transition.getId())
                .transitionName(transition.getTransitionName())
                .fromStateCode(transition.getFromState().getStateCode())
                .fromStateName(transition.getFromState().getStateName())
                .toStateCode(transition.getToState().getStateCode())
                .toStateName(transition.getToState().getStateName())
                .transitionType(transition.getTransitionType())
                .requiresApproval(transition.getRequiresApproval())
                .autoTransition(transition.getAutoTransition())
                .conditionExpression(transition.getConditionExpression())
                .requiredRoleCode(null) // TODO: Add required role field to WorkflowTransition entity
                .configuration(transition.getConfiguration())
                .createdAt(transition.getCreatedAt())
                .build();
    }
}
