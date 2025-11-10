package com.eflo.user.integration;

import com.eflo.user.integration.dto.WorkflowInstanceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for WorkflowServiceClient.
 * Provides graceful degradation when the Workflow Service is unavailable.
 */
@Slf4j
@Component
public class WorkflowServiceClientFallback implements WorkflowServiceClient {

    @Override
    public List<WorkflowInstanceDTO> getWorkflowsByUserId(String userId) {
        log.warn("Workflow Service unavailable. Returning empty list for user workflows. UserId: {}", userId);
        return Collections.emptyList();
    }

    @Override
    public List<WorkflowInstanceDTO> getWorkflowsByBusinessUnitId(String businessUnitId) {
        log.warn("Workflow Service unavailable. Returning empty list for business unit workflows. BusinessUnitId: {}", businessUnitId);
        return Collections.emptyList();
    }

    @Override
    public WorkflowInstanceDTO getWorkflowById(String workflowId) {
        log.warn("Workflow Service unavailable. Returning null for workflow details. WorkflowId: {}", workflowId);
        return null;
    }

    @Override
    public Boolean checkUserAccess(String workflowId, String userId) {
        log.warn("Workflow Service unavailable. Defaulting to no access. WorkflowId: {}, UserId: {}", workflowId, userId);
        return false;
    }

    @Override
    public List<WorkflowInstanceDTO> getPendingWorkflowsByUserId(String userId) {
        log.warn("Workflow Service unavailable. Returning empty list for pending workflows. UserId: {}", userId);
        return Collections.emptyList();
    }
}
