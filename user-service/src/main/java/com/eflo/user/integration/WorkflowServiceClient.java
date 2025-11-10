package com.eflo.user.integration;

import com.eflo.user.integration.dto.WorkflowInstanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with the Workflow Service.
 */
@FeignClient(
        name = "workflow-service",
        fallback = WorkflowServiceClientFallback.class
)
public interface WorkflowServiceClient {

    /**
     * Get all workflow instances assigned to a user.
     *
     * @param userId the user ID
     * @return list of workflow instances
     */
    @GetMapping("/api/workflows/user/{userId}")
    List<WorkflowInstanceDTO> getWorkflowsByUserId(@PathVariable("userId") String userId);

    /**
     * Get workflow instances for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return list of workflow instances
     */
    @GetMapping("/api/workflows/business-unit/{businessUnitId}")
    List<WorkflowInstanceDTO> getWorkflowsByBusinessUnitId(@PathVariable("businessUnitId") String businessUnitId);

    /**
     * Get workflow instance by ID.
     *
     * @param workflowId the workflow instance ID
     * @return workflow instance details
     */
    @GetMapping("/api/workflows/{workflowId}")
    WorkflowInstanceDTO getWorkflowById(@PathVariable("workflowId") String workflowId);

    /**
     * Check if user has access to a specific workflow.
     *
     * @param userId     the user ID
     * @param workflowId the workflow instance ID
     * @return true if user has access
     */
    @GetMapping("/api/workflows/{workflowId}/access")
    Boolean checkUserAccess(@PathVariable("workflowId") String workflowId, @RequestParam("userId") String userId);

    /**
     * Get pending workflow tasks for a user.
     *
     * @param userId the user ID
     * @return list of pending workflow instances
     */
    @GetMapping("/api/workflows/user/{userId}/pending")
    List<WorkflowInstanceDTO> getPendingWorkflowsByUserId(@PathVariable("userId") String userId);
}
