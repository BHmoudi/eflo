package com.eflo.docgen.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client for Workflow Service
 * Fetches workflow instance data
 */
@FeignClient(name = "workflow-service", path = "/api/v1/workflow")
public interface WorkflowServiceClient {

    /**
     * Get workflow instance details
     */
    @GetMapping("/instances/{instanceId}")
    Map<String, Object> getWorkflowInstance(@PathVariable("instanceId") Long instanceId);

    /**
     * Get instances for an order
     */
    @GetMapping("/instances/order/{orderId}")
    java.util.List<Map<String, Object>> getInstancesByOrderId(@PathVariable("orderId") Long orderId);
}
