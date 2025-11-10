package com.eflo.order.service;

import com.eflo.order.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @InjectMocks
    private PricingService pricingService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .basePrice(new BigDecimal("25000.00"))
                .vatRate(new BigDecimal("20.00"))
                .options(new ArrayList<>())
                .accessories(new ArrayList<>())
                .contractServices(new ArrayList<>())
                .aids(new ArrayList<>())
                .supplements(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should calculate basic pricing without extras")
    void testBasicPricing() {
        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(order.getTotalBeforeTax()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(order.getVatAmount()).isEqualByComparingTo(new BigDecimal("5000.00")); // 20% of 25000
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("30000.00"));
    }

    @Test
    @DisplayName("Should calculate pricing with options")
    void testPricingWithOptions() {
        // Given
        OrderOption option1 = OrderOption.builder()
                .price(new BigDecimal("1500.00"))
                .cost(new BigDecimal("1000.00"))
                .build();
        OrderOption option2 = OrderOption.builder()
                .price(new BigDecimal("2500.00"))
                .cost(new BigDecimal("2000.00"))
                .build();

        order.addOption(option1);
        order.addOption(option2);

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        assertThat(order.getOptionsTotal()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("29000.00")); // 25000 + 4000
        assertThat(order.getVatAmount()).isEqualByComparingTo(new BigDecimal("5800.00")); // 20% of 29000
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("34800.00"));
    }

    @Test
    @DisplayName("Should calculate pricing with accessories")
    void testPricingWithAccessories() {
        // Given
        OrderAccessory accessory = OrderAccessory.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .totalPrice(new BigDecimal("1000.00"))
                .build();

        order.addAccessory(accessory);

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        assertThat(order.getAccessoriesTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("26000.00"));
    }

    @Test
    @DisplayName("Should apply discount amount")
    void testDiscountAmount() {
        // Given
        order.setDiscountAmount(new BigDecimal("2000.00"));

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        assertThat(order.getTotalBeforeTax()).isEqualByComparingTo(new BigDecimal("23000.00")); // 25000 - 2000
        assertThat(order.getVatAmount()).isEqualByComparingTo(new BigDecimal("4600.00")); // 20% of 23000
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("27600.00"));
    }

    @Test
    @DisplayName("Should apply discount percentage")
    void testDiscountPercentage() {
        // Given
        order.setDiscountPercentage(new BigDecimal("10.00")); // 10% discount

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("2500.00"); // 10% of 25000
        assertThat(order.getDiscountAmount()).isEqualByComparingTo(expectedDiscount);
        assertThat(order.getTotalBeforeTax()).isEqualByComparingTo(new BigDecimal("22500.00"));
        assertThat(order.getVatAmount()).isEqualByComparingTo(new BigDecimal("4500.00"));
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("27000.00"));
    }

    @Test
    @DisplayName("Should apply approved aids")
    void testApprovedAids() {
        // Given
        OrderAid approvedAid = OrderAid.builder()
                .amount(new BigDecimal("1000.00"))
                .approvalStatus(OrderAid.ApprovalStatus.APPROVED)
                .build();
        OrderAid pendingAid = OrderAid.builder()
                .amount(new BigDecimal("500.00"))
                .approvalStatus(OrderAid.ApprovalStatus.PENDING)
                .build();

        order.addAid(approvedAid);
        order.addAid(pendingAid);

        // When
        pricingService.recalculateOrderPricing(order);

        // Then - only approved aid should be applied
        assertThat(order.getAidsTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(order.getTotalBeforeTax()).isEqualByComparingTo(new BigDecimal("24000.00")); // 25000 - 1000
    }

    @Test
    @DisplayName("Should calculate pricing with supplements")
    void testPricingWithSupplements() {
        // Given
        OrderSupplement supplement = OrderSupplement.builder()
                .amount(new BigDecimal("500.00"))
                .build();

        order.addSupplement(supplement);

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        assertThat(order.getSupplementsTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("25500.00"));
    }

    @Test
    @DisplayName("Should calculate margins correctly")
    void testMarginCalculation() {
        // Given
        order.setCostPrice(new BigDecimal("20000.00"));

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        // Total before tax: 25000
        // Total amount (with VAT): 30000
        // Cost: 20000
        // Gross margin: 30000 - 20000 = 10000
        // Net margin: 25000 - 20000 = 5000
        // Margin %: (5000 / 30000) * 100 = 16.666... rounds to 17.00

        assertThat(order.getGrossMargin()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(order.getNetMargin()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(order.getMarginPercentage()).isEqualByComparingTo(new BigDecimal("17.00"));
    }

    @Test
    @DisplayName("Should calculate complex pricing with all components")
    void testComplexPricing() {
        // Given
        order.addOption(OrderOption.builder().price(new BigDecimal("2000.00")).cost(new BigDecimal("1500.00")).build());
        order.addAccessory(OrderAccessory.builder().totalPrice(new BigDecimal("1000.00")).totalCost(new BigDecimal("700.00")).build());
        order.addContractService(OrderContractService.builder().price(new BigDecimal("3000.00")).cost(new BigDecimal("2000.00")).build());
        order.addSupplement(OrderSupplement.builder().amount(new BigDecimal("500.00")).build());

        OrderAid aid = OrderAid.builder()
                .amount(new BigDecimal("1500.00"))
                .approvalStatus(OrderAid.ApprovalStatus.APPROVED)
                .build();
        order.addAid(aid);

        order.setDiscountPercentage(new BigDecimal("5.00")); // 5% discount
        order.setCostPrice(new BigDecimal("18000.00"));

        // When
        pricingService.recalculateOrderPricing(order);

        // Then
        // Subtotal: 25000 + 2000 + 1000 + 3000 + 500 = 31500
        // Discount (5%): 1575
        // After discount: 31500 - 1575 = 29925
        // After aids: 29925 - 1500 = 28425
        // VAT (20%): 5685
        // Total: 34110

        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("31500.00"));
        assertThat(order.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("1575.00"));
        assertThat(order.getTotalBeforeTax()).isEqualByComparingTo(new BigDecimal("28425.00"));
        assertThat(order.getVatAmount()).isEqualByComparingTo(new BigDecimal("5685.00"));
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("34110.00"));

        // Total cost: 18000 + 1500 + 700 + 2000 = 22200
        // Net margin: 28425 - 22200 = 6225
        assertThat(order.getCostPrice()).isEqualByComparingTo(new BigDecimal("22200.00"));
        assertThat(order.getNetMargin()).isEqualByComparingTo(new BigDecimal("6225.00"));
    }
}
