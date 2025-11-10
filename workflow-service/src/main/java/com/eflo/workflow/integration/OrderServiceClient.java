package com.eflo.workflow.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign Client for Order Service
 *
 * Provides integration with the Order Service for order data retrieval.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@FeignClient(name = "order-service", url = "${eflo.workflow.integration.order-service.url}")
public interface OrderServiceClient {

    /**
     * Get order details by ID
     *
     * @param orderId Order ID
     * @return Order details
     */
    @GetMapping("/api/v1/orders/{orderId}")
    Map<String, Object> getOrderById(@PathVariable Long orderId);

    /**
     * Get order status
     *
     * @param orderId Order ID
     * @return Order status information
     */
    @GetMapping("/api/v1/orders/{orderId}/status")
    Map<String, Object> getOrderStatus(@PathVariable Long orderId);

    /**
     * Update order workflow status
     *
     * @param id Order ID
     * @param status New workflow status
     */
    @PutMapping("/api/v1/orders/{id}/workflow-status")
    void updateOrderWorkflowStatus(@PathVariable Long id, @RequestParam String status);

    /**
     * Get order affaire code
     *
     * @param orderId Order ID
     * @return Affaire code
     */
    @GetMapping("/api/v1/orders/{orderId}/affaire-code")
    String getOrderAffaireCode(@PathVariable Long orderId);
}
