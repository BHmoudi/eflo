package com.eflo.user.integration;

import com.eflo.user.integration.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for OrderServiceClient.
 * Provides graceful degradation when the Order Service is unavailable.
 */
@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public List<OrderDTO> getOrdersByUserId(String userId) {
        log.warn("Order Service unavailable. Returning empty list for user orders. UserId: {}", userId);
        return Collections.emptyList();
    }

    @Override
    public List<OrderDTO> getOrdersByBusinessUnitId(String businessUnitId) {
        log.warn("Order Service unavailable. Returning empty list for business unit orders. BusinessUnitId: {}", businessUnitId);
        return Collections.emptyList();
    }

    @Override
    public OrderDTO getOrderById(String orderId) {
        log.warn("Order Service unavailable. Returning null for order details. OrderId: {}", orderId);
        return null;
    }

    @Override
    public Boolean checkUserAccess(String orderId, String userId) {
        log.warn("Order Service unavailable. Defaulting to no access. OrderId: {}, UserId: {}", orderId, userId);
        return false;
    }
}
