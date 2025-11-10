package com.eflo.commission.service;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.entity.CommissionScaleTier;
import com.eflo.commission.domain.model.request.CommissionScaleTierRequest;
import com.eflo.commission.domain.model.request.CreateCommissionScaleRequest;
import com.eflo.commission.domain.model.request.UpdateCommissionScaleRequest;
import com.eflo.commission.domain.repository.CommissionScaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScaleManagementService {

    private final CommissionScaleRepository commissionScaleRepository;

    @Transactional
    public CommissionScale createScale(CreateCommissionScaleRequest request) {
        log.info("Creating commission scale: {}", request.getScaleCode());

        // Check if scale code already exists
        commissionScaleRepository.findByScaleCode(request.getScaleCode())
                .ifPresent(scale -> {
                    throw new IllegalArgumentException(
                            "Commission scale with code already exists: " + request.getScaleCode());
                });

        // Validate dates
        if (request.getValidTo() != null &&
                request.getValidFrom().isAfter(request.getValidTo())) {
            throw new IllegalArgumentException(
                    "Valid from date must be before valid to date");
        }

        // Create scale entity
        CommissionScale scale = CommissionScale.builder()
                .scaleCode(request.getScaleCode())
                .scaleName(request.getScaleName())
                .scaleDescription(request.getScaleDescription())
                .businessUnitId(request.getBusinessUnitId())
                .orderType(request.getOrderType())
                .commissionType(request.getCommissionType())
                .calculationMethod(request.getCalculationMethod())
                .commissionRatePercentage(request.getCommissionRatePercentage())
                .fixedAmount(request.getFixedAmount())
                .minimumMarginRequired(request.getMinimumMarginRequired())
                .minimumRevenueRequired(request.getMinimumRevenueRequired())
                .maximumCommissionAmount(request.getMaximumCommissionAmount())
                .managerSplitEnabled(request.getManagerSplitEnabled() != null
                        ? request.getManagerSplitEnabled() : false)
                .managerSplitPercentage(request.getManagerSplitPercentage())
                .configurationJson(request.getConfigurationJson())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .tiers(new ArrayList<>())
                .build();

        // Add tiers if provided
        if (request.getTiers() != null && !request.getTiers().isEmpty()) {
            for (CommissionScaleTierRequest tierRequest : request.getTiers()) {
                CommissionScaleTier tier = createTierFromRequest(tierRequest);
                scale.addTier(tier);
            }
        }

        // If this is set as default, unset other defaults for same business unit and order type
        if (scale.getIsDefault()) {
            unsetOtherDefaults(scale.getBusinessUnitId(), scale.getOrderType());
        }

        scale = commissionScaleRepository.save(scale);

        log.info("Commission scale created successfully: {} - ID: {}",
                scale.getScaleCode(), scale.getId());

        return scale;
    }

    @Transactional
    public CommissionScale updateScale(Long scaleId, UpdateCommissionScaleRequest request) {
        log.info("Updating commission scale: {}", scaleId);

        CommissionScale scale = commissionScaleRepository.findById(scaleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleId));

        // Update fields if provided
        if (request.getScaleName() != null) {
            scale.setScaleName(request.getScaleName());
        }
        if (request.getScaleDescription() != null) {
            scale.setScaleDescription(request.getScaleDescription());
        }
        if (request.getCommissionType() != null) {
            scale.setCommissionType(request.getCommissionType());
        }
        if (request.getCalculationMethod() != null) {
            scale.setCalculationMethod(request.getCalculationMethod());
        }
        if (request.getCommissionRatePercentage() != null) {
            scale.setCommissionRatePercentage(request.getCommissionRatePercentage());
        }
        if (request.getFixedAmount() != null) {
            scale.setFixedAmount(request.getFixedAmount());
        }
        if (request.getMinimumMarginRequired() != null) {
            scale.setMinimumMarginRequired(request.getMinimumMarginRequired());
        }
        if (request.getMinimumRevenueRequired() != null) {
            scale.setMinimumRevenueRequired(request.getMinimumRevenueRequired());
        }
        if (request.getMaximumCommissionAmount() != null) {
            scale.setMaximumCommissionAmount(request.getMaximumCommissionAmount());
        }
        if (request.getManagerSplitEnabled() != null) {
            scale.setManagerSplitEnabled(request.getManagerSplitEnabled());
        }
        if (request.getManagerSplitPercentage() != null) {
            scale.setManagerSplitPercentage(request.getManagerSplitPercentage());
        }
        if (request.getConfigurationJson() != null) {
            scale.setConfigurationJson(request.getConfigurationJson());
        }
        if (request.getValidFrom() != null) {
            scale.setValidFrom(request.getValidFrom());
        }
        if (request.getValidTo() != null) {
            scale.setValidTo(request.getValidTo());
        }
        if (request.getIsActive() != null) {
            scale.setIsActive(request.getIsActive());
        }
        if (request.getIsDefault() != null) {
            scale.setIsDefault(request.getIsDefault());
            if (request.getIsDefault()) {
                unsetOtherDefaults(scale.getBusinessUnitId(), scale.getOrderType());
            }
        }

        // Update tiers if provided
        if (request.getTiers() != null) {
            // Clear existing tiers
            scale.getTiers().clear();

            // Add new tiers
            for (CommissionScaleTierRequest tierRequest : request.getTiers()) {
                CommissionScaleTier tier = createTierFromRequest(tierRequest);
                scale.addTier(tier);
            }
        }

        // Validate dates
        if (scale.getValidTo() != null &&
                scale.getValidFrom().isAfter(scale.getValidTo())) {
            throw new IllegalArgumentException(
                    "Valid from date must be before valid to date");
        }

        scale = commissionScaleRepository.save(scale);

        log.info("Commission scale updated successfully: {}", scaleId);

        return scale;
    }

    @Transactional
    public void deleteScale(Long scaleId) {
        log.info("Deleting commission scale: {}", scaleId);

        CommissionScale scale = commissionScaleRepository.findById(scaleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleId));

        // Check if scale is in use
        // Note: In production, you'd check if any commissions reference this scale
        // For now, we'll just deactivate instead of hard delete
        scale.setIsActive(false);
        commissionScaleRepository.save(scale);

        log.info("Commission scale deactivated: {}", scaleId);
    }

    @Transactional(readOnly = true)
    public CommissionScale getScaleById(Long scaleId) {
        return commissionScaleRepository.findById(scaleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleId));
    }

    @Transactional(readOnly = true)
    public CommissionScale getScaleByCode(String scaleCode) {
        return commissionScaleRepository.findByScaleCode(scaleCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleCode));
    }

    @Transactional(readOnly = true)
    public Page<CommissionScale> getAllScales(Pageable pageable) {
        return commissionScaleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<CommissionScale> getActiveScales() {
        return commissionScaleRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<CommissionScale> getScalesByBusinessUnit(Long businessUnitId) {
        return commissionScaleRepository.findByBusinessUnitIdAndIsActiveTrue(businessUnitId);
    }

    @Transactional(readOnly = true)
    public List<CommissionScale> getScalesByBusinessUnitAndOrderType(
            Long businessUnitId, String orderType) {
        return commissionScaleRepository
                .findByBusinessUnitIdAndOrderTypeAndIsActiveTrue(businessUnitId, orderType)
                .map(List::of)
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<CommissionScale> getValidScalesForDate(LocalDate date) {
        return commissionScaleRepository.findByIsActiveTrue().stream()
                .filter(scale -> scale.isValidOn(date))
                .toList();
    }

    @Transactional
    public CommissionScale activateScale(Long scaleId) {
        log.info("Activating commission scale: {}", scaleId);

        CommissionScale scale = commissionScaleRepository.findById(scaleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleId));

        scale.setIsActive(true);
        scale = commissionScaleRepository.save(scale);

        log.info("Commission scale activated: {}", scaleId);

        return scale;
    }

    @Transactional
    public CommissionScale deactivateScale(Long scaleId) {
        log.info("Deactivating commission scale: {}", scaleId);

        CommissionScale scale = commissionScaleRepository.findById(scaleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleId));

        scale.setIsActive(false);
        scale = commissionScaleRepository.save(scale);

        log.info("Commission scale deactivated: {}", scaleId);

        return scale;
    }

    @Transactional
    public CommissionScale setAsDefault(Long scaleId) {
        log.info("Setting commission scale as default: {}", scaleId);

        CommissionScale scale = commissionScaleRepository.findById(scaleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission scale not found: " + scaleId));

        // Unset other defaults for same business unit and order type
        unsetOtherDefaults(scale.getBusinessUnitId(), scale.getOrderType());

        scale.setIsDefault(true);
        scale = commissionScaleRepository.save(scale);

        log.info("Commission scale set as default: {}", scaleId);

        return scale;
    }

    private CommissionScaleTier createTierFromRequest(CommissionScaleTierRequest request) {
        return CommissionScaleTier.builder()
                .tierOrder(request.getTierOrder())
                .tierName(request.getTierName())
                .thresholdMin(request.getMinAmount())
                .thresholdMax(request.getMaxAmount())
                .commissionRatePercentage(request.getCommissionRate())
                .fixedAmount(request.getFixedAmount())
                .tierDescription(request.getDescription())
                .build();
    }

    private void unsetOtherDefaults(Long businessUnitId, String orderType) {
        List<CommissionScale> defaultScales = commissionScaleRepository
                .findByBusinessUnitIdAndOrderTypeAndIsActiveTrue(businessUnitId, orderType)
                .stream()
                .filter(CommissionScale::getIsDefault)
                .toList();

        for (CommissionScale scale : defaultScales) {
            scale.setIsDefault(false);
            commissionScaleRepository.save(scale);
        }
    }
}
