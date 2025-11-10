package com.eflo.commission.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_scale_tiers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"commission_scale_id", "tier_order"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionScaleTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_scale_id", nullable = false)
    private CommissionScale commissionScale;

    @Column(name = "tier_order", nullable = false)
    private Integer tierOrder;

    @Column(name = "tier_name", length = 100)
    private String tierName;

    @Column(name = "tier_description")
    private String tierDescription;

    @Column(name = "threshold_min", nullable = false, precision = 12, scale = 2)
    private BigDecimal thresholdMin;

    @Column(name = "threshold_max", precision = 12, scale = 2)
    private BigDecimal thresholdMax;

    @Column(name = "commission_rate_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRatePercentage;

    @Column(name = "fixed_amount", precision = 12, scale = 2)
    private BigDecimal fixedAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Helper methods
    public boolean isInRange(BigDecimal value) {
        if (value.compareTo(thresholdMin) < 0) {
            return false;
        }
        return thresholdMax == null || value.compareTo(thresholdMax) <= 0;
    }

    public boolean isUnlimited() {
        return thresholdMax == null;
    }

    public BigDecimal calculateCommission(BigDecimal baseValue) {
        if (fixedAmount != null) {
            return fixedAmount;
        }
        return baseValue.multiply(commissionRatePercentage).divide(BigDecimal.valueOf(100));
    }
}
