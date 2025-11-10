package com.eflo.docgen.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client for Order Service
 * Fetches order data for document generation
 */
@FeignClient(name = "order-service", path = "/api/v1/orders")
public interface OrderServiceClient {

    /**
     * Get complete order data for template rendering
     */
    @GetMapping("/{orderId}/template-data")
    Map<String, Object> getOrderDataForTemplate(@PathVariable("orderId") Long orderId);

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    Map<String, Object> getOrder(@PathVariable("orderId") Long orderId);
}
