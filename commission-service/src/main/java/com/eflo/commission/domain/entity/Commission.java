package com.eflo.commission.domain.entity;

import com.eflo.commission.domain.enums.CommissionStatus;
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
import java.util.Map;

@Entity
@Table(name = "commissions")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true, nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_scale_id", nullable = false)
    private CommissionScale commissionScale;

    @Column(name = "scale_code", nullable = false, length = 50)
    private String scaleCode;

    @Column(name = "scale_name", nullable = false)
    private String scaleName;

    @Column(name = "business_unit_id", nullable = false)
    private Long businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "salesperson_id", nullable = false)
    private Long salespersonId;

    @Column(name = "salesperson_name", nullable = false)
    private String salespersonName;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "order_total_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal orderTotalRevenue;

    @Column(name = "order_net_margin", nullable = false, precision = 12, scale = 2)
    private BigDecimal orderNetMargin;

    @Column(name = "vehicle_commission_base", precision = 12, scale = 2)
    private BigDecimal vehicleCommissionBase;

    @Column(name = "vehicle_commission_rate", precision = 5, scale = 2)
    private BigDecimal vehicleCommissionRate;

    @Column(name = "vehicle_commission_excl_tax", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal vehicleCommissionExclTax = BigDecimal.ZERO;

    @Column(name = "vehicle_commission_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal vehicleCommissionTax = BigDecimal.ZERO;

    @Column(name = "vehicle_commission_incl_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal vehicleCommissionInclTax = BigDecimal.ZERO;

    @Column(name = "accessory_commission_excl_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal accessoryCommissionExclTax = BigDecimal.ZERO;

    @Column(name = "accessory_commission_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal accessoryCommissionTax = BigDecimal.ZERO;

    @Column(name = "accessory_commission_incl_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal accessoryCommissionInclTax = BigDecimal.ZERO;

    @Column(name = "service_commission_excl_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceCommissionExclTax = BigDecimal.ZERO;

    @Column(name = "service_commission_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceCommissionTax = BigDecimal.ZERO;

    @Column(name = "service_commission_incl_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceCommissionInclTax = BigDecimal.ZERO;

    @Column(name = "total_commission_excl_tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionExclTax;

    @Column(name = "total_commission_tax", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCommissionTax = BigDecimal.ZERO;

    @Column(name = "total_commission_incl_tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionInclTax;

    @Column(name = "manager_split_enabled")
    @Builder.Default
    private Boolean managerSplitEnabled = false;

    @Column(name = "manager_split_percentage", precision = 5, scale = 2)
    private BigDecimal managerSplitPercentage;

    @Column(name = "manager_commission_excl_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal managerCommissionExclTax = BigDecimal.ZERO;

    @Column(name = "salesperson_net_commission", precision = 12, scale = 2)
    private BigDecimal salespersonNetCommission;

    @Column(name = "calculation_method", nullable = false, length = 30)
    private String calculationMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_details", columnDefinition = "jsonb")
    private Map<String, Object> calculationDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tier_breakdown", columnDefinition = "jsonb")
    private Map<String, Object> tierBreakdown;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private CommissionStatus status = CommissionStatus.PENDING_CALCULATION;

    @Column(name = "calculation_date")
    private LocalDateTime calculationDate;

    @Column(name = "validation_date")
    private LocalDateTime validationDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_batch_id")
    private Long paymentBatchId;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Column(name = "adjustment_reason", length = 500)
    private String adjustmentReason;

    @Column(name = "adjustment_amount", precision = 12, scale = 2)
    private BigDecimal adjustmentAmount;

    @Column(name = "adjusted_by", length = 100)
    private String adjustedBy;

    @Column(name = "adjusted_at")
    private LocalDateTime adjustedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
    public void calculateTotals() {
        this.totalCommissionExclTax = vehicleCommissionExclTax
            .add(accessoryCommissionExclTax)
            .add(serviceCommissionExclTax);

        this.totalCommissionTax = vehicleCommissionTax
            .add(accessoryCommissionTax)
            .add(serviceCommissionTax);

        this.totalCommissionInclTax = totalCommissionExclTax.add(totalCommissionTax);
    }

    public boolean isPaid() {
        return status == CommissionStatus.PAID;
    }

    public boolean isValidated() {
        return status == CommissionStatus.VALIDATED || status == CommissionStatus.PENDING_PAYMENT || isPaid();
    }

    public boolean isPendingPayment() {
        return status == CommissionStatus.VALIDATED || status == CommissionStatus.PENDING_PAYMENT;
    }
}
