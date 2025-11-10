package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "order_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    // Customer & Business Info
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "business_unit_id", nullable = false)
    private Long businessUnitId;

    @Column(name = "salesperson_id", nullable = false)
    private Long salespersonId;

    // Vehicle Info
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vin", length = 17)
    private String vin;

    @Column(name = "make", length = 100)
    private String make;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "trim", length = 100)
    private String trim;

    @Column(name = "color_exterior", length = 50)
    private String colorExterior;

    @Column(name = "color_interior", length = 50)
    private String colorInterior;

    // MOVE-specific vehicle fields
    @Column(name = "semi_clair_model", length = 50)
    private String semiClairModel;

    @Column(name = "semi_clair_version", length = 50)
    private String semiClairVersion;

    @Column(name = "co2_level")
    private Integer co2Level;

    @Column(name = "body_type", length = 50)
    private String bodyType;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    // MOVE-specific business metadata
    @Column(name = "product_type", length = 10)
    private String productType;

    @Column(name = "tariff_number")
    private Integer tariffNumber;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "family_barcode", length = 50)
    private String familyBarcode;

    @Column(name = "distrinet_code", length = 50)
    private String distrinetCode;

    @Column(name = "distrinet_export_number", length = 50)
    private String distrinetExportNumber;

    // Business organization fields
    @Column(name = "establishment_name")
    private String establishmentName;

    @Column(name = "identifiant_rr", length = 50)
    private String identifiantRr;

    @Column(name = "rattachement", length = 50)
    private String rattachement;

    @Column(name = "seller_type", length = 1)
    private String sellerType;

    // Customer denormalized fields
    @Column(name = "customer_first_name")
    private String customerFirstName;

    @Column(name = "customer_last_name")
    private String customerLastName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "customer_postal_code", length = 10)
    private String customerPostalCode;

    @Column(name = "customer_city", length = 100)
    private String customerCity;

    @Column(name = "customer_civility", length = 10)
    private String customerCivility;

    @Column(name = "customer_type", length = 10)
    private String customerType;

    @Column(name = "customer_sa")
    private Integer customerSa;

    @Column(name = "customer_commercial_name")
    private String customerCommercialName;

    // Salesperson denormalized fields
    @Column(name = "salesperson_ipn", length = 50)
    private String salespersonIpn;

    @Column(name = "salesperson_name")
    private String salespersonName;

    // Pricing Fields
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "options_total", precision = 15, scale = 2)
    private BigDecimal optionsTotal = BigDecimal.ZERO;

    @Column(name = "accessories_total", precision = 15, scale = 2)
    private BigDecimal accessoriesTotal = BigDecimal.ZERO;

    @Column(name = "services_total", precision = 15, scale = 2)
    private BigDecimal servicesTotal = BigDecimal.ZERO;

    @Column(name = "aids_total", precision = 15, scale = 2)
    private BigDecimal aidsTotal = BigDecimal.ZERO;

    @Column(name = "supplements_total", precision = 15, scale = 2)
    private BigDecimal supplementsTotal = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "total_before_tax", precision = 15, scale = 2)
    private BigDecimal totalBeforeTax = BigDecimal.ZERO;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate = new BigDecimal("20.00");

    @Column(name = "vat_amount", precision = 15, scale = 2)
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Margin Fields
    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "gross_margin", precision = 15, scale = 2)
    private BigDecimal grossMargin = BigDecimal.ZERO;

    @Column(name = "net_margin", precision = 15, scale = 2)
    private BigDecimal netMargin = BigDecimal.ZERO;

    @Column(name = "margin_percentage", precision = 5, scale = 2)
    private BigDecimal marginPercentage = BigDecimal.ZERO;

    // Trade-in
    @Column(name = "tradein_vehicle_id")
    private Long tradeinVehicleId;

    @Column(name = "tradein_value", precision = 15, scale = 2)
    private BigDecimal tradeinValue = BigDecimal.ZERO;

    // Financing (metadata only)
    @Column(name = "financing_type", length = 50)
    private String financingType;

    @Column(name = "financing_institution", length = 100)
    private String financingInstitution;

    @Column(name = "financing_amount", precision = 15, scale = 2)
    private BigDecimal financingAmount;

    @Column(name = "financing_term_months")
    private Integer financingTermMonths;

    @Column(name = "financing_interest_rate", precision = 5, scale = 2)
    private BigDecimal financingInterestRate;

    // MOVE-specific financing fields
    @Column(name = "financing_contract_diac", length = 100)
    private String financingContractDiac;

    @Column(name = "financing_with_deposit")
    private Boolean financingWithDeposit;

    @Column(name = "financing_number_of_services")
    private Integer financingNumberOfServices;

    // Aids
    @Column(name = "aide_rpe", precision = 10, scale = 2)
    private BigDecimal aideRpe = BigDecimal.ZERO;

    @Column(name = "aide_autres", precision = 10, scale = 2)
    private BigDecimal aideAutres = BigDecimal.ZERO;

    // Trade-in flag
    @Column(name = "has_trade_in")
    private Boolean hasTradeIn = false;

    // Workflow
    @Column(name = "workflow_instance_id")
    private Long workflowInstanceId;

    @Column(name = "workflow_current_state", length = 100)
    private String workflowCurrentState;

    // Status & Lifecycle
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.DRAFT;

    // Delivery
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "delivery_location")
    private String deliveryLocation;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    // Notes & Comments
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_comments", columnDefinition = "TEXT")
    private String internalComments;

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    private Long createdByUserId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_user_id")
    private Long deletedByUserId;

    @Version
    @Column(name = "version")
    private Integer version = 0;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderAccessory> accessories = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderContractService> contractServices = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderAid> aids = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderSupplement> supplements = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDelivery> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderCommercialAction> commercialActions = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderTradeIn> tradeIns = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderAssignedCondition> assignedConditions = new ArrayList<>();

    // Helper methods
    public void addOption(OrderOption option) {
        options.add(option);
        option.setOrder(this);
    }

    public void addAccessory(OrderAccessory accessory) {
        accessories.add(accessory);
        accessory.setOrder(this);
    }

    public void addContractService(OrderContractService service) {
        contractServices.add(service);
        service.setOrder(this);
    }

    public void addAid(OrderAid aid) {
        aids.add(aid);
        aid.setOrder(this);
    }

    public void addSupplement(OrderSupplement supplement) {
        supplements.add(supplement);
        supplement.setOrder(this);
    }

    public void addDelivery(OrderDelivery delivery) {
        deliveries.add(delivery);
        delivery.setOrder(this);
    }

    public void addHistory(OrderHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setOrder(this);
    }

    public void addCommercialAction(OrderCommercialAction action) {
        commercialActions.add(action);
        action.setOrder(this);
    }

    public void addTradeIn(OrderTradeIn tradeIn) {
        tradeIns.add(tradeIn);
        tradeIn.setOrder(this);
    }

    public enum OrderType {
        VN,  // New Vehicle
        VO,  // Used Vehicle
        EVO  // Evolution
    }

    public enum OrderStatus {
        DRAFT,
        PENDING,
        CONFIRMED,
        IN_PRODUCTION,
        READY_FOR_DELIVERY,
        DELIVERED,
        INVOICED,
        CANCELLED,
        ON_HOLD
    }
}
