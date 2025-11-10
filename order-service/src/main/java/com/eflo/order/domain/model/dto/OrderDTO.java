package com.eflo.order.domain.model.dto;

import com.eflo.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;
    private String orderNumber;
    private Order.OrderType orderType;

    // Customer & Business
    private Long customerId;
    private Long businessUnitId;
    private Long salespersonId;

    // Vehicle
    private Long vehicleId;
    private String vin;
    private String make;
    private String model;
    private Integer year;
    private String trim;
    private String colorExterior;
    private String colorInterior;

    // Pricing
    private BigDecimal basePrice;
    private BigDecimal optionsTotal;
    private BigDecimal accessoriesTotal;
    private BigDecimal servicesTotal;
    private BigDecimal aidsTotal;
    private BigDecimal supplementsTotal;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal totalBeforeTax;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;

    // Margin
    private BigDecimal costPrice;
    private BigDecimal grossMargin;
    private BigDecimal netMargin;
    private BigDecimal marginPercentage;

    // Trade-in
    private Long tradeinVehicleId;
    private BigDecimal tradeinValue;

    // Financing
    private String financingType;
    private String financingInstitution;
    private BigDecimal financingAmount;
    private Integer financingTermMonths;
    private BigDecimal financingInterestRate;

    // Workflow
    private Long workflowInstanceId;
    private String workflowCurrentState;

    // Status
    private Order.OrderStatus status;

    // Delivery
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String deliveryLocation;
    private String deliveryNotes;

    // Notes
    private String notes;
    private String internalComments;

    // Child collections
    private List<OrderOptionDTO> options;
    private List<OrderAccessoryDTO> accessories;
    private List<OrderContractServiceDTO> contractServices;
    private List<OrderAidDTO> aids;
    private List<OrderSupplementDTO> supplements;

    // Audit
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private LocalDateTime updatedAt;
    private Long updatedByUserId;
    private Integer version;
}
