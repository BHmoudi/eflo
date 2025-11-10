package com.eflo.order.service;

import com.eflo.order.domain.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Recalculates all pricing fields for an order
     */
    @Transactional
    public void recalculateOrderPricing(Order order) {
        log.debug("Recalculating pricing for order: {}", order.getOrderNumber());

        // Calculate component totals
        BigDecimal optionsTotal = calculateOptionsTotal(order);
        BigDecimal accessoriesTotal = calculateAccessoriesTotal(order);
        BigDecimal servicesTotal = calculateContractServicesTotal(order);
        BigDecimal aidsTotal = calculateAidsTotal(order);
        BigDecimal supplementsTotal = calculateSupplementsTotal(order);

        order.setOptionsTotal(optionsTotal);
        order.setAccessoriesTotal(accessoriesTotal);
        order.setServicesTotal(servicesTotal);
        order.setAidsTotal(aidsTotal);
        order.setSupplementsTotal(supplementsTotal);

        // Calculate subtotal: base + options + accessories + services + supplements
        BigDecimal subtotal = order.getBasePrice()
                .add(optionsTotal)
                .add(accessoriesTotal)
                .add(servicesTotal)
                .add(supplementsTotal)
                .setScale(SCALE, ROUNDING_MODE);

        order.setSubtotal(subtotal);

        // Apply discount
        BigDecimal totalBeforeTax = applyDiscount(order, subtotal);

        // Subtract aids (financial aids reduce the price)
        totalBeforeTax = totalBeforeTax.subtract(aidsTotal).setScale(SCALE, ROUNDING_MODE);
        order.setTotalBeforeTax(totalBeforeTax);

        // Calculate VAT
        BigDecimal vatRate = order.getVatRate() != null ? order.getVatRate() : new BigDecimal("20.00");
        BigDecimal vatAmount = totalBeforeTax
                .multiply(vatRate.divide(new BigDecimal("100"), SCALE, ROUNDING_MODE))
                .setScale(SCALE, ROUNDING_MODE);

        order.setVatAmount(vatAmount);

        // Calculate total amount
        BigDecimal totalAmount = totalBeforeTax.add(vatAmount).setScale(SCALE, ROUNDING_MODE);
        order.setTotalAmount(totalAmount);

        // Calculate margins
        calculateMargins(order);

        log.debug("Pricing recalculation complete. Total: {}, Margin: {}%",
                 totalAmount, order.getMarginPercentage());
    }

    /**
     * Calculate total price of all options
     */
    private BigDecimal calculateOptionsTotal(Order order) {
        return order.getOptions().stream()
                .map(OrderOption::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate total price of all accessories
     */
    private BigDecimal calculateAccessoriesTotal(Order order) {
        return order.getAccessories().stream()
                .map(OrderAccessory::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate total price of all contract services
     */
    private BigDecimal calculateContractServicesTotal(Order order) {
        return order.getContractServices().stream()
                .map(OrderContractService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate total of all approved aids
     */
    private BigDecimal calculateAidsTotal(Order order) {
        return order.getAids().stream()
                .filter(aid -> aid.getApprovalStatus() == OrderAid.ApprovalStatus.APPROVED)
                .map(OrderAid::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate total of all supplements
     */
    private BigDecimal calculateSupplementsTotal(Order order) {
        return order.getSupplements().stream()
                .map(OrderSupplement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Apply discount to subtotal
     */
    private BigDecimal applyDiscount(Order order, BigDecimal subtotal) {
        BigDecimal discountAmount = order.getDiscountAmount() != null ?
                order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal discountPercentage = order.getDiscountPercentage() != null ?
                order.getDiscountPercentage() : BigDecimal.ZERO;

        // Apply percentage discount first
        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentageDiscount = subtotal
                    .multiply(discountPercentage.divide(new BigDecimal("100"), SCALE, ROUNDING_MODE))
                    .setScale(SCALE, ROUNDING_MODE);
            discountAmount = discountAmount.add(percentageDiscount);
        }

        order.setDiscountAmount(discountAmount);

        return subtotal.subtract(discountAmount).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate gross margin, net margin, and margin percentage
     */
    private void calculateMargins(Order order) {
        // Calculate total cost
        BigDecimal totalCost = calculateTotalCost(order);
        order.setCostPrice(totalCost);

        // Gross Margin = Total Amount (including VAT) - Total Cost
        BigDecimal grossMargin = order.getTotalAmount().subtract(totalCost).setScale(SCALE, ROUNDING_MODE);
        order.setGrossMargin(grossMargin);

        // Net Margin = Total Before Tax - Total Cost
        BigDecimal netMargin = order.getTotalBeforeTax().subtract(totalCost).setScale(SCALE, ROUNDING_MODE);
        order.setNetMargin(netMargin);

        // Margin Percentage = (Net Margin / Total Amount) * 100
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginPercentage = netMargin
                    .divide(order.getTotalAmount(), SCALE, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"))
                    .setScale(SCALE, ROUNDING_MODE);
            order.setMarginPercentage(marginPercentage);
        } else {
            order.setMarginPercentage(BigDecimal.ZERO);
        }
    }

    /**
     * Calculate total cost of the order
     */
    private BigDecimal calculateTotalCost(Order order) {
        BigDecimal baseCost = order.getCostPrice() != null ? order.getCostPrice() : BigDecimal.ZERO;

        BigDecimal optionsCost = order.getOptions().stream()
                .map(option -> option.getCost() != null ? option.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal accessoriesCost = order.getAccessories().stream()
                .map(accessory -> accessory.getTotalCost() != null ? accessory.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal servicesCost = order.getContractServices().stream()
                .map(service -> service.getCost() != null ? service.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return baseCost
                .add(optionsCost)
                .add(accessoriesCost)
                .add(servicesCost)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Apply a discount to an order
     */
    @Transactional
    public void applyDiscount(Order order, BigDecimal discountAmount, BigDecimal discountPercentage) {
        if (discountAmount != null) {
            order.setDiscountAmount(discountAmount);
        }
        if (discountPercentage != null) {
            order.setDiscountPercentage(discountPercentage);
        }
        recalculateOrderPricing(order);
    }
}
