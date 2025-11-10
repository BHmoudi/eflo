package com.eflo.document.integration;

import com.eflo.document.integration.dto.WorkflowResponse;
import com.eflo.document.integration.dto.TaskResponse;
import com.eflo.document.integration.dto.TaskCompleteRequest;
import com.eflo.document.integration.dto.WorkflowEventRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for communicating with the Workflow Service.
 * Provides methods to interact with workflows and tasks related to document processing.
 *
 * <p>This client includes circuit breaker configuration for resilience and fault tolerance.
 * Fallback methods are provided to handle service unavailability gracefully.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@FeignClient(
    name = "workflow-service",
    path = "/api/v1/workflows",
    configuration = FeignClientConfiguration.class
)
public interface WorkflowServiceClient {

    /**
     * Retrieves the workflow associated with a specific order.
     *
     * @param orderId the ID of the order
     * @return the workflow details
     */
    @GetMapping("/order/{orderId}")
    @CircuitBreaker(name = "workflow-service", fallbackMethod = "getOrderWorkflowFallback")
    WorkflowResponse getOrderWorkflow(@PathVariable("orderId") Long orderId);

    /**
     * Retrieves a specific workflow task by its ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the task details
     */
    @GetMapping("/tasks/{taskId}")
    @CircuitBreaker(name = "workflow-service", fallbackMethod = "getTaskFallback")
    TaskResponse getTask(@PathVariable("taskId") Long taskId);

    /**
     * Marks a workflow task as complete.
     *
     * <p>This endpoint is used to notify the workflow service when a document-related
     * task has been completed (e.g., document validation, document upload).</p>
     *
     * @param taskId the ID of the task to complete
     * @param request the task completion request containing completion details
     * @return the updated task details
     */
    @PutMapping("/tasks/{taskId}/complete")
    @CircuitBreaker(name = "workflow-service", fallbackMethod = "completeTaskFallback")
    TaskResponse completeTask(
        @PathVariable("taskId") Long taskId,
        @RequestBody TaskCompleteRequest request
    );

    /**
     * Publishes a workflow event for a specific order.
     *
     * <p>This endpoint is used to trigger workflow state changes based on document events
     * (e.g., all documents uploaded, document rejected, document expired).</p>
     *
     * @param orderId the ID of the order
     * @param event the workflow event to publish
     */
    @PostMapping("/order/{orderId}/event")
    @CircuitBreaker(name = "workflow-service", fallbackMethod = "publishWorkflowEventFallback")
    void publishWorkflowEvent(
        @PathVariable("orderId") Long orderId,
        @RequestBody WorkflowEventRequest event
    );

    /**
     * Fallback method for getOrderWorkflow when the workflow service is unavailable.
     *
     * @param orderId the order ID
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default WorkflowResponse getOrderWorkflowFallback(Long orderId, Throwable throwable) {
        // Log the error appropriately
        return null;
    }

    /**
     * Fallback method for getTask when the workflow service is unavailable.
     *
     * @param taskId the task ID
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default TaskResponse getTaskFallback(Long taskId, Throwable throwable) {
        // Log the error appropriately
        return null;
    }

    /**
     * Fallback method for completeTask when the workflow service is unavailable.
     *
     * @param taskId the task ID
     * @param request the completion request
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default TaskResponse completeTaskFallback(
        Long taskId,
        TaskCompleteRequest request,
        Throwable throwable
    ) {
        // Log the error appropriately
        // Consider queuing the completion for retry
        return null;
    }

    /**
     * Fallback method for publishWorkflowEvent when the workflow service is unavailable.
     *
     * @param orderId the order ID
     * @param event the workflow event
     * @param throwable the exception that triggered the fallback
     */
    default void publishWorkflowEventFallback(
        Long orderId,
        WorkflowEventRequest event,
        Throwable throwable
    ) {
        // Log the error appropriately
        // Consider queuing the event for retry
    }
}
