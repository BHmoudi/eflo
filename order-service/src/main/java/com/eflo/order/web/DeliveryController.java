package com.eflo.order.web;

import com.eflo.order.domain.entity.OrderDelivery;
import com.eflo.order.domain.model.dto.OrderDeliveryDTO;
import com.eflo.order.service.DeliveryService;
import com.eflo.order.mapper.DeliveryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.eflo.order.security.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "APIs for managing order deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;

    @PostMapping("/order/{orderId}/schedule")
    @Operation(summary = "Schedule a delivery for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDeliveryDTO> scheduleDelivery(
            @PathVariable Long orderId,
            @RequestParam OrderDelivery.DeliveryType deliveryType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeEnd,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) String instructions,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = SecurityUtils.getUserId(jwt);
        OrderDelivery delivery = deliveryService.scheduleDelivery(
                orderId, deliveryType, scheduledDate, timeStart, timeEnd,
                address, city, state, postalCode, country,
                contactName, contactPhone, contactEmail, instructions, userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryMapper.toDTO(delivery));
    }

    @PutMapping("/{deliveryId}")
    @Operation(summary = "Update delivery details")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDeliveryDTO> updateDelivery(
            @PathVariable Long deliveryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeEnd,
            @RequestParam(required = false) String instructions,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = SecurityUtils.getUserId(jwt);
        OrderDelivery delivery = deliveryService.updateDelivery(
                deliveryId, scheduledDate, timeStart, timeEnd, instructions, userId
        );
        return ResponseEntity.ok(deliveryMapper.toDTO(delivery));
    }

    @PostMapping("/{deliveryId}/complete")
    @Operation(summary = "Complete a delivery")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDeliveryDTO> completeDelivery(
            @PathVariable Long deliveryId,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "false") boolean signatureCaptured,
            @RequestParam(required = false) String signatureData,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = SecurityUtils.getUserId(jwt);
        OrderDelivery delivery = deliveryService.completeDelivery(
                deliveryId, userId, notes, signatureCaptured, signatureData
        );
        return ResponseEntity.ok(deliveryMapper.toDTO(delivery));
    }

    @PostMapping("/{deliveryId}/cancel")
    @Operation(summary = "Cancel a delivery")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDeliveryDTO> cancelDelivery(
            @PathVariable Long deliveryId,
            @RequestParam String reason,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = SecurityUtils.getUserId(jwt);
        OrderDelivery delivery = deliveryService.cancelDelivery(deliveryId, reason, userId);
        return ResponseEntity.ok(deliveryMapper.toDTO(delivery));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get deliveries for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDeliveryDTO>> getDeliveriesForOrder(@PathVariable Long orderId) {
        List<OrderDelivery> deliveries = deliveryService.getDeliveriesForOrder(orderId);
        return ResponseEntity.ok(deliveries.stream().map(deliveryMapper::toDTO).toList());
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get deliveries by scheduled date")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDeliveryDTO>> getDeliveriesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<OrderDelivery> deliveries = deliveryService.getDeliveriesByDate(date);
        return ResponseEntity.ok(deliveries.stream().map(deliveryMapper::toDTO).toList());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get deliveries by status")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDeliveryDTO>> getDeliveriesByStatus(@PathVariable OrderDelivery.DeliveryStatus status) {
        List<OrderDelivery> deliveries = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(deliveries.stream().map(deliveryMapper::toDTO).toList());
    }

    // User extraction centralized in SecurityUtils
}
