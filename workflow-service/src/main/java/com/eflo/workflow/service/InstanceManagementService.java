package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.*;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.enums.StateType;
import com.eflo.workflow.domain.model.request.CreateInstanceRequest;
import com.eflo.workflow.domain.model.response.InstanceResponse;
import com.eflo.workflow.domain.repository.*;
import com.eflo.workflow.exception.InstanceNotFoundException;
import com.eflo.workflow.exception.InvalidWorkflowStateException;
import com.eflo.workflow.mapper.WorkflowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Instance Management Service
 *
 * Manages workflow instance lifecycle: creation, state management, and completion.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstanceManagementService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowProcessRepository processRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final WorkflowStateTaskDocumentRepository taskDocumentRepository;
    private final WorkflowMapper mapper;
    private final WorkflowEventPublisher eventPublisher;
    private final TaskManagementService taskManagementService;

    /**
     * Create a new workflow instance
     */
    @Transactional
    public InstanceResponse createInstance(CreateInstanceRequest request, String createdBy) {
        log.info("Creating workflow instance for order: {}", request.getOrderId());

        // Load process
        WorkflowProcess process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new RuntimeException("Process not found: " + request.getProcessId()));

        // Create instance
        WorkflowInstance instance = mapper.toInstance(request);
        instance.setProcess(process);
        instance.setInstanceStatus(InstanceStatus.CREATED);
        instance.setCreatedBy(createdBy);

        // Calculate expected completion
        if (request.getExpectedDurationDays() != null) {
            instance.setExpectedCompletionDate(
                    LocalDateTime.now().plusDays(request.getExpectedDurationDays())
            );
        } else if (process.getMaxDurationDays() != null) {
            instance.setExpectedCompletionDate(
                    LocalDateTime.now().plusDays(process.getMaxDurationDays())
            );
        }

        WorkflowInstance savedInstance = instanceRepository.save(instance);

        // Create history entry
        WorkflowHistory history = WorkflowHistory.forInstanceEvent(
                savedInstance,
                EventType.INSTANCE_CREATED,
                "Workflow instance created",
                null,
                createdBy
        );
        savedInstance.addHistoryEntry(history);
        instanceRepository.save(savedInstance);

        // Publish event
        eventPublisher.publishInstanceEvent(
                EventType.INSTANCE_CREATED,
                savedInstance.getId(),
                savedInstance.getOrderId(),
                process.getProcessCode(),
                null,
                createdBy,
                "Workflow instance created",
                null
        );

        log.info("Workflow instance created with ID: {}", savedInstance.getId());
        return mapper.toInstanceResponse(savedInstance);
    }

    /**
     * Start a workflow instance
     */
    @Transactional
    public InstanceResponse startInstance(Long instanceId, String startedBy) {
        log.info("Starting workflow instance: {}", instanceId);

        WorkflowInstance instance = getInstanceEntity(instanceId);

        if (instance.getInstanceStatus() != InstanceStatus.CREATED) {
            throw new InvalidWorkflowStateException(
                    instance.getInstanceStatus().name(),
                    "start"
            );
        }

        // Find start state
        WorkflowState startState = stateRepository.findStartStateByProcessId(instance.getProcess().getId())
                .orElseThrow(() -> new RuntimeException("Start state not found for process"));

        // Update instance
        instance.start();
        instance.setCurrentState(startState);
        instance.setUpdatedBy(startedBy);

        // Create tasks for start state
        taskManagementService.createTasksForState(instance, startState, startedBy);

        // Create history
        WorkflowHistory history = WorkflowHistory.forInstanceEvent(
                instance,
                EventType.INSTANCE_STARTED,
                "Workflow instance started at state: " + startState.getStateName(),
                null,
                startedBy
        );
        instance.addHistoryEntry(history);

        WorkflowInstance savedInstance = instanceRepository.save(instance);

        // Publish event
        eventPublisher.publishInstanceEvent(
                EventType.INSTANCE_STARTED,
                savedInstance.getId(),
                savedInstance.getOrderId(),
                savedInstance.getProcess().getProcessCode(),
                null,
                startedBy,
                "Instance started",
                null
        );

        log.info("Workflow instance started: {}", instanceId);
        return mapper.toInstanceResponse(savedInstance);
    }

    /**
     * Pause a workflow instance
     */
    @Transactional
    public void pauseInstance(Long instanceId, String pausedBy) {
        log.info("Pausing workflow instance: {}", instanceId);

        WorkflowInstance instance = getInstanceEntity(instanceId);

        if (instance.getInstanceStatus() != InstanceStatus.RUNNING) {
            throw new InvalidWorkflowStateException(
                    instance.getInstanceStatus().name(),
                    "pause"
            );
        }

        instance.pause();
        instance.setUpdatedBy(pausedBy);

        WorkflowHistory history = WorkflowHistory.forInstanceEvent(
                instance,
                EventType.INSTANCE_PAUSED,
                "Workflow instance paused",
                null,
                pausedBy
        );
        instance.addHistoryEntry(history);

        instanceRepository.save(instance);

        eventPublisher.publishInstanceEvent(
                EventType.INSTANCE_PAUSED,
                instance.getId(),
                instance.getOrderId(),
                instance.getProcess().getProcessCode(),
                null,
                pausedBy,
                "Instance paused",
                null
        );
    }

    /**
     * Resume a paused workflow instance
     */
    @Transactional
    public void resumeInstance(Long instanceId, String resumedBy) {
        log.info("Resuming workflow instance: {}", instanceId);

        WorkflowInstance instance = getInstanceEntity(instanceId);

        if (instance.getInstanceStatus() != InstanceStatus.PAUSED) {
            throw new InvalidWorkflowStateException(
                    instance.getInstanceStatus().name(),
                    "resume"
            );
        }

        instance.resume();
        instance.setUpdatedBy(resumedBy);

        WorkflowHistory history = WorkflowHistory.forInstanceEvent(
                instance,
                EventType.INSTANCE_RESUMED,
                "Workflow instance resumed",
                null,
                resumedBy
        );
        instance.addHistoryEntry(history);

        instanceRepository.save(instance);

        eventPublisher.publishInstanceEvent(
                EventType.INSTANCE_RESUMED,
                instance.getId(),
                instance.getOrderId(),
                instance.getProcess().getProcessCode(),
                null,
                resumedBy,
                "Instance resumed",
                null
        );
    }

    /**
     * Cancel a workflow instance
     */
    @Transactional
    public void cancelInstance(Long instanceId, String reason, String cancelledBy) {
        log.info("Cancelling workflow instance: {}", instanceId);

        WorkflowInstance instance = getInstanceEntity(instanceId);

        if (instance.getInstanceStatus().isTerminal()) {
            throw new InvalidWorkflowStateException(
                    instance.getInstanceStatus().name(),
                    "cancel"
            );
        }

        instance.cancel();
        instance.setUpdatedBy(cancelledBy);
        instance.setErrorMessage(reason);

        WorkflowHistory history = WorkflowHistory.forInstanceEvent(
                instance,
                EventType.INSTANCE_CANCELLED,
                "Workflow instance cancelled: " + reason,
                null,
                cancelledBy
        );
        instance.addHistoryEntry(history);

        instanceRepository.save(instance);

        eventPublisher.publishInstanceEvent(
                EventType.INSTANCE_CANCELLED,
                instance.getId(),
                instance.getOrderId(),
                instance.getProcess().getProcessCode(),
                null,
                cancelledBy,
                "Instance cancelled",
                null
        );
    }

    /**
     * Get instance by ID
     */
    @Transactional(readOnly = true)
    public InstanceResponse getInstance(Long instanceId) {
        WorkflowInstance instance = getInstanceEntity(instanceId);
        return mapper.toInstanceResponse(instance);
    }

    /**
     * Get instance by order ID
     */
    @Transactional(readOnly = true)
    public List<InstanceResponse> getInstanceByOrderId(Long orderId) {
        List<WorkflowInstance> instances = instanceRepository.findByOrderId(orderId);
        return mapper.toInstanceResponseList(instances);
    }

    /**
     * Get instances by status
     */
    @Transactional(readOnly = true)
    public List<InstanceResponse> getInstancesByStatus(InstanceStatus status) {
        List<WorkflowInstance> instances = instanceRepository.findByInstanceStatus(status);
        return mapper.toInstanceResponseList(instances);
    }

    /**
     * Get instance entity (for internal use)
     */
    @Transactional(readOnly = true)
    public WorkflowInstance getInstanceEntity(Long instanceId) {
        return instanceRepository.findById(instanceId)
                .orElseThrow(() -> new InstanceNotFoundException(instanceId));
    }

    /**
     * Check instance overdue status
     */
    @Transactional
    public void checkAndUpdateOverdueStatus(Long instanceId) {
        WorkflowInstance instance = getInstanceEntity(instanceId);
        instance.checkOverdue();
        instanceRepository.save(instance);

        if (instance.getIsOverdue()) {
            eventPublisher.publishDeadlineEvent(
                    EventType.INSTANCE_OVERDUE,
                    instance.getId(),
                    instance.getOrderId(),
                    "Instance is overdue"
            );
        }
    }

    /**
     * Create workflow instance automatically from order event
     * This is called by OrderEventListener when an order is created
     */
    @Transactional
    public WorkflowInstance createInstanceFromOrderEvent(Long orderId, String orderType, String orderNumber, String createdBy) {
        log.info("Auto-creating workflow instance for order: {} (type: {})", orderId, orderType);

        // Find active workflow for this order type
        List<WorkflowProcess> processes = processRepository.findByOrderTypeAndIsActiveTrue(orderType);

        if (processes.isEmpty()) {
            log.warn("No active workflow found for order type: {}", orderType);
            return null;
        }

        if (processes.size() > 1) {
            log.warn("Multiple active workflows found for order type: {}. Using first one.", orderType);
        }

        WorkflowProcess process = processes.get(0);

        // Find START state
        WorkflowState startState = stateRepository.findStartStateByProcessId(process.getId())
                .orElseThrow(() -> new RuntimeException("START state not found for process: " + process.getProcessCode()));

        // Create instance
        WorkflowInstance instance = WorkflowInstance.builder()
                .process(process)
                .orderId(orderId)
                .instanceName("Workflow for Order #" + orderNumber)
                .instanceStatus(InstanceStatus.RUNNING)
                .currentState(startState)
                .startDate(LocalDateTime.now())
                .createdBy(createdBy != null ? createdBy : "SYSTEM")
                .updatedBy(createdBy != null ? createdBy : "SYSTEM")
                .build();

        // Set expected completion
        if (process.getMaxDurationDays() != null) {
            instance.setExpectedCompletionDate(
                    LocalDateTime.now().plusDays(process.getMaxDurationDays())
            );
        }

        WorkflowInstance savedInstance = instanceRepository.save(instance);

        // Create history entry
        WorkflowHistory history = WorkflowHistory.forInstanceEvent(
                savedInstance,
                EventType.INSTANCE_CREATED,
                "Workflow instance auto-created from order creation event",
                null,
                createdBy != null ? createdBy : "SYSTEM"
        );
        savedInstance.addHistoryEntry(history);
        historyRepository.save(history);

        // Create tasks for start state
        taskManagementService.createTasksForState(savedInstance, startState, createdBy != null ? createdBy : "SYSTEM");

        log.info("Workflow instance auto-created: instanceId={}, orderId={}, processCode={}",
                savedInstance.getId(), orderId, process.getProcessCode());

        // Publish instance created event with document requirements
        publishInstanceCreatedEventWithDocuments(savedInstance, startState);

        return savedInstance;
    }

    /**
     * Publish workflow instance created event with document requirements
     * This event will be consumed by document-service to initialize document placeholders
     */
    private void publishInstanceCreatedEventWithDocuments(WorkflowInstance instance, WorkflowState startState) {
        // Collect all required documents from START state tasks
        List<java.util.Map<String, Object>> requiredDocuments = new java.util.ArrayList<>();

        for (WorkflowStateTask task : startState.getTasks()) {
            for (WorkflowStateTaskDocument doc : task.getRequiredDocuments()) {
                requiredDocuments.add(java.util.Map.of(
                        "documentTypeCode", doc.getDocumentTypeCode(),
                        "isMandatory", doc.getIsMandatory(),
                        "uploadDeadlineHours", doc.getUploadDeadlineHours() != null ? doc.getUploadDeadlineHours() : 0,
                        "validationRequired", doc.getValidationRequired(),
                        "description", doc.getDescription() != null ? doc.getDescription() : "",
                        "taskCode", task.getTaskCode(),
                        "taskName", task.getTaskName()
                ));
            }
        }

        // Publish event to Kafka
        java.util.Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("requiredDocuments", requiredDocuments);
        eventData.put("stateCode", startState.getStateCode());
        eventData.put("stateName", startState.getStateName());

        eventPublisher.publishInstanceEvent(
                EventType.INSTANCE_CREATED,
                instance.getId(),
                instance.getOrderId(),
                instance.getProcess().getProcessCode(),
                null, // userId
                instance.getCreatedBy(),
                "Workflow instance created with " + requiredDocuments.size() + " required documents",
                eventData
        );

        log.info("Published instance created event with {} required documents", requiredDocuments.size());
    }
}
