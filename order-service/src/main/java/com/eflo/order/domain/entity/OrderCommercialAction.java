package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_commercial_actions", indexes = {
    @Index(name = "idx_commercial_actions_order", columnList = "order_id"),
    @Index(name = "idx_commercial_actions_code", columnList = "action_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommercialAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(name = "amount_or_percentage", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountOrPercentage;

    @Column(name = "is_percentage")
    @Builder.Default
    private Boolean isPercentage = false;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal vatRate = BigDecimal.valueOf(20.00);

    @Column(name = "vat_type", length = 10)
    private String vatType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
