package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_trade_ins", indexes = {
    @Index(name = "idx_trade_ins_order", columnList = "order_id"),
    @Index(name = "idx_trade_ins_vin", columnList = "vin"),
    @Index(name = "idx_trade_ins_status", columnList = "status"),
    @Index(name = "idx_trade_ins_owner", columnList = "owner_customer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTradeIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Owner information
    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    // Vehicle type
    @Column(name = "vehicle_type", length = 10)
    private String vehicleType; // VP, VU

    // Vehicle information
    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "version")
    private String version;

    @Column(name = "body_type", length = 50)
    private String bodyType;

    // Identification
    @Column(name = "vin", length = 17)
    private String vin;

    @Column(name = "registration_number", length = 20)
    private String registrationNumber;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "first_registration_date")
    private LocalDate firstRegistrationDate;

    // Technical details
    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(name = "engine_power")
    private Integer enginePower;

    @Column(name = "transmission", length = 50)
    private String transmission;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "origin", length = 10)
    private String origin;

    @Column(name = "number_of_doors")
    private Integer numberOfDoors;

    // Colors
    @Column(name = "exterior_color", length = 100)
    private String exteriorColor;

    @Column(name = "interior_color", length = 100)
    private String interiorColor;

    // Pricing (all in EUR, excl tax)
    @Column(name = "estimated_value", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal estimatedValue = BigDecimal.ZERO;

    @Column(name = "overestimation", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overestimation = BigDecimal.ZERO;

    @Column(name = "final_value", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal finalValue = BigDecimal.ZERO;

    @Column(name = "engagement_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal engagementAmount = BigDecimal.ZERO;

    @Column(name = "conversion_bonus", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal conversionBonus = BigDecimal.ZERO;

    @Column(name = "trade_in_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tradeInBalance = BigDecimal.ZERO;

    // Status
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, EVALUATED, ACCEPTED, COMPLETED, CANCELLED

    // Evaluation details
    @Column(name = "evaluation_date")
    private LocalDate evaluationDate;

    @Column(name = "evaluated_by_user_id")
    private Long evaluatedByUserId;

    @Column(name = "evaluation_notes", columnDefinition = "TEXT")
    private String evaluationNotes;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
