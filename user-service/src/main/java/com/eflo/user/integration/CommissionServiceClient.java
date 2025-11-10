package com.eflo.user.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for communicating with the Commission Service.
 */
@FeignClient(
        name = "commission-service",
        fallback = CommissionServiceClientFallback.class
)
public interface CommissionServiceClient {

    /**
     * Get commission summary for a user.
     *
     * @param userId the user ID
     * @return commission summary with total, pending, paid amounts
     */
    @GetMapping("/api/commissions/user/{userId}/summary")
    Map<String, BigDecimal> getCommissionSummaryByUserId(@PathVariable("userId") String userId);

    /**
     * Get total commissions for a user.
     *
     * @param userId the user ID
     * @return total commission amount
     */
    @GetMapping("/api/commissions/user/{userId}/total")
    BigDecimal getTotalCommissionsByUserId(@PathVariable("userId") String userId);

    /**
     * Get commission details for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return commission summary
     */
    @GetMapping("/api/commissions/business-unit/{businessUnitId}/summary")
    Map<String, BigDecimal> getCommissionsByBusinessUnitId(@PathVariable("businessUnitId") String businessUnitId);

    /**
     * Check if user has pending commissions.
     *
     * @param userId the user ID
     * @return true if user has pending commissions
     */
    @GetMapping("/api/commissions/user/{userId}/has-pending")
    Boolean hasPendingCommissions(@PathVariable("userId") String userId);

    /**
     * Get commission details for a specific order.
     *
     * @param orderId the order ID
     * @param userId  the user ID
     * @return commission amount
     */
    @GetMapping("/api/commissions/order/{orderId}")
    BigDecimal getCommissionByOrderId(@PathVariable("orderId") String orderId, @RequestParam("userId") String userId);
}
