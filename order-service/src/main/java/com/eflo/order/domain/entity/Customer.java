package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customers_email", columnList = "email"),
    @Index(name = "idx_customers_type", columnList = "customer_type"),
    @Index(name = "idx_customers_last_name", columnList = "last_name"),
    @Index(name = "idx_customers_external_id", columnList = "external_customer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_type", nullable = false, length = 10)
    private String customerType; // PA or PRO

    @Column(name = "civility", length = 10)
    private String civility;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "commercial_name")
    private String commercialName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "siret", length = 14)
    private String siret;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_mobile", length = 20)
    private String phoneMobile;

    @Column(name = "phone_landline", length = 20)
    private String phoneLandline;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 2)
    @Builder.Default
    private String country = "FR";

    @Column(name = "sa")
    private Integer sa;

    @Column(name = "external_customer_id", length = 100)
    private String externalCustomerId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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
