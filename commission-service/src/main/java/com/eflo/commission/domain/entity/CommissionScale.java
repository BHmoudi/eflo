package com.eflo.commission.domain.entity;

import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "commission_scales")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionScale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scale_code", unique = true, nullable = false, length = 50)
    private String scaleCode;

    @Column(name = "scale_name", nullable = false)
    private String scaleName;

    @Column(name = "scale_description", columnDefinition = "TEXT")
    private String scaleDescription;

    @Column(name = "business_unit_id", nullable = false)
    private Long businessUnitId;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", nullable = false, length = 20)
    private CommissionType commissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 30)
    private CalculationMethod calculationMethod;

    @Column(name = "commission_rate_percentage", precision = 5, scale = 2)
    private BigDecimal commissionRatePercentage;

    @Column(name = "fixed_amount", precision = 12, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "minimum_margin_required", precision = 12, scale = 2)
    private BigDecimal minimumMarginRequired;

    @Column(name = "minimum_revenue_required", precision = 12, scale = 2)
    private BigDecimal minimumRevenueRequired;

    @Column(name = "maximum_commission_amount", precision = 12, scale = 2)
    private BigDecimal maximumCommissionAmount;

    @Column(name = "manager_split_enabled")
    private Boolean managerSplitEnabled = false;

    @Column(name = "manager_split_percentage", precision = 5, scale = 2)
    private BigDecimal managerSplitPercentage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration_json", columnDefinition = "jsonb")
    private Map<String, Object> configurationJson;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "commissionScale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("tierOrder ASC")
    @Builder.Default
    private List<CommissionScaleTier> tiers = new ArrayList<>();

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

    @Version
    @Column(name = "version")
    private Integer version;

    // Helper methods
    public void addTier(CommissionScaleTier tier) {
        tiers.add(tier);
        tier.setCommissionScale(this);
    }

    public void removeTier(CommissionScaleTier tier) {
        tiers.remove(tier);
        tier.setCommissionScale(null);
    }

    public boolean isValidOn(LocalDate date) {
        if (!isActive) {
            return false;
        }
        if (validFrom != null && date.isBefore(validFrom)) {
            return false;
        }
        return validTo == null || !date.isAfter(validTo);
    }

    public boolean meetsMinimumRequirements(BigDecimal margin, BigDecimal revenue) {
        boolean meetsMargin = minimumMarginRequired == null || margin.compareTo(minimumMarginRequired) >= 0;
        boolean meetsRevenue = minimumRevenueRequired == null || revenue.compareTo(minimumRevenueRequired) >= 0;
        return meetsMargin && meetsRevenue;
    }
}
