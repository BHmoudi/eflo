package com.eflo.order.mapper;

import com.eflo.order.domain.entity.*;
import com.eflo.order.domain.model.dto.*;
import com.eflo.order.domain.model.request.CreateOrderRequest;
import com.eflo.order.domain.model.request.UpdateOrderRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request, Long userId) {
        return Order.builder()
                .orderType(request.getOrderType())
                .customerId(request.getCustomerId())
                .businessUnitId(request.getBusinessUnitId())
                .salespersonId(request.getSalespersonId())
                .vehicleId(request.getVehicleId())
                .vin(request.getVin())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .trim(request.getTrim())
                .colorExterior(request.getColorExterior())
                .colorInterior(request.getColorInterior())
                .basePrice(request.getBasePrice())
                .vatRate(request.getVatRate() != null ? request.getVatRate() : new BigDecimal("20.00"))
                .tradeinVehicleId(request.getTradeinVehicleId())
                .tradeinValue(request.getTradeinValue() != null ? request.getTradeinValue() : BigDecimal.ZERO)
                .financingType(request.getFinancingType())
                .financingInstitution(request.getFinancingInstitution())
                .financingAmount(request.getFinancingAmount())
                .financingTermMonths(request.getFinancingTermMonths())
                .financingInterestRate(request.getFinancingInterestRate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .deliveryLocation(request.getDeliveryLocation())
                .deliveryNotes(request.getDeliveryNotes())
                .notes(request.getNotes())
                .internalComments(request.getInternalComments())
                .status(Order.OrderStatus.DRAFT)
                .createdByUserId(userId)
                .build();
    }

    public void updateEntityFromRequest(Order order, UpdateOrderRequest request) {
        if (request.getVehicleId() != null) order.setVehicleId(request.getVehicleId());
        if (request.getVin() != null) order.setVin(request.getVin());
        if (request.getMake() != null) order.setMake(request.getMake());
        if (request.getModel() != null) order.setModel(request.getModel());
        if (request.getYear() != null) order.setYear(request.getYear());
        if (request.getTrim() != null) order.setTrim(request.getTrim());
        if (request.getColorExterior() != null) order.setColorExterior(request.getColorExterior());
        if (request.getColorInterior() != null) order.setColorInterior(request.getColorInterior());
        if (request.getBasePrice() != null) order.setBasePrice(request.getBasePrice());
        if (request.getTradeinVehicleId() != null) order.setTradeinVehicleId(request.getTradeinVehicleId());
        if (request.getTradeinValue() != null) order.setTradeinValue(request.getTradeinValue());
        if (request.getFinancingType() != null) order.setFinancingType(request.getFinancingType());
        if (request.getFinancingInstitution() != null) order.setFinancingInstitution(request.getFinancingInstitution());
        if (request.getFinancingAmount() != null) order.setFinancingAmount(request.getFinancingAmount());
        if (request.getFinancingTermMonths() != null) order.setFinancingTermMonths(request.getFinancingTermMonths());
        if (request.getFinancingInterestRate() != null) order.setFinancingInterestRate(request.getFinancingInterestRate());
        if (request.getExpectedDeliveryDate() != null) order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        if (request.getDeliveryLocation() != null) order.setDeliveryLocation(request.getDeliveryLocation());
        if (request.getDeliveryNotes() != null) order.setDeliveryNotes(request.getDeliveryNotes());
        if (request.getNotes() != null) order.setNotes(request.getNotes());
        if (request.getInternalComments() != null) order.setInternalComments(request.getInternalComments());
    }

    public OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .customerId(order.getCustomerId())
                .businessUnitId(order.getBusinessUnitId())
                .salespersonId(order.getSalespersonId())
                .vehicleId(order.getVehicleId())
                .vin(order.getVin())
                .make(order.getMake())
                .model(order.getModel())
                .year(order.getYear())
                .trim(order.getTrim())
                .colorExterior(order.getColorExterior())
                .colorInterior(order.getColorInterior())
                .basePrice(order.getBasePrice())
                .optionsTotal(order.getOptionsTotal())
                .accessoriesTotal(order.getAccessoriesTotal())
                .servicesTotal(order.getServicesTotal())
                .aidsTotal(order.getAidsTotal())
                .supplementsTotal(order.getSupplementsTotal())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .discountPercentage(order.getDiscountPercentage())
                .totalBeforeTax(order.getTotalBeforeTax())
                .vatRate(order.getVatRate())
                .vatAmount(order.getVatAmount())
                .totalAmount(order.getTotalAmount())
                .costPrice(order.getCostPrice())
                .grossMargin(order.getGrossMargin())
                .netMargin(order.getNetMargin())
                .marginPercentage(order.getMarginPercentage())
                .tradeinVehicleId(order.getTradeinVehicleId())
                .tradeinValue(order.getTradeinValue())
                .financingType(order.getFinancingType())
                .financingInstitution(order.getFinancingInstitution())
                .financingAmount(order.getFinancingAmount())
                .financingTermMonths(order.getFinancingTermMonths())
                .financingInterestRate(order.getFinancingInterestRate())
                .workflowInstanceId(order.getWorkflowInstanceId())
                .workflowCurrentState(order.getWorkflowCurrentState())
                .status(order.getStatus())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .deliveryLocation(order.getDeliveryLocation())
                .deliveryNotes(order.getDeliveryNotes())
                .notes(order.getNotes())
                .internalComments(order.getInternalComments())
                .options(toOptionDTOs(order.getOptions()))
                .accessories(toAccessoryDTOs(order.getAccessories()))
                .contractServices(toContractServiceDTOs(order.getContractServices()))
                .aids(toAidDTOs(order.getAids()))
                .supplements(toSupplementDTOs(order.getSupplements()))
                .createdAt(order.getCreatedAt())
                .createdByUserId(order.getCreatedByUserId())
                .updatedAt(order.getUpdatedAt())
                .updatedByUserId(order.getUpdatedByUserId())
                .version(order.getVersion())
                .build();
    }

    public List<OrderDTO> toDTOs(List<Order> orders) {
        return orders.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OrderOptionDTO toOptionDTO(OrderOption option) {
        return OrderOptionDTO.builder()
                .id(option.getId())
                .orderId(option.getOrder().getId())
                .optionCode(option.getOptionCode())
                .optionName(option.getOptionName())
                .optionCategory(option.getOptionCategory())
                .description(option.getDescription())
                .price(option.getPrice())
                .cost(option.getCost())
                .isMandatory(option.getIsMandatory())
                .isFactoryOption(option.getIsFactoryOption())
                .build();
    }

    public List<OrderOptionDTO> toOptionDTOs(List<OrderOption> options) {
        return options.stream().map(this::toOptionDTO).collect(Collectors.toList());
    }

    public OrderAccessoryDTO toAccessoryDTO(OrderAccessory accessory) {
        return OrderAccessoryDTO.builder()
                .id(accessory.getId())
                .orderId(accessory.getOrder().getId())
                .accessoryCode(accessory.getAccessoryCode())
                .accessoryName(accessory.getAccessoryName())
                .accessoryCategory(accessory.getAccessoryCategory())
                .description(accessory.getDescription())
                .quantity(accessory.getQuantity())
                .unitPrice(accessory.getUnitPrice())
                .totalPrice(accessory.getTotalPrice())
                .costPerUnit(accessory.getCostPerUnit())
                .totalCost(accessory.getTotalCost())
                .supplier(accessory.getSupplier())
                .installationRequired(accessory.getInstallationRequired())
                .build();
    }

    public List<OrderAccessoryDTO> toAccessoryDTOs(List<OrderAccessory> accessories) {
        return accessories.stream().map(this::toAccessoryDTO).collect(Collectors.toList());
    }

    public OrderContractServiceDTO toContractServiceDTO(OrderContractService service) {
        return OrderContractServiceDTO.builder()
                .id(service.getId())
                .orderId(service.getOrder().getId())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .serviceType(service.getServiceType())
                .description(service.getDescription())
                .price(service.getPrice())
                .cost(service.getCost())
                .durationMonths(service.getDurationMonths())
                .coverageDetails(service.getCoverageDetails())
                .provider(service.getProvider())
                .build();
    }

    public List<OrderContractServiceDTO> toContractServiceDTOs(List<OrderContractService> services) {
        return services.stream().map(this::toContractServiceDTO).collect(Collectors.toList());
    }

    public OrderAidDTO toAidDTO(OrderAid aid) {
        return OrderAidDTO.builder()
                .id(aid.getId())
                .orderId(aid.getOrder().getId())
                .aidCode(aid.getAidCode())
                .aidName(aid.getAidName())
                .aidType(aid.getAidType())
                .description(aid.getDescription())
                .amount(aid.getAmount())
                .provider(aid.getProvider())
                .eligibilityCriteria(aid.getEligibilityCriteria())
                .approvalStatus(aid.getApprovalStatus())
                .approvedByUserId(aid.getApprovedByUserId())
                .approvedAt(aid.getApprovedAt())
                .build();
    }

    public List<OrderAidDTO> toAidDTOs(List<OrderAid> aids) {
        return aids.stream().map(this::toAidDTO).collect(Collectors.toList());
    }

    public OrderSupplementDTO toSupplementDTO(OrderSupplement supplement) {
        return OrderSupplementDTO.builder()
                .id(supplement.getId())
                .orderId(supplement.getOrder().getId())
                .supplementCode(supplement.getSupplementCode())
                .supplementName(supplement.getSupplementName())
                .supplementType(supplement.getSupplementType())
                .description(supplement.getDescription())
                .amount(supplement.getAmount())
                .reason(supplement.getReason())
                .approvedByUserId(supplement.getApprovedByUserId())
                .approvedAt(supplement.getApprovedAt())
                .build();
    }

    public List<OrderSupplementDTO> toSupplementDTOs(List<OrderSupplement> supplements) {
        return supplements.stream().map(this::toSupplementDTO).collect(Collectors.toList());
    }
}
