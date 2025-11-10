package com.eflo.commission.web.controller;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.enums.CommissionStatus;
import com.eflo.commission.domain.model.dto.CommissionDTO;
import com.eflo.commission.domain.model.request.AdjustCommissionRequest;
import com.eflo.commission.domain.model.request.CalculateCommissionRequest;
import com.eflo.commission.domain.model.request.CommissionSearchCriteria;
import com.eflo.commission.domain.model.response.ApiResponse;
import com.eflo.commission.domain.model.response.CommissionCalculationResponse;
import com.eflo.commission.domain.model.response.PageResponse;
import com.eflo.commission.domain.repository.CommissionRepository;
import com.eflo.commission.mapper.CommissionMapper;
import com.eflo.commission.service.CommissionCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/commissions")
@RequiredArgsConstructor
@Slf4j
public class CommissionController {

    private final CommissionCalculationService commissionCalculationService;
    private final CommissionRepository commissionRepository;
    private final CommissionMapper commissionMapper;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT')")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CommissionCalculationResponse>> calculateCommission(
            @Valid @RequestBody CalculateCommissionRequest request) {

        log.info("Manual commission calculation requested for order: {}", request.getOrderNumber());

        Commission commission = commissionCalculationService.calculateCommission(request);
        CommissionDTO dto = commissionMapper.toDto(commission);

        CommissionCalculationResponse response = CommissionCalculationResponse.builder()
                .commission(dto)
                .calculated(true)
                .calculationMessage("Commission calculated successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commission calculated successfully", response));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'ACCOUNTANT')")
    @PostMapping("/{commissionId}/recalculate")
    public ResponseEntity<ApiResponse<CommissionDTO>> recalculateCommission(
            @PathVariable Long commissionId) {

        log.info("Recalculating commission: {}", commissionId);

        Commission commission = commissionCalculationService.recalculateCommission(commissionId);
        CommissionDTO dto = commissionMapper.toDto(commission);

        return ResponseEntity.ok(
                ApiResponse.success("Commission recalculated successfully", dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    @PostMapping("/{commissionId}/validate")
    public ResponseEntity<ApiResponse<CommissionDTO>> validateCommission(
            @PathVariable Long commissionId,
            Authentication authentication) {

        String validatedBy = authentication != null ? authentication.getName() : "system";
        log.info("Validating commission: {} by: {}", commissionId, validatedBy);

        Commission commission = commissionCalculationService.validateCommission(
                commissionId, validatedBy);
        CommissionDTO dto = commissionMapper.toDto(commission);

        return ResponseEntity.ok(
                ApiResponse.success("Commission validated successfully", dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    @PostMapping("/{commissionId}/adjust")
    public ResponseEntity<ApiResponse<CommissionDTO>> adjustCommission(
            @PathVariable Long commissionId,
            @Valid @RequestBody AdjustCommissionRequest request,
            Authentication authentication) {

        String adjustedBy = authentication != null ? authentication.getName() : "system";
        log.info("Adjusting commission: {} by: {}", commissionId, adjustedBy);

        Commission commission = commissionCalculationService.adjustCommission(
                commissionId, request, adjustedBy);
        CommissionDTO dto = commissionMapper.toDto(commission);

        return ResponseEntity.ok(
                ApiResponse.success("Commission adjusted successfully", dto));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{commissionId}")
    public ResponseEntity<ApiResponse<Void>> cancelCommission(
            @PathVariable Long commissionId,
            @RequestParam String reason,
            Authentication authentication) {

        String cancelledBy = authentication != null ? authentication.getName() : "system";
        log.info("Cancelling commission: {} by: {}", commissionId, cancelledBy);

        commissionCalculationService.cancelCommission(commissionId, reason, cancelledBy);

        return ResponseEntity.ok(
                ApiResponse.success("Commission cancelled successfully", null));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/{commissionId}")
    public ResponseEntity<ApiResponse<CommissionDTO>> getCommissionById(
            @PathVariable Long commissionId) {

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission not found: " + commissionId));

        CommissionDTO dto = commissionMapper.toDto(commission);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<CommissionDTO>> getCommissionByOrderId(
            @PathVariable Long orderId) {

        Commission commission = commissionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission not found for order: " + orderId));

        CommissionDTO dto = commissionMapper.toDto(commission);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CommissionDTO>>> searchCommissions(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) CommissionStatus status,
            @RequestParam(required = false) LocalDate calculationDateFrom,
            @RequestParam(required = false) LocalDate calculationDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build specification for search
        Specification<Commission> spec = Specification.where(null);

        if (orderId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("orderId"), orderId));
        }
        if (orderNumber != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("orderNumber"), "%" + orderNumber + "%"));
        }
        if (businessUnitId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("businessUnitId"), businessUnitId));
        }
        if (salespersonId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("salespersonId"), salespersonId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (calculationDateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("calculationDate"),
                            calculationDateFrom.atStartOfDay()));
        }
        if (calculationDateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("calculationDate"),
                            calculationDateTo.atTime(23, 59, 59)));
        }

        Page<Commission> commissions = commissionRepository.findAll(spec, pageable);
        Page<CommissionDTO> dtoPage = commissions.map(commissionMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(dtoPage)));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/salesperson/{salespersonId}")
    public ResponseEntity<ApiResponse<List<CommissionDTO>>> getCommissionsBySalesperson(
            @PathVariable Long salespersonId,
            @RequestParam(required = false) CommissionStatus status) {

        List<Commission> commissions;
        if (status != null) {
            commissions = commissionRepository.findBySalespersonIdAndStatus(
                    salespersonId, status);
        } else {
            commissions = commissionRepository.findBySalespersonId(salespersonId);
        }

        List<CommissionDTO> dtos = commissions.stream()
                .map(commissionMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<CommissionDTO>>> getCommissionsByStatus(
            @PathVariable CommissionStatus status) {

        List<Commission> commissions = commissionRepository.findByStatus(status);
        List<CommissionDTO> dtos = commissions.stream()
                .map(commissionMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/pending-payment")
    public ResponseEntity<ApiResponse<List<CommissionDTO>>> getPendingPaymentCommissions(
            @RequestParam Long businessUnitId,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        List<Commission> commissions = commissionCalculationService
                .findPendingPaymentCommissions(businessUnitId, year, month);

        List<CommissionDTO> dtos = commissions.stream()
                .map(commissionMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'ACCOUNTANT', 'VIEWER')")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommissionStats(
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {

        Specification<Commission> spec = Specification.where(null);

        if (businessUnitId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("businessUnitId"), businessUnitId));
        }
        if (dateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("calculationDate"),
                            dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("calculationDate"),
                            dateTo.atTime(23, 59, 59)));
        }

        List<Commission> commissions = commissionRepository.findAll(spec);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", commissions.size());
        stats.put("totalAmount", commissions.stream()
                .map(Commission::getTotalCommissionExclTax)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        stats.put("byStatus", commissions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Commission::getStatus,
                        java.util.stream.Collectors.counting())));

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
