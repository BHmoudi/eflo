package com.eflo.commission.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "commission_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commission_id", nullable = false)
    private Long commissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_id", insertable = false, updatable = false)
    private Commission commission;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "change_description", columnDefinition = "TEXT")
    private String changeDescription;

    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    @Column(name = "new_status", length = 30)
    private String newStatus;

    @Column(name = "previous_amount", precision = 12, scale = 2)
    private BigDecimal previousAmount;

    @Column(name = "new_amount", precision = 12, scale = 2)
    private BigDecimal newAmount;

    @Column(name = "amount_difference", precision = 12, scale = 2)
    private BigDecimal amountDifference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "change_details", columnDefinition = "jsonb")
    private Map<String, Object> changeDetails;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "changed_at", nullable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;
}
