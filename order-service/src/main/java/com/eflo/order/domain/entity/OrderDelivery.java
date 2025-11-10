package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "order_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "delivery_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time_start")
    private LocalTime scheduledTimeStart;

    @Column(name = "scheduled_time_end")
    private LocalTime scheduledTimeEnd;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Column(name = "delivery_address_line1")
    private String deliveryAddressLine1;

    @Column(name = "delivery_address_line2")
    private String deliveryAddressLine2;

    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;

    @Column(name = "delivery_state", length = 100)
    private String deliveryState;

    @Column(name = "delivery_postal_code", length = 20)
    private String deliveryPostalCode;

    @Column(name = "delivery_country", length = 100)
    private String deliveryCountry;

    @Column(name = "delivery_contact_name")
    private String deliveryContactName;

    @Column(name = "delivery_contact_phone", length = 50)
    private String deliveryContactPhone;

    @Column(name = "delivery_contact_email")
    private String deliveryContactEmail;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Column(name = "delivery_status", length = 50)
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus = DeliveryStatus.SCHEDULED;

    @Column(name = "delivered_by_user_id")
    private Long deliveredByUserId;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "signature_captured")
    private Boolean signatureCaptured = false;

    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    private Long createdByUserId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DeliveryType {
        PICKUP,
        HOME_DELIVERY,
        DEALER_DELIVERY
    }

    public enum DeliveryStatus {
        SCHEDULED,
        CONFIRMED,
        IN_TRANSIT,
        DELIVERED,
        FAILED,
        CANCELLED
    }
}
