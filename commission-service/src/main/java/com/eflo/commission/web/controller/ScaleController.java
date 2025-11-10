package com.eflo.commission.web.controller;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.model.dto.CommissionScaleDTO;
import com.eflo.commission.domain.model.request.CreateCommissionScaleRequest;
import com.eflo.commission.domain.model.request.UpdateCommissionScaleRequest;
import com.eflo.commission.domain.model.response.ApiResponse;
import com.eflo.commission.domain.model.response.PageResponse;
import com.eflo.commission.mapper.CommissionScaleMapper;
import com.eflo.commission.service.ScaleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scales")
@RequiredArgsConstructor
@Slf4j
public class ScaleController {

    private final ScaleManagementService scaleManagementService;
    private final CommissionScaleMapper scaleMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> createScale(
            @Valid @RequestBody CreateCommissionScaleRequest request) {

        log.info("Creating commission scale: {}", request.getScaleCode());

        CommissionScale scale = scaleManagementService.createScale(request);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commission scale created successfully", dto));
    }

    @PutMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> updateScale(
            @PathVariable Long scaleId,
            @Valid @RequestBody UpdateCommissionScaleRequest request) {

        log.info("Updating commission scale: {}", scaleId);

        CommissionScale scale = scaleManagementService.updateScale(scaleId, request);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.ok(
                ApiResponse.success("Commission scale updated successfully", dto));
    }

    @DeleteMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<Void>> deleteScale(@PathVariable Long scaleId) {
        log.info("Deleting commission scale: {}", scaleId);

        scaleManagementService.deleteScale(scaleId);

        return ResponseEntity.ok(
                ApiResponse.success("Commission scale deleted successfully", null));
    }

    @GetMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> getScaleById(
            @PathVariable Long scaleId) {

        CommissionScale scale = scaleManagementService.getScaleById(scaleId);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/code/{scaleCode}")
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> getScaleByCode(
            @PathVariable String scaleCode) {

        CommissionScale scale = scaleManagementService.getScaleByCode(scaleCode);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CommissionScaleDTO>>> getAllScales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CommissionScale> scales = scaleManagementService.getAllScales(pageable);
        Page<CommissionScaleDTO> dtoPage = scales.map(scaleMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(dtoPage)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CommissionScaleDTO>>> getActiveScales() {
        List<CommissionScale> scales = scaleManagementService.getActiveScales();
        List<CommissionScaleDTO> dtos = scales.stream()
                .map(scaleMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/business-unit/{businessUnitId}")
    public ResponseEntity<ApiResponse<List<CommissionScaleDTO>>> getScalesByBusinessUnit(
            @PathVariable Long businessUnitId) {

        List<CommissionScale> scales = scaleManagementService
                .getScalesByBusinessUnit(businessUnitId);
        List<CommissionScaleDTO> dtos = scales.stream()
                .map(scaleMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/business-unit/{businessUnitId}/order-type/{orderType}")
    public ResponseEntity<ApiResponse<List<CommissionScaleDTO>>> getScalesByBusinessUnitAndOrderType(
            @PathVariable Long businessUnitId,
            @PathVariable String orderType) {

        List<CommissionScale> scales = scaleManagementService
                .getScalesByBusinessUnitAndOrderType(businessUnitId, orderType);
        List<CommissionScaleDTO> dtos = scales.stream()
                .map(scaleMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/valid")
    public ResponseEntity<ApiResponse<List<CommissionScaleDTO>>> getValidScalesForDate(
            @RequestParam(required = false) LocalDate date) {

        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<CommissionScale> scales = scaleManagementService.getValidScalesForDate(effectiveDate);
        List<CommissionScaleDTO> dtos = scales.stream()
                .map(scaleMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PatchMapping("/{scaleId}/activate")
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> activateScale(
            @PathVariable Long scaleId) {

        log.info("Activating commission scale: {}", scaleId);

        CommissionScale scale = scaleManagementService.activateScale(scaleId);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.ok(
                ApiResponse.success("Commission scale activated successfully", dto));
    }

    @PatchMapping("/{scaleId}/deactivate")
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> deactivateScale(
            @PathVariable Long scaleId) {

        log.info("Deactivating commission scale: {}", scaleId);

        CommissionScale scale = scaleManagementService.deactivateScale(scaleId);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.ok(
                ApiResponse.success("Commission scale deactivated successfully", dto));
    }

    @PatchMapping("/{scaleId}/set-default")
    public ResponseEntity<ApiResponse<CommissionScaleDTO>> setAsDefault(
            @PathVariable Long scaleId) {

        log.info("Setting commission scale as default: {}", scaleId);

        CommissionScale scale = scaleManagementService.setAsDefault(scaleId);
        CommissionScaleDTO dto = scaleMapper.toDto(scale);

        return ResponseEntity.ok(
                ApiResponse.success("Commission scale set as default successfully", dto));
    }
}
