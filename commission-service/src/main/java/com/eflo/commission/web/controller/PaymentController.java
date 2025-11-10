package com.eflo.commission.web.controller;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.entity.CommissionPayment;
import com.eflo.commission.domain.enums.PaymentStatus;
import com.eflo.commission.domain.model.dto.CommissionDTO;
import com.eflo.commission.domain.model.dto.CommissionPaymentDTO;
import com.eflo.commission.domain.model.request.CreatePaymentBatchRequest;
import com.eflo.commission.domain.model.response.ApiResponse;
import com.eflo.commission.domain.model.response.PageResponse;
import com.eflo.commission.domain.model.response.PaymentBatchResponse;
import com.eflo.commission.mapper.CommissionMapper;
import com.eflo.commission.mapper.CommissionPaymentMapper;
import com.eflo.commission.service.PaymentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentManagementService paymentManagementService;
    private final CommissionPaymentMapper paymentMapper;
    private final CommissionMapper commissionMapper;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT')")
    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<PaymentBatchResponse>> createPaymentBatch(
            @Valid @RequestBody CreatePaymentBatchRequest request) {

        log.info("Creating payment batch: {}", request.getBatchName());

        CommissionPayment payment = paymentManagementService.createPaymentBatch(request);
        CommissionPaymentDTO dto = paymentMapper.toDto(payment);

        PaymentBatchResponse response = PaymentBatchResponse.builder()
                .payment(dto)
                .includedCommissionIds(request.getCommissionIds())
                .successCount(payment.getTotalCommissions())
                .failureCount(0)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment batch created successfully", response));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    @PostMapping("/batches/{paymentId}/process")
    public ResponseEntity<ApiResponse<CommissionPaymentDTO>> processPayment(
            @PathVariable Long paymentId) {

        log.info("Processing payment batch: {}", paymentId);

        CommissionPayment payment = paymentManagementService.processPayment(paymentId);
        CommissionPaymentDTO dto = paymentMapper.toDto(payment);

        return ResponseEntity.ok(
                ApiResponse.success("Payment batch processed successfully", dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    @PostMapping("/batches/{paymentId}/approve")
    public ResponseEntity<ApiResponse<CommissionPaymentDTO>> approvePayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        String approvedBy = authentication != null ? authentication.getName() : "system";
        log.info("Approving payment batch: {} by: {}", paymentId, approvedBy);

        CommissionPayment payment = paymentManagementService.approvePayment(
                paymentId, approvedBy);
        CommissionPaymentDTO dto = paymentMapper.toDto(payment);

        return ResponseEntity.ok(
                ApiResponse.success("Payment batch approved successfully", dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    @PostMapping("/batches/{paymentId}/reject")
    public ResponseEntity<ApiResponse<CommissionPaymentDTO>> rejectPayment(
            @PathVariable Long paymentId,
            @RequestParam String reason,
            Authentication authentication) {

        String rejectedBy = authentication != null ? authentication.getName() : "system";
        log.info("Rejecting payment batch: {} by: {}", paymentId, rejectedBy);

        CommissionPayment payment = paymentManagementService.rejectPayment(
                paymentId, reason, rejectedBy);
        CommissionPaymentDTO dto = paymentMapper.toDto(payment);

        return ResponseEntity.ok(
                ApiResponse.success("Payment batch rejected successfully", dto));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/batches/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam String reason,
            Authentication authentication) {

        String cancelledBy = authentication != null ? authentication.getName() : "system";
        log.info("Cancelling payment batch: {} by: {}", paymentId, cancelledBy);

        paymentManagementService.cancelPayment(paymentId, reason, cancelledBy);

        return ResponseEntity.ok(
                ApiResponse.success("Payment batch cancelled successfully", null));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches/{paymentId}")
    public ResponseEntity<ApiResponse<CommissionPaymentDTO>> getPaymentById(
            @PathVariable Long paymentId) {

        CommissionPayment payment = paymentManagementService.getPaymentById(paymentId);
        CommissionPaymentDTO dto = paymentMapper.toDto(payment);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches/number/{batchNumber}")
    public ResponseEntity<ApiResponse<CommissionPaymentDTO>> getPaymentByBatchNumber(
            @PathVariable String batchNumber) {

        CommissionPayment payment = paymentManagementService
                .getPaymentByBatchNumber(batchNumber);
        CommissionPaymentDTO dto = paymentMapper.toDto(payment);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<PageResponse<CommissionPaymentDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CommissionPayment> payments = paymentManagementService.getAllPayments(pageable);
        Page<CommissionPaymentDTO> dtoPage = payments.map(paymentMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(dtoPage)));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches/status/{status}")
    public ResponseEntity<ApiResponse<List<CommissionPaymentDTO>>> getPaymentsByStatus(
            @PathVariable PaymentStatus status) {

        List<CommissionPayment> payments = paymentManagementService.getPaymentsByStatus(status);
        List<CommissionPaymentDTO> dtos = payments.stream()
                .map(paymentMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches/period/{year}/{month}")
    public ResponseEntity<ApiResponse<List<CommissionPaymentDTO>>> getPaymentsByYearAndMonth(
            @PathVariable Integer year,
            @PathVariable Integer month) {

        List<CommissionPayment> payments = paymentManagementService
                .getPaymentsByYearAndMonth(year, month);
        List<CommissionPaymentDTO> dtos = payments.stream()
                .map(paymentMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches/business-unit/{businessUnitId}")
    public ResponseEntity<ApiResponse<List<CommissionPaymentDTO>>> getPaymentsByBusinessUnit(
            @PathVariable Long businessUnitId) {

        List<CommissionPayment> payments = paymentManagementService
                .getPaymentsByBusinessUnit(businessUnitId);
        List<CommissionPaymentDTO> dtos = payments.stream()
                .map(paymentMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/batches/{paymentId}/commissions")
    public ResponseEntity<ApiResponse<List<CommissionDTO>>> getCommissionsInPaymentBatch(
            @PathVariable Long paymentId) {

        List<Commission> commissions = paymentManagementService
                .getCommissionsInPaymentBatch(paymentId);
        List<CommissionDTO> dtos = commissions.stream()
                .map(commissionMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
