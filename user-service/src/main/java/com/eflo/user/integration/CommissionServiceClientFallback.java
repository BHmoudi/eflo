package com.eflo.user.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback implementation for CommissionServiceClient.
 * Provides graceful degradation when the Commission Service is unavailable.
 */
@Slf4j
@Component
public class CommissionServiceClientFallback implements CommissionServiceClient {

    @Override
    public Map<String, BigDecimal> getCommissionSummaryByUserId(String userId) {
        log.warn("Commission Service unavailable. Returning empty summary. UserId: {}", userId);
        Map<String, BigDecimal> emptySummary = new HashMap<>();
        emptySummary.put("total", BigDecimal.ZERO);
        emptySummary.put("pending", BigDecimal.ZERO);
        emptySummary.put("paid", BigDecimal.ZERO);
        return emptySummary;
    }

    @Override
    public BigDecimal getTotalCommissionsByUserId(String userId) {
        log.warn("Commission Service unavailable. Returning zero for total commissions. UserId: {}", userId);
        return BigDecimal.ZERO;
    }

    @Override
    public Map<String, BigDecimal> getCommissionsByBusinessUnitId(String businessUnitId) {
        log.warn("Commission Service unavailable. Returning empty summary for business unit. BusinessUnitId: {}", businessUnitId);
        Map<String, BigDecimal> emptySummary = new HashMap<>();
        emptySummary.put("total", BigDecimal.ZERO);
        emptySummary.put("pending", BigDecimal.ZERO);
        emptySummary.put("paid", BigDecimal.ZERO);
        return emptySummary;
    }

    @Override
    public Boolean hasPendingCommissions(String userId) {
        log.warn("Commission Service unavailable. Returning false for pending commissions. UserId: {}", userId);
        return false;
    }

    @Override
    public BigDecimal getCommissionByOrderId(String orderId, String userId) {
        log.warn("Commission Service unavailable. Returning zero for order commission. OrderId: {}, UserId: {}", orderId, userId);
        return BigDecimal.ZERO;
    }
}
