package com.eflo.document.integration;

import com.eflo.document.integration.dto.OrderResponse;
import com.eflo.document.integration.dto.BusinessUnitResponse;
import com.eflo.document.integration.dto.DocumentStatusUpdateRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for communicating with the Order Service.
 * Provides methods to retrieve order information, update document status, and access business unit details.
 *
 * <p>This client includes circuit breaker configuration for resilience and fault tolerance.
 * Fallback methods are provided to handle service unavailability gracefully.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@FeignClient(
    name = "order-service",
    path = "/api/v1/orders",
    configuration = FeignClientConfiguration.class
)
public interface OrderServiceClient {

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the ID of the order to retrieve
     * @return the order details
     */
    @GetMapping("/{id}")
    @CircuitBreaker(name = "order-service", fallbackMethod = "getOrderFallback")
    OrderResponse getOrder(@PathVariable("id") Long orderId);

    /**
     * Retrieves an order by its order number.
     *
     * @param orderNumber the order number to search for
     * @return the order details
     */
    @GetMapping("/number/{orderNumber}")
    @CircuitBreaker(name = "order-service", fallbackMethod = "getOrderByNumberFallback")
    OrderResponse getOrderByNumber(@PathVariable("orderNumber") String orderNumber);

    /**
     * Updates the document status for a specific order.
     *
     * <p>This endpoint is used to notify the order service when document
     * validation status changes (e.g., all documents validated, documents rejected).</p>
     *
     * @param orderId the ID of the order
     * @param request the document status update request
     */
    @PutMapping("/{id}/documents/status")
    @CircuitBreaker(name = "order-service", fallbackMethod = "updateDocumentStatusFallback")
    void updateDocumentStatus(
        @PathVariable("id") Long orderId,
        @RequestBody DocumentStatusUpdateRequest request
    );

    /**
     * Retrieves the business unit associated with an order.
     *
     * @param orderId the ID of the order
     * @return the business unit details
     */
    @GetMapping("/{id}/business-unit")
    @CircuitBreaker(name = "order-service", fallbackMethod = "getBusinessUnitFallback")
    BusinessUnitResponse getBusinessUnit(@PathVariable("id") Long orderId);

    /**
     * Fallback method for getOrder when the order service is unavailable.
     *
     * @param orderId the order ID
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default OrderResponse getOrderFallback(Long orderId, Throwable throwable) {
        // Log the error appropriately
        return null;
    }

    /**
     * Fallback method for getOrderByNumber when the order service is unavailable.
     *
     * @param orderNumber the order number
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default OrderResponse getOrderByNumberFallback(String orderNumber, Throwable throwable) {
        // Log the error appropriately
        return null;
    }

    /**
     * Fallback method for updateDocumentStatus when the order service is unavailable.
     *
     * @param orderId the order ID
     * @param request the update request
     * @param throwable the exception that triggered the fallback
     */
    default void updateDocumentStatusFallback(
        Long orderId,
        DocumentStatusUpdateRequest request,
        Throwable throwable
    ) {
        // Log the error appropriately
        // Consider queuing the update for retry
    }

    /**
     * Fallback method for getBusinessUnit when the order service is unavailable.
     *
     * @param orderId the order ID
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default BusinessUnitResponse getBusinessUnitFallback(Long orderId, Throwable throwable) {
        // Log the error appropriately
        return null;
    }
}
