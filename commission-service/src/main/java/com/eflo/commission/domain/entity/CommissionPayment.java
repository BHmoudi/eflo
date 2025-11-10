package com.eflo.commission.domain.entity;

import com.eflo.commission.domain.enums.PaymentStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_payments")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number", unique = true, nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "batch_name", nullable = false)
    private String batchName;

    @Column(name = "payment_year", nullable = false)
    private Integer paymentYear;

    @Column(name = "payment_month", nullable = false)
    private Integer paymentMonth;

    @Column(name = "payment_period", nullable = false, length = 20)
    private String paymentPeriod;

    @Column(name = "business_unit_id")
    private Long businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "total_commissions", nullable = false)
    @Builder.Default
    private Integer totalCommissions = 0;

    @Column(name = "total_amount_excl_tax", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmountExclTax = BigDecimal.ZERO;

    @Column(name = "total_amount_tax", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmountTax = BigDecimal.ZERO;

    @Column(name = "total_amount_incl_tax", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmountInclTax = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_file_path", length = 500)
    private String paymentFilePath;

    @Column(name = "scheduled_payment_date")
    private LocalDate scheduledPaymentDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

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

    // Helper methods
    public boolean isProcessed() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public void incrementCommissionCount() {
        this.totalCommissions++;
    }

    public void addCommissionAmount(BigDecimal amountExclTax, BigDecimal tax) {
        this.totalAmountExclTax = this.totalAmountExclTax.add(amountExclTax);
        this.totalAmountTax = this.totalAmountTax.add(tax);
        this.totalAmountInclTax = this.totalAmountInclTax.add(amountExclTax).add(tax);
    }
}
