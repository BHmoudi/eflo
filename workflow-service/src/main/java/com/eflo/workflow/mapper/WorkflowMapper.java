package com.eflo.workflow.mapper;

import com.eflo.workflow.domain.entity.*;
import com.eflo.workflow.domain.model.request.CreateInstanceRequest;
import com.eflo.workflow.domain.model.request.CreateProcessRequest;
import com.eflo.workflow.domain.model.response.InstanceResponse;
import com.eflo.workflow.domain.model.response.ProcessResponse;
import com.eflo.workflow.domain.model.response.TaskResponse;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct Mapper for Workflow Entities and DTOs
 *
 * Provides bidirectional mapping between entities and DTOs.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowMapper {

    // Process mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "states", ignore = true)
    @Mapping(target = "instances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "processVersion", constant = "1")
    WorkflowProcess toProcess(CreateProcessRequest request);

    ProcessResponse toProcessResponse(WorkflowProcess process);

    List<ProcessResponse> toProcessResponseList(List<WorkflowProcess> processes);

    // Instance mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "process", ignore = true)
    @Mapping(target = "currentState", ignore = true)
    @Mapping(target = "instanceStatus", constant = "CREATED")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "historyEntries", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "actualCompletionDate", ignore = true)
    @Mapping(target = "isOverdue", constant = "false")
    @Mapping(target = "overdueSince", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "errorDetails", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    WorkflowInstance toInstance(CreateInstanceRequest request);

    @Mapping(source = "process.id", target = "processId")
    @Mapping(source = "process.processCode", target = "processCode")
    @Mapping(source = "process.processName", target = "processName")
    @Mapping(source = "currentState.id", target = "currentStateId")
    @Mapping(source = "currentState.stateName", target = "currentStateName")
    InstanceResponse toInstanceResponse(WorkflowInstance instance);

    List<InstanceResponse> toInstanceResponseList(List<WorkflowInstance> instances);

    // Task mappings
    @Mapping(source = "instance.id", target = "instanceId")
    @Mapping(target = "assignedToUserName", ignore = true)
    TaskResponse toTaskResponse(WorkflowInstanceTask task);

    List<TaskResponse> toTaskResponseList(List<WorkflowInstanceTask> tasks);

    @AfterMapping
    default void enrichTaskResponse(@MappingTarget TaskResponse taskResponse, WorkflowInstanceTask task) {
        // Can be used to add additional enrichment logic
    }
}
