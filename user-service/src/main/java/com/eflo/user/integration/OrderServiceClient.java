package com.eflo.user.integration;

import com.eflo.user.integration.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with the Order Service.
 */
@FeignClient(
        name = "order-service",
        fallback = OrderServiceClientFallback.class
)
public interface OrderServiceClient {

    /**
     * Get all orders for a specific user.
     *
     * @param userId the user ID
     * @return list of orders
     */
    @GetMapping("/api/orders/user/{userId}")
    List<OrderDTO> getOrdersByUserId(@PathVariable("userId") String userId);

    /**
     * Get orders for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return list of orders
     */
    @GetMapping("/api/orders/business-unit/{businessUnitId}")
    List<OrderDTO> getOrdersByBusinessUnitId(@PathVariable("businessUnitId") String businessUnitId);

    /**
     * Get order by ID.
     *
     * @param orderId the order ID
     * @return order details
     */
    @GetMapping("/api/orders/{orderId}")
    OrderDTO getOrderById(@PathVariable("orderId") String orderId);

    /**
     * Check if user has access to a specific order.
     *
     * @param userId  the user ID
     * @param orderId the order ID
     * @return true if user has access
     */
    @GetMapping("/api/orders/{orderId}/access")
    Boolean checkUserAccess(@PathVariable("orderId") String orderId, @RequestParam("userId") String userId);
}
