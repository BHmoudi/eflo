package com.eflo.commission.service;

import com.eflo.commission.calculator.CommissionCalculationContext;
import com.eflo.commission.calculator.CommissionCalculationResult;
import com.eflo.commission.calculator.CommissionCalculator;
import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.entity.CommissionHistory;
import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CommissionStatus;
import com.eflo.commission.domain.model.event.CommissionCalculatedEvent;
import com.eflo.commission.domain.model.event.CommissionValidatedEvent;
import com.eflo.commission.domain.model.request.AdjustCommissionRequest;
import com.eflo.commission.domain.model.request.CalculateCommissionRequest;
import com.eflo.commission.domain.repository.CommissionHistoryRepository;
import com.eflo.commission.domain.repository.CommissionRepository;
import com.eflo.commission.domain.repository.CommissionScaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionCalculationService {

    private final CommissionRepository commissionRepository;
    private final CommissionScaleRepository commissionScaleRepository;
    private final CommissionHistoryRepository commissionHistoryRepository;
    private final CommissionCalculator commissionCalculator;
    private final ManagerSplitService managerSplitService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final BigDecimal DEFAULT_TAX_RATE = BigDecimal.valueOf(21);
    private static final String TOPIC_COMMISSION_CALCULATED = "commission.calculated";

    @Transactional
    public Commission calculateCommission(CalculateCommissionRequest request) {
        log.info("Starting commission calculation for order: {}", request.getOrderNumber());

        // Check if commission already exists
        commissionRepository.findByOrderId(request.getOrderId())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Commission already exists for order: " + request.getOrderNumber());
                });

        // Find appropriate commission scale
        CommissionScale scale = findApplicableScale(
                request.getBusinessUnitId(),
                request.getOrderType(),
                request.getScaleCode()
        );

        // Build calculation context
        CommissionCalculationContext context = buildCalculationContext(request, scale);

        // Calculate commission using the calculator
        CommissionCalculationResult result = commissionCalculator.calculate(context);

        // Create commission entity
        Commission commission = createCommissionEntity(request, scale, result);

        // Apply manager split if enabled
        if (scale.getManagerSplitEnabled() && request.getManagerId() != null) {
            managerSplitService.applyManagerSplit(commission, scale.getManagerSplitPercentage());
        }

        // Save commission
        commission = commissionRepository.save(commission);

        // Create history record
        createHistoryRecord(commission, "Commission calculated", null);

        // Publish event
        publishCommissionCalculatedEvent(commission);

        log.info("Commission calculated successfully for order: {} - Commission ID: {}",
                request.getOrderNumber(), commission.getId());

        return commission;
    }

    @Transactional
    public Commission recalculateCommission(Long commissionId) {
        log.info("Recalculating commission: {}", commissionId);

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission not found: " + commissionId));

        if (commission.isPaid()) {
            throw new IllegalStateException(
                    "Cannot recalculate paid commission: " + commissionId);
        }

        // Store old values for history
        BigDecimal oldAmount = commission.getTotalCommissionExclTax();

        // Build recalculation context
        CommissionCalculationContext context = CommissionCalculationContext.builder()
                .orderId(commission.getOrderId())
                .orderNumber(commission.getOrderNumber())
                .orderType(commission.getOrderType())
                .orderTotalRevenue(commission.getOrderTotalRevenue())
                .orderNetMargin(commission.getOrderNetMargin())
                .commissionScale(commission.getCommissionScale())
                .businessUnitId(commission.getBusinessUnitId())
                .businessUnitName(commission.getBusinessUnitName())
                .salespersonId(commission.getSalespersonId())
                .salespersonName(commission.getSalespersonName())
                .managerId(commission.getManagerId())
                .managerName(commission.getManagerName())
                .build();

        // Recalculate
        CommissionCalculationResult result = commissionCalculator.calculate(context);

        // Update commission
        updateCommissionFromResult(commission, result);

        // Reapply manager split if enabled
        if (commission.getManagerSplitEnabled() && commission.getManagerId() != null) {
            managerSplitService.applyManagerSplit(
                    commission,
                    commission.getManagerSplitPercentage()
            );
        }

        // Reset status to pending calculation
        commission.setStatus(CommissionStatus.CALCULATED);
        commission.setCalculationDate(LocalDateTime.now());

        commission = commissionRepository.save(commission);

        // Create history record
        String historyNote = String.format("Commission recalculated. Old amount: %s, New amount: %s",
                oldAmount, commission.getTotalCommissionExclTax());
        createHistoryRecord(commission, "Commission recalculated", historyNote);

        log.info("Commission recalculated successfully: {}", commissionId);

        return commission;
    }

    @Transactional
    public Commission validateCommission(Long commissionId, String validatedBy) {
        log.info("Validating commission: {} by: {}", commissionId, validatedBy);

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission not found: " + commissionId));

        if (commission.getStatus() != CommissionStatus.CALCULATED) {
            throw new IllegalStateException(
                    "Commission must be in CALCULATED status to validate. Current status: " +
                            commission.getStatus());
        }

        commission.setStatus(CommissionStatus.VALIDATED);
        commission.setValidationDate(LocalDateTime.now());

        commission = commissionRepository.save(commission);

        // Create history record
        createHistoryRecord(commission, "Commission validated by " + validatedBy, null);

        // Publish event
        publishCommissionValidatedEvent(commission, validatedBy);

        log.info("Commission validated successfully: {}", commissionId);

        return commission;
    }

    @Transactional
    public Commission adjustCommission(Long commissionId, AdjustCommissionRequest request,
                                       String adjustedBy) {
        log.info("Adjusting commission: {} by: {}", commissionId, adjustedBy);

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission not found: " + commissionId));

        if (commission.isPaid()) {
            throw new IllegalStateException(
                    "Cannot adjust paid commission: " + commissionId);
        }

        BigDecimal oldAmount = commission.getTotalCommissionExclTax();

        // Apply adjustment
        commission.setAdjustmentAmount(request.getAdjustmentAmount());
        commission.setAdjustmentReason(request.getAdjustmentReason());
        commission.setAdjustedBy(adjustedBy);
        commission.setAdjustedAt(LocalDateTime.now());

        // Recalculate totals with adjustment
        BigDecimal adjustedAmount = commission.getTotalCommissionExclTax()
                .add(request.getAdjustmentAmount());
        commission.setTotalCommissionExclTax(adjustedAmount);

        // Recalculate tax
        BigDecimal taxRate = DEFAULT_TAX_RATE.divide(BigDecimal.valueOf(100));
        BigDecimal adjustedTax = adjustedAmount.multiply(taxRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        commission.setTotalCommissionTax(adjustedTax);
        commission.setTotalCommissionInclTax(adjustedAmount.add(adjustedTax));

        if (request.getNotes() != null) {
            commission.setNotes(commission.getNotes() != null
                    ? commission.getNotes() + "\n" + request.getNotes()
                    : request.getNotes());
        }

        // Reset to calculated status requiring re-validation
        commission.setStatus(CommissionStatus.CALCULATED);
        commission.setValidationDate(null);

        commission = commissionRepository.save(commission);

        // Create history record
        String historyNote = String.format(
                "Commission adjusted. Old amount: %s, Adjustment: %s, New amount: %s. Reason: %s",
                oldAmount, request.getAdjustmentAmount(),
                commission.getTotalCommissionExclTax(), request.getAdjustmentReason());
        createHistoryRecord(commission, "Commission adjusted by " + adjustedBy, historyNote);

        log.info("Commission adjusted successfully: {}", commissionId);

        return commission;
    }

    @Transactional
    public void cancelCommission(Long commissionId, String reason, String cancelledBy) {
        log.info("Cancelling commission: {} by: {}", commissionId, cancelledBy);

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Commission not found: " + commissionId));

        if (commission.isPaid()) {
            throw new IllegalStateException(
                    "Cannot cancel paid commission: " + commissionId);
        }

        commission.setStatus(CommissionStatus.CANCELLED);

        commissionRepository.save(commission);

        // Create history record
        createHistoryRecord(commission,
                "Commission cancelled by " + cancelledBy,
                "Reason: " + reason);

        log.info("Commission cancelled successfully: {}", commissionId);
    }

    @Transactional(readOnly = true)
    public List<Commission> findPendingPaymentCommissions(Long businessUnitId,
                                                           Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return commissionRepository.findByBusinessUnitIdAndStatusAndCalculationDateBetween(
                businessUnitId,
                CommissionStatus.VALIDATED,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }

    private CommissionScale findApplicableScale(Long businessUnitId, String orderType,
                                                 String scaleCode) {
        if (scaleCode != null && !scaleCode.isEmpty()) {
            return commissionScaleRepository.findByScaleCode(scaleCode)
                    .filter(scale -> scale.isValidOn(LocalDate.now()))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Commission scale not found or not valid: " + scaleCode));
        }

        // Find default scale for business unit and order type
        return commissionScaleRepository
                .findByBusinessUnitIdAndOrderTypeAndIsActiveTrue(businessUnitId, orderType)
                .stream()
                .filter(scale -> scale.isValidOn(LocalDate.now()))
                .filter(CommissionScale::getIsDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active commission scale found for business unit: " +
                                businessUnitId + " and order type: " + orderType));
    }

    private CommissionCalculationContext buildCalculationContext(
            CalculateCommissionRequest request, CommissionScale scale) {

        BigDecimal taxRate = request.getTaxRate() != null
                ? request.getTaxRate()
                : DEFAULT_TAX_RATE;

        return CommissionCalculationContext.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .orderType(request.getOrderType())
                .orderTotalRevenue(request.getOrderTotalRevenue())
                .orderNetMargin(request.getOrderNetMargin())
                .vehicleMargin(request.getVehicleMargin())
                .accessoryMargin(request.getAccessoryMargin())
                .serviceMargin(request.getServiceMargin())
                .commissionScale(scale)
                .businessUnitId(request.getBusinessUnitId())
                .businessUnitName(request.getBusinessUnitName())
                .salespersonId(request.getSalespersonId())
                .salespersonName(request.getSalespersonName())
                .managerId(request.getManagerId())
                .managerName(request.getManagerName())
                .additionalData(request.getAdditionalData())
                .taxRate(taxRate)
                .build();
    }

    private Commission createCommissionEntity(CalculateCommissionRequest request,
                                               CommissionScale scale,
                                               CommissionCalculationResult result) {
        Commission commission = Commission.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .orderType(request.getOrderType())
                .commissionScale(scale)
                .scaleCode(scale.getScaleCode())
                .scaleName(scale.getScaleName())
                .businessUnitId(request.getBusinessUnitId())
                .businessUnitName(request.getBusinessUnitName())
                .salespersonId(request.getSalespersonId())
                .salespersonName(request.getSalespersonName())
                .managerId(request.getManagerId())
                .managerName(request.getManagerName())
                .orderTotalRevenue(request.getOrderTotalRevenue())
                .orderNetMargin(request.getOrderNetMargin())
                .vehicleCommissionBase(result.getVehicleCommissionBase())
                .vehicleCommissionRate(result.getVehicleCommissionRate())
                .vehicleCommissionExclTax(result.getVehicleCommissionExclTax())
                .vehicleCommissionTax(result.getVehicleCommissionTax())
                .vehicleCommissionInclTax(result.getVehicleCommissionInclTax())
                .accessoryCommissionExclTax(result.getAccessoryCommissionExclTax())
                .accessoryCommissionTax(result.getAccessoryCommissionTax())
                .accessoryCommissionInclTax(result.getAccessoryCommissionInclTax())
                .serviceCommissionExclTax(result.getServiceCommissionExclTax())
                .serviceCommissionTax(result.getServiceCommissionTax())
                .serviceCommissionInclTax(result.getServiceCommissionInclTax())
                .totalCommissionExclTax(result.getTotalCommissionExclTax())
                .totalCommissionTax(result.getTotalCommissionTax())
                .totalCommissionInclTax(result.getTotalCommissionInclTax())
                .calculationMethod(result.getCalculationMethod())
                .calculationDetails(result.getCalculationDetails())
                .tierBreakdown(convertTierBreakdownToMap(result.getTierBreakdown()))
                .status(CommissionStatus.CALCULATED)
                .calculationDate(LocalDateTime.now())
                .managerSplitEnabled(scale.getManagerSplitEnabled())
                .managerSplitPercentage(scale.getManagerSplitPercentage())
                .build();

        commission.calculateTotals();
        return commission;
    }

    private void updateCommissionFromResult(Commission commission,
                                             CommissionCalculationResult result) {
        commission.setVehicleCommissionBase(result.getVehicleCommissionBase());
        commission.setVehicleCommissionRate(result.getVehicleCommissionRate());
        commission.setVehicleCommissionExclTax(result.getVehicleCommissionExclTax());
        commission.setVehicleCommissionTax(result.getVehicleCommissionTax());
        commission.setVehicleCommissionInclTax(result.getVehicleCommissionInclTax());
        commission.setAccessoryCommissionExclTax(result.getAccessoryCommissionExclTax());
        commission.setAccessoryCommissionTax(result.getAccessoryCommissionTax());
        commission.setAccessoryCommissionInclTax(result.getAccessoryCommissionInclTax());
        commission.setServiceCommissionExclTax(result.getServiceCommissionExclTax());
        commission.setServiceCommissionTax(result.getServiceCommissionTax());
        commission.setServiceCommissionInclTax(result.getServiceCommissionInclTax());
        commission.setTotalCommissionExclTax(result.getTotalCommissionExclTax());
        commission.setTotalCommissionTax(result.getTotalCommissionTax());
        commission.setTotalCommissionInclTax(result.getTotalCommissionInclTax());
        commission.setCalculationDetails(result.getCalculationDetails());
        commission.setTierBreakdown(convertTierBreakdownToMap(result.getTierBreakdown()));

        commission.calculateTotals();
    }

    private Map<String, Object> convertTierBreakdownToMap(List<CommissionCalculationResult.TierCalculationDetail> tierBreakdown) {
        if (tierBreakdown == null || tierBreakdown.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("tiers", tierBreakdown);
        return breakdown;
    }

    private void createHistoryRecord(Commission commission, String action, String notes) {
        CommissionHistory history = CommissionHistory.builder()
                .commission(commission)
                .changeType(action)
                .previousStatus(commission.getStatus().name())
                .newStatus(commission.getStatus().name())
                .previousAmount(commission.getTotalCommissionExclTax())
                .newAmount(commission.getTotalCommissionExclTax())
                .changeDescription(notes)
                .build();

        commissionHistoryRepository.save(history);
    }

    private void publishCommissionCalculatedEvent(Commission commission) {
        try {
            CommissionCalculatedEvent event = CommissionCalculatedEvent.builder()
                    .commissionId(commission.getId())
                    .orderId(commission.getOrderId())
                    .orderNumber(commission.getOrderNumber())
                    .salespersonId(commission.getSalespersonId())
                    .salespersonName(commission.getSalespersonName())
                    .totalCommissionExclTax(commission.getTotalCommissionExclTax())
                    .totalCommissionInclTax(commission.getTotalCommissionInclTax())
                    .scaleCode(commission.getScaleCode())
                    .calculationMethod(commission.getCalculationMethod())
                    .calculationDate(commission.getCalculationDate())
                    .eventId(UUID.randomUUID().toString())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(TOPIC_COMMISSION_CALCULATED, event);
            log.debug("Published CommissionCalculatedEvent for commission: {}", commission.getId());
        } catch (Exception e) {
            log.error("Failed to publish CommissionCalculatedEvent", e);
        }
    }

    private void publishCommissionValidatedEvent(Commission commission, String validatedBy) {
        try {
            CommissionValidatedEvent event = CommissionValidatedEvent.builder()
                    .commissionId(commission.getId())
                    .orderId(commission.getOrderId())
                    .orderNumber(commission.getOrderNumber())
                    .salespersonId(commission.getSalespersonId())
                    .salespersonName(commission.getSalespersonName())
                    .totalCommissionExclTax(commission.getTotalCommissionExclTax())
                    .totalCommissionInclTax(commission.getTotalCommissionInclTax())
                    .validatedBy(validatedBy)
                    .validationDate(commission.getValidationDate())
                    .eventId(UUID.randomUUID().toString())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("commission.validated", event);
            log.debug("Published CommissionValidatedEvent for commission: {}", commission.getId());
        } catch (Exception e) {
            log.error("Failed to publish CommissionValidatedEvent", e);
        }
    }
}
