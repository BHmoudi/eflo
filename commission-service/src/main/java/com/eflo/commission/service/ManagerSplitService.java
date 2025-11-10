package com.eflo.commission.service;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.entity.CommissionScale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating manager split on commissions
 */
@Service
@Slf4j
public class ManagerSplitService {

    /**
     * Calculate manager split for a commission
     */
    public ManagerSplitResult calculateSplit(Commission commission) {
        if (!commission.getManagerSplitEnabled()) {
            log.debug("Manager split not enabled for commission {}", commission.getId());
            return ManagerSplitResult.noSplit(commission.getTotalCommissionExclTax());
        }

        BigDecimal totalCommission = commission.getTotalCommissionExclTax();
        BigDecimal managerPercentage = commission.getManagerSplitPercentage();

        if (managerPercentage == null || managerPercentage.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("Manager split percentage is zero for commission {}", commission.getId());
            return ManagerSplitResult.noSplit(totalCommission);
        }

        BigDecimal managerCommission = totalCommission
                .multiply(managerPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal salespersonCommission = totalCommission.subtract(managerCommission);

        log.debug("Split calculated - Total: {}, Manager ({}%): {}, Salesperson: {}",
                totalCommission, managerPercentage, managerCommission, salespersonCommission);

        return ManagerSplitResult.builder()
                .totalCommission(totalCommission)
                .managerPercentage(managerPercentage)
                .managerCommission(managerCommission)
                .salespersonCommission(salespersonCommission)
                .splitApplied(true)
                .build();
    }

    /**
     * Calculate split based on scale configuration
     */
    public ManagerSplitResult calculateSplit(BigDecimal totalCommission, CommissionScale scale) {
        if (!Boolean.TRUE.equals(scale.getManagerSplitEnabled())) {
            return ManagerSplitResult.noSplit(totalCommission);
        }

        BigDecimal managerPercentage = scale.getManagerSplitPercentage();
        if (managerPercentage == null || managerPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return ManagerSplitResult.noSplit(totalCommission);
        }

        BigDecimal managerCommission = totalCommission
                .multiply(managerPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal salespersonCommission = totalCommission.subtract(managerCommission);

        return ManagerSplitResult.builder()
                .totalCommission(totalCommission)
                .managerPercentage(managerPercentage)
                .managerCommission(managerCommission)
                .salespersonCommission(salespersonCommission)
                .splitApplied(true)
                .build();
    }

    /**
     * Apply split to commission entity
     */
    public void applySplit(Commission commission, ManagerSplitResult split) {
        commission.setManagerSplitEnabled(split.isSplitApplied());
        commission.setManagerSplitPercentage(split.getManagerPercentage());
        commission.setManagerCommissionExclTax(split.getManagerCommission());
        commission.setSalespersonNetCommission(split.getSalespersonCommission());

        log.debug("Applied split to commission {} - Manager: {}, Salesperson: {}",
                commission.getId(), split.getManagerCommission(), split.getSalespersonCommission());
    }

    /**
     * Apply manager split with total commission amount
     */
    public void applyManagerSplit(Commission commission, BigDecimal totalCommission) {
        ManagerSplitResult split = calculateSplit(commission);
        applySplit(commission, split);
    }

    /**
     * Manager split calculation result
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerSplitResult {
        private BigDecimal totalCommission;
        private BigDecimal managerPercentage;
        private BigDecimal managerCommission;
        private BigDecimal salespersonCommission;
        private boolean splitApplied;

        public static ManagerSplitResult noSplit(BigDecimal totalCommission) {
            return ManagerSplitResult.builder()
                    .totalCommission(totalCommission)
                    .managerPercentage(BigDecimal.ZERO)
                    .managerCommission(BigDecimal.ZERO)
                    .salespersonCommission(totalCommission)
                    .splitApplied(false)
                    .build();
        }
    }
}
