package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.*;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.enums.TaskStatus;
import com.eflo.workflow.domain.model.event.StateChangedEvent;
import com.eflo.workflow.domain.model.request.TransitionRequest;
import com.eflo.workflow.domain.repository.*;
import com.eflo.workflow.exception.InvalidTransitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Transition Service
 *
 * Manages workflow state transitions and validation.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransitionService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final WorkflowInstanceTaskRepository taskRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final WorkflowEventPublisher eventPublisher;
    private final TaskManagementService taskManagementService;

    /**
     * Execute a state transition
     */
    @Transactional
    public void executeTransition(Long instanceId, TransitionRequest request,
                                  Long userId, String userName) {
        log.info("Executing transition for instance: {} to state: {}",
                instanceId, request.getToStateId());

        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Instance not found"));

        WorkflowState currentState = instance.getCurrentState();
        WorkflowState targetState = stateRepository.findById(request.getToStateId())
                .orElseThrow(() -> new RuntimeException("Target state not found"));

        // Validate transition
        if (!request.getForceTransition()) {
            validateTransition(instance, currentState, targetState);
        }

        // Get transition definition
        WorkflowTransition transition = transitionRepository
                .findByFromStateIdAndToStateId(currentState.getId(), targetState.getId())
                .orElse(null);

        // Update instance state
        instance.updateCurrentState(targetState);
        instance.setUpdatedBy(userName);

        // Update context data if provided
        if (request.getContextData() != null) {
            instance.setContextData(request.getContextData());
        }

        // Check if final state
        if (targetState.getIsFinalState()) {
            instance.complete();
        }

        instanceRepository.save(instance);

        // Create history entry
        WorkflowHistory history = WorkflowHistory.forStateChange(
                instance,
                currentState,
                targetState,
                transition,
                userId,
                userName
        );
        historyRepository.save(history);

        // Create tasks for new state
        taskManagementService.createTasksForState(instance, targetState, userName);

        // Publish state changed event
        publishStateChangedEvent(instance, currentState, targetState, transition, userId, userName);

        log.info("Transition executed successfully for instance: {}", instanceId);
    }

    /**
     * Validate if transition is allowed
     */
    private void validateTransition(WorkflowInstance instance,
                                    WorkflowState fromState,
                                    WorkflowState toState) {
        // Check if transition exists
        boolean transitionExists = transitionRepository
                .existsByFromStateIdAndToStateId(fromState.getId(), toState.getId());

        if (!transitionExists) {
            throw new InvalidTransitionException(fromState.getStateName(), toState.getStateName());
        }

        // Check if all mandatory tasks are completed
        List<WorkflowInstanceTask> pendingTasks = taskRepository
                .findByInstanceIdAndTaskStatus(instance.getId(), TaskStatus.PENDING);

        List<WorkflowInstanceTask> assignedTasks = taskRepository
                .findByInstanceIdAndTaskStatus(instance.getId(), TaskStatus.ASSIGNED);

        List<WorkflowInstanceTask> inProgressTasks = taskRepository
                .findByInstanceIdAndTaskStatus(instance.getId(), TaskStatus.IN_PROGRESS);

        long incompleteTasks = pendingTasks.stream()
                .filter(task -> task.getStateTask().getIsMandatory())
                .count()
                + assignedTasks.stream()
                .filter(task -> task.getStateTask().getIsMandatory())
                .count()
                + inProgressTasks.stream()
                .filter(task -> task.getStateTask().getIsMandatory())
                .count();

        if (incompleteTasks > 0) {
            throw new InvalidTransitionException(
                    "Cannot transition: " + incompleteTasks + " mandatory tasks are not completed"
            );
        }
    }

    /**
     * Get available transitions for current state
     */
    @Transactional(readOnly = true)
    public List<WorkflowTransition> getAvailableTransitions(Long instanceId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Instance not found"));

        if (instance.getCurrentState() == null) {
            return List.of();
        }

        return transitionRepository.findByFromStateId(instance.getCurrentState().getId());
    }

    /**
     * Publish state changed event
     */
    private void publishStateChangedEvent(WorkflowInstance instance,
                                          WorkflowState fromState,
                                          WorkflowState toState,
                                          WorkflowTransition transition,
                                          Long userId,
                                          String userName) {
        StateChangedEvent event = StateChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .instanceId(instance.getId())
                .orderId(instance.getOrderId())
                .processCode(instance.getProcess().getProcessCode())
                .fromStateId(fromState != null ? fromState.getId() : null)
                .fromStateName(fromState != null ? fromState.getStateName() : null)
                .toStateId(toState.getId())
                .toStateName(toState.getStateName())
                .transitionId(transition != null ? transition.getId() : null)
                .transitionName(transition != null ? transition.getTransitionName() : null)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .userName(userName)
                .build();

        eventPublisher.publishStateChangedEvent(event);
    }
}
