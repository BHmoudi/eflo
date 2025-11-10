package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.*;
import com.eflo.workflow.domain.enums.TaskType;
import com.eflo.workflow.domain.repository.*;
import com.eflo.workflow.exception.ProcessNotFoundException;
import com.eflo.workflow.exception.InvalidWorkflowStateException;
import com.eflo.workflow.web.dto.request.CreateStateRequest;
import com.eflo.workflow.web.dto.request.CreateTaskRequest;
import com.eflo.workflow.web.dto.response.StateResponse;
import com.eflo.workflow.web.dto.response.TaskDefinitionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * State Management Service
 * Handles workflow states and tasks configuration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StateManagementService {

    private final WorkflowProcessRepository processRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowStateTaskRepository stateTaskRepository;
    private final WorkflowStateTaskDocumentRepository taskDocumentRepository;
    private final WorkflowTaskTypesRepository taskTypesRepository;
    private final WorkflowApprovalChainsRepository approvalChainsRepository;
    private final WorkflowTaskDependenciesRepository taskDependenciesRepository;
    private final WorkflowTaskConditionsRepository taskConditionsRepository;
    private final WorkflowRoleRepository roleRepository;

    /**
     * Add a state to a process
     */
    @Transactional
    public StateResponse addState(String processCode, CreateStateRequest request) {
        log.info("Adding state {} to process {}", request.getStateCode(), processCode);

        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        // Check if state already exists
        if (stateRepository.findByProcessIdAndStateCode(process.getId(), request.getStateCode()).isPresent()) {
            throw new InvalidWorkflowStateException("State with code " + request.getStateCode() + " already exists");
        }

        WorkflowState state = WorkflowState.builder()
                .process(process)
                .stateCode(request.getStateCode())
                .stateName(request.getStateName())
                .description(request.getDescription())
                .stateOrder(request.getStateOrder())
                .stateType(request.getStateType())
                .expectedDurationHours(request.getExpectedDurationHours())
                .isFinalState(request.getIsFinalState())
                .requiresApproval(request.getRequiresApproval())
                .allowSkip(request.getAllowSkip())
                .configuration(request.getConfiguration())
                .build();

        WorkflowState savedState = stateRepository.save(state);
        return mapToStateResponse(savedState);
    }

    /**
     * Get all states for a process
     */
    @Transactional(readOnly = true)
    public List<StateResponse> getProcessStates(String processCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        List<WorkflowState> states = stateRepository.findByProcessIdOrderByStateOrderAsc(process.getId());
        return states.stream()
                .map(this::mapToStateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific state
     */
    @Transactional(readOnly = true)
    public StateResponse getState(String processCode, String stateCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        return mapToStateResponse(state);
    }

    /**
     * Update a state
     */
    @Transactional
    public StateResponse updateState(String processCode, String stateCode, CreateStateRequest request) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        state.setStateName(request.getStateName());
        state.setDescription(request.getDescription());
        state.setStateOrder(request.getStateOrder());
        state.setStateType(request.getStateType());
        state.setExpectedDurationHours(request.getExpectedDurationHours());
        state.setIsFinalState(request.getIsFinalState());
        state.setRequiresApproval(request.getRequiresApproval());
        state.setAllowSkip(request.getAllowSkip());
        state.setConfiguration(request.getConfiguration());

        WorkflowState updated = stateRepository.save(state);
        return mapToStateResponse(updated);
    }

    /**
     * Delete a state
     */
    @Transactional
    public void deleteState(String processCode, String stateCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        stateRepository.delete(state);
    }

    /**
     * Add a task to a state
     */
    @Transactional
    public TaskDefinitionResponse addTask(String processCode, String stateCode, CreateTaskRequest request) {
        log.info("Adding task {} to state {} in process {}", request.getTaskCode(), stateCode, processCode);

        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        // Build the task
        WorkflowStateTask.WorkflowStateTaskBuilder taskBuilder = WorkflowStateTask.builder()
                .state(state)
                .taskCode(request.getTaskCode())
                .taskName(request.getTaskName())
                .description(request.getDescription())
                .taskType(request.getTaskType())
                .taskOrder(request.getTaskOrder())
                .assignedToRole(request.getAssignedToRole())
                .assignedToUserId(request.getAssignedToUserId())
                .isMandatory(request.getIsMandatory())
                .expectedDurationHours(request.getExpectedDurationHours())
                .autoAssign(request.getAutoAssign())
                .requiresApproval(request.getRequiresApproval())
                .formDefinition(request.getFormDefinition())
                .validationRules(request.getValidationRules())
                .configuration(request.getConfiguration());

        WorkflowStateTask task = taskBuilder.build();
        WorkflowStateTask savedTask = stateTaskRepository.save(task);

        // Handle dependencies
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            for (CreateTaskRequest.TaskDependencyRequest dep : request.getDependencies()) {
                WorkflowStateTask requiredTask = stateTaskRepository.findByStateIdAndTaskCode(state.getId(), dep.getRequiredTaskCode())
                        .orElseThrow(() -> new InvalidWorkflowStateException("Required task not found: " + dep.getRequiredTaskCode()));

                com.eflo.workflow.domain.WorkflowTaskDependency dependency = com.eflo.workflow.domain.WorkflowTaskDependency.builder()
                        .dependentTask(savedTask)
                        .requiredTask(requiredTask)
                        .dependencyType(com.eflo.workflow.domain.enums.DependencyType.valueOf(dep.getDependencyType()))
                        .requiredStatus(dep.getRequiredStatus())
                        .build();

                taskDependenciesRepository.save(dependency);
            }
        }

        // Handle conditions
        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            for (CreateTaskRequest.TaskConditionRequest cond : request.getConditions()) {
                com.eflo.workflow.domain.WorkflowTaskCondition condition = com.eflo.workflow.domain.WorkflowTaskCondition.builder()
                        .task(savedTask)
                        .conditionType(com.eflo.workflow.domain.enums.ConditionType.valueOf(cond.getConditionType()))
                        .conditionField(cond.getConditionField())
                        .conditionOperator(cond.getConditionOperator())
                        .conditionValue(cond.getConditionValue())
                        .conditionExpression(cond.getConditionExpression())
                        .isActive(true)
                        .build();

                taskConditionsRepository.save(condition);
            }
        }

        // Link approval chain if provided
        if (request.getApprovalChainId() != null) {
            // This would be handled by a separate repository/entity
            // Omitted for brevity as it's already in the DB schema
        }

        // Handle required documents
        if (request.getRequiredDocuments() != null && !request.getRequiredDocuments().isEmpty()) {
            int order = 0;
            for (String documentTypeCode : request.getRequiredDocuments()) {
                WorkflowStateTaskDocument docReq = WorkflowStateTaskDocument.builder()
                        .stateTask(savedTask)
                        .documentTypeCode(documentTypeCode)
                        .isMandatory(true) // Default to mandatory
                        .validationRequired(true)
                        .displayOrder(order++)
                        .build();

                taskDocumentRepository.save(docReq);
                log.debug("Added document requirement: taskCode={}, documentTypeCode={}",
                        savedTask.getTaskCode(), documentTypeCode);
            }
        }

        return mapToTaskDefinitionResponse(savedTask);
    }

    /**
     * Get all tasks for a state
     */
    @Transactional(readOnly = true)
    public List<TaskDefinitionResponse> getStateTasks(String processCode, String stateCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        List<WorkflowStateTask> tasks = stateTaskRepository.findByStateIdOrderByTaskOrderAsc(state.getId());
        return tasks.stream()
                .map(this::mapToTaskDefinitionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a task
     */
    @Transactional
    public TaskDefinitionResponse updateTask(String processCode, String stateCode, String taskCode, CreateTaskRequest request) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        WorkflowStateTask task = stateTaskRepository.findByStateIdAndTaskCode(state.getId(), taskCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("Task not found: " + taskCode));

        // Update fields
        task.setTaskName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setTaskOrder(request.getTaskOrder());
        task.setAssignedToRole(request.getAssignedToRole());
        task.setAssignedToUserId(request.getAssignedToUserId());
        task.setIsMandatory(request.getIsMandatory());
        task.setExpectedDurationHours(request.getExpectedDurationHours());
        task.setAutoAssign(request.getAutoAssign());
        task.setRequiresApproval(request.getRequiresApproval());
        task.setFormDefinition(request.getFormDefinition());
        task.setValidationRules(request.getValidationRules());
        task.setConfiguration(request.getConfiguration());

        WorkflowStateTask updated = stateTaskRepository.save(task);

        // Update required documents
        if (request.getRequiredDocuments() != null) {
            // Delete existing document requirements
            taskDocumentRepository.deleteByStateTaskId(updated.getId());

            // Add new document requirements
            int order = 0;
            for (String documentTypeCode : request.getRequiredDocuments()) {
                WorkflowStateTaskDocument docReq = WorkflowStateTaskDocument.builder()
                        .stateTask(updated)
                        .documentTypeCode(documentTypeCode)
                        .isMandatory(true)
                        .validationRequired(true)
                        .displayOrder(order++)
                        .build();

                taskDocumentRepository.save(docReq);
            }
        }

        return mapToTaskDefinitionResponse(updated);
    }

    /**
     * Delete a task
     */
    @Transactional
    public void deleteTask(String processCode, String stateCode, String taskCode) {
        WorkflowProcess process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new ProcessNotFoundException(processCode));

        WorkflowState state = stateRepository.findByProcessIdAndStateCode(process.getId(), stateCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("State not found: " + stateCode));

        WorkflowStateTask task = stateTaskRepository.findByStateIdAndTaskCode(state.getId(), taskCode)
                .orElseThrow(() -> new InvalidWorkflowStateException("Task not found: " + taskCode));

        stateTaskRepository.delete(task);
    }

    // Helper methods
    private StateResponse mapToStateResponse(WorkflowState state) {
        List<TaskDefinitionResponse> tasks = stateTaskRepository.findByStateIdOrderByTaskOrderAsc(state.getId())
                .stream()
                .map(this::mapToTaskDefinitionResponse)
                .collect(Collectors.toList());

        return StateResponse.builder()
                .id(state.getId())
                .stateCode(state.getStateCode())
                .stateName(state.getStateName())
                .description(state.getDescription())
                .stateOrder(state.getStateOrder())
                .stateType(state.getStateType())
                .expectedDurationHours(state.getExpectedDurationHours())
                .isFinalState(state.getIsFinalState())
                .requiresApproval(state.getRequiresApproval())
                .allowSkip(state.getAllowSkip())
                .configuration(state.getConfiguration())
                .tasks(tasks)
                .createdAt(state.getCreatedAt())
                .updatedAt(state.getUpdatedAt())
                .build();
    }

    private TaskDefinitionResponse mapToTaskDefinitionResponse(WorkflowStateTask task) {
        // Get dependencies - need to find by task entity not by ID
        List<com.eflo.workflow.domain.WorkflowTaskDependency> deps = taskDependenciesRepository.findAll().stream()
                .filter(d -> d.getDependentTask() != null && d.getDependentTask().getId().equals(task.getId()))
                .collect(Collectors.toList());

        List<TaskDefinitionResponse.TaskDependencyInfo> dependencies = deps.stream()
                .map(dep -> TaskDefinitionResponse.TaskDependencyInfo.builder()
                        .id(dep.getId())
                        .requiredTaskCode(dep.getRequiredTask() != null ? dep.getRequiredTask().getTaskCode() : null)
                        .dependencyType(dep.getDependencyType() != null ? dep.getDependencyType().name() : null)
                        .requiredStatus(dep.getRequiredStatus())
                        .build())
                .collect(Collectors.toList());

        // Get conditions - need to find by task entity
        List<com.eflo.workflow.domain.WorkflowTaskCondition> conds = taskConditionsRepository.findAll().stream()
                .filter(c -> c.getTask() != null && c.getTask().getId().equals(task.getId()))
                .collect(Collectors.toList());

        List<TaskDefinitionResponse.TaskConditionInfo> conditions = conds.stream()
                .map(cond -> TaskDefinitionResponse.TaskConditionInfo.builder()
                        .id(cond.getId())
                        .conditionType(cond.getConditionType() != null ? cond.getConditionType().name() : null)
                        .conditionField(cond.getConditionField())
                        .conditionOperator(cond.getConditionOperator())
                        .conditionValue(cond.getConditionValue())
                        .conditionExpression(cond.getConditionExpression())
                        .build())
                .collect(Collectors.toList());

        // Get required documents
        List<String> requiredDocuments = taskDocumentRepository.findByStateTaskIdOrderByDisplayOrderAsc(task.getId())
                .stream()
                .map(WorkflowStateTaskDocument::getDocumentTypeCode)
                .collect(Collectors.toList());

        return TaskDefinitionResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .taskOrder(task.getTaskOrder())
                .assignedToRole(task.getAssignedToRole())
                .assignedRoleId(null) // TODO: Add this field to WorkflowStateTask if needed
                .assignedToUserId(task.getAssignedToUserId())
                .isMandatory(task.getIsMandatory())
                .expectedDurationHours(task.getExpectedDurationHours())
                .autoAssign(task.getAutoAssign())
                .requiresApproval(task.getRequiresApproval())
                .formDefinition(task.getFormDefinition())
                .validationRules(task.getValidationRules())
                .configuration(task.getConfiguration())
                .requiredDocuments(requiredDocuments)
                .dependencies(dependencies)
                .conditions(conditions)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
