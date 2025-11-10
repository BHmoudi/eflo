package com.eflo.order.domain.model.request;

import com.eflo.order.domain.entity.Order;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Order type is required")
    private Order.OrderType orderType;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Business unit ID is required")
    private Long businessUnitId;

    @NotNull(message = "Salesperson ID is required")
    private Long salespersonId;

    // Vehicle Info (optional for draft)
    private Long vehicleId;

    @Size(max = 17, message = "VIN must be at most 17 characters")
    private String vin;

    @Size(max = 100)
    private String make;

    @Size(max = 100)
    private String model;

    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @Size(max = 100)
    private String trim;

    @Size(max = 50)
    private String colorExterior;

    @Size(max = 50)
    private String colorInterior;

    // Pricing
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be positive")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", message = "VAT rate must be positive")
    @DecimalMax(value = "100.0", message = "VAT rate must not exceed 100")
    private BigDecimal vatRate;

    // Trade-in
    private Long tradeinVehicleId;
    private BigDecimal tradeinValue;

    // Financing
    private String financingType;
    private String financingInstitution;
    private BigDecimal financingAmount;
    private Integer financingTermMonths;
    private BigDecimal financingInterestRate;

    // Delivery
    private LocalDate expectedDeliveryDate;
    private String deliveryLocation;
    private String deliveryNotes;

    // Notes
    private String notes;
    private String internalComments;
}
