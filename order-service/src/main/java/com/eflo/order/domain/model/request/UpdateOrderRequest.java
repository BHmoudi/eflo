package com.eflo.order.domain.model.request;

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
public class UpdateOrderRequest {

    private Long vehicleId;
    private String vin;
    private String make;
    private String model;
    private Integer year;
    private String trim;
    private String colorExterior;
    private String colorInterior;
    private BigDecimal basePrice;
    private Long tradeinVehicleId;
    private BigDecimal tradeinValue;
    private String financingType;
    private String financingInstitution;
    private BigDecimal financingAmount;
    private Integer financingTermMonths;
    private BigDecimal financingInterestRate;
    private LocalDate expectedDeliveryDate;
    private String deliveryLocation;
    private String deliveryNotes;
    private String notes;
    private String internalComments;
}
