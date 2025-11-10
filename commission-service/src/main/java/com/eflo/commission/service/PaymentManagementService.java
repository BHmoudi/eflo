package com.eflo.commission.service;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.entity.CommissionPayment;
import com.eflo.commission.domain.enums.CommissionStatus;
import com.eflo.commission.domain.enums.PaymentStatus;
import com.eflo.commission.domain.model.event.CommissionPaidEvent;
import com.eflo.commission.domain.model.event.PaymentBatchCreatedEvent;
import com.eflo.commission.domain.model.request.CreatePaymentBatchRequest;
import com.eflo.commission.domain.repository.CommissionPaymentRepository;
import com.eflo.commission.domain.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentManagementService {

    private final CommissionPaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_PAYMENT_CREATED = "payment.batch.created";
    private static final String TOPIC_COMMISSION_PAID = "commission.paid";

    @Transactional
    public CommissionPayment createPaymentBatch(CreatePaymentBatchRequest request) {
        log.info("Creating payment batch: {} for {}/{}", request.getBatchName(),
                request.getPaymentYear(), request.getPaymentMonth());

        // Validate commission IDs
        List<Commission> commissions = validateCommissionsForPayment(request.getCommissionIds());

        // Generate batch number
        String batchNumber = generateBatchNumber(request.getPaymentYear(), request.getPaymentMonth());

        // Create payment batch
        CommissionPayment payment = CommissionPayment.builder()
                .batchNumber(batchNumber)
                .batchName(request.getBatchName())
                .paymentYear(request.getPaymentYear())
                .paymentMonth(request.getPaymentMonth())
                .paymentPeriod(String.format("%d-%02d",
                        request.getPaymentYear(), request.getPaymentMonth()))
                .businessUnitId(request.getBusinessUnitId())
                .businessUnitName(request.getBusinessUnitName())
                .totalCommissions(0)
                .totalAmountExclTax(BigDecimal.ZERO)
                .totalAmountTax(BigDecimal.ZERO)
                .totalAmountInclTax(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .scheduledPaymentDate(request.getScheduledPaymentDate() != null
                        ? request.getScheduledPaymentDate()
                        : LocalDate.now().plusDays(7))
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        // Associate commissions with payment batch
        for (Commission commission : commissions) {
            associateCommissionWithPayment(commission, payment);
            payment.incrementCommissionCount();
            payment.addCommissionAmount(
                    commission.getTotalCommissionExclTax(),
                    commission.getTotalCommissionTax()
            );
        }

        payment = paymentRepository.save(payment);

        // Publish event
        publishPaymentBatchCreatedEvent(payment);

        log.info("Payment batch created successfully: {} - ID: {}, Total commissions: {}",
                batchNumber, payment.getId(), payment.getTotalCommissions());

        return payment;
    }

    @Transactional
    public CommissionPayment processPayment(Long paymentId) {
        log.info("Processing payment batch: {}", paymentId);

        CommissionPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment batch not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment batch is not in PENDING status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setProcessingStartedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        try {
            // Get all commissions in this payment batch
            List<Commission> commissions = commissionRepository
                    .findByPaymentBatchId(payment.getId());

            // Mark commissions as paid
            for (Commission commission : commissions) {
                markCommissionAsPaid(commission, payment);
            }

            // Update payment status
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProcessedDate(LocalDateTime.now());
            payment.setCompletedDate(LocalDateTime.now());
            payment.setProcessingCompletedAt(LocalDateTime.now());

            payment = paymentRepository.save(payment);

            log.info("Payment batch processed successfully: {}", paymentId);

        } catch (Exception e) {
            log.error("Error processing payment batch: {}", paymentId, e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProcessingError(e.getMessage());
            payment.setProcessingCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw new RuntimeException("Failed to process payment batch", e);
        }

        return payment;
    }

    @Transactional
    public CommissionPayment approvePayment(Long paymentId, String approvedBy) {
        log.info("Approving payment batch: {} by: {}", paymentId, approvedBy);

        CommissionPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment batch not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment batch must be in PENDING status to approve");
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment = paymentRepository.save(payment);

        log.info("Payment batch approved: {}", paymentId);

        return payment;
    }

    @Transactional
    public CommissionPayment rejectPayment(Long paymentId, String reason, String rejectedBy) {
        log.info("Rejecting payment batch: {} by: {}", paymentId, rejectedBy);

        CommissionPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment batch not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot reject completed payment batch");
        }

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setProcessingError(reason);
        payment = paymentRepository.save(payment);

        // Release commissions back to validated status
        List<Commission> commissions = commissionRepository
                .findByPaymentBatchId(payment.getId());

        for (Commission commission : commissions) {
            commission.setStatus(CommissionStatus.VALIDATED);
            commission.setPaymentBatchId(null);
            commission.setPaymentReference(null);
            commissionRepository.save(commission);
        }

        log.info("Payment batch rejected: {}", paymentId);

        return payment;
    }

    @Transactional
    public void cancelPayment(Long paymentId, String reason, String cancelledBy) {
        log.info("Cancelling payment batch: {} by: {}", paymentId, cancelledBy);

        CommissionPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment batch not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot cancel completed payment batch");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setProcessingError(reason);
        paymentRepository.save(payment);

        // Release commissions back to validated status
        List<Commission> commissions = commissionRepository
                .findByPaymentBatchId(payment.getId());

        for (Commission commission : commissions) {
            commission.setStatus(CommissionStatus.VALIDATED);
            commission.setPaymentBatchId(null);
            commission.setPaymentReference(null);
            commissionRepository.save(commission);
        }

        log.info("Payment batch cancelled: {}", paymentId);
    }

    @Transactional(readOnly = true)
    public CommissionPayment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment batch not found: " + paymentId));
    }

    @Transactional(readOnly = true)
    public CommissionPayment getPaymentByBatchNumber(String batchNumber) {
        return paymentRepository.findByBatchNumber(batchNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment batch not found: " + batchNumber));
    }

    @Transactional(readOnly = true)
    public Page<CommissionPayment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<CommissionPayment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<CommissionPayment> getPaymentsByYearAndMonth(Integer year, Integer month) {
        return paymentRepository.findByPaymentYearAndPaymentMonth(year, month);
    }

    @Transactional(readOnly = true)
    public List<CommissionPayment> getPaymentsByBusinessUnit(Long businessUnitId) {
        return paymentRepository.findByBusinessUnitId(businessUnitId);
    }

    @Transactional(readOnly = true)
    public List<Commission> getCommissionsInPaymentBatch(Long paymentId) {
        return commissionRepository.findByPaymentBatchId(paymentId);
    }

    private List<Commission> validateCommissionsForPayment(List<Long> commissionIds) {
        List<Commission> commissions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long commissionId : commissionIds) {
            Commission commission = commissionRepository.findById(commissionId)
                    .orElse(null);

            if (commission == null) {
                errors.add("Commission not found: " + commissionId);
                continue;
            }

            if (commission.getStatus() != CommissionStatus.VALIDATED) {
                errors.add("Commission " + commissionId +
                        " is not validated. Current status: " + commission.getStatus());
                continue;
            }

            if (commission.getPaymentBatchId() != null) {
                errors.add("Commission " + commissionId +
                        " is already assigned to payment batch: " + commission.getPaymentBatchId());
                continue;
            }

            commissions.add(commission);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid commissions for payment: " + String.join(", ", errors));
        }

        if (commissions.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid commissions provided for payment batch");
        }

        return commissions;
    }

    private void associateCommissionWithPayment(Commission commission,
                                                 CommissionPayment payment) {
        commission.setStatus(CommissionStatus.PENDING_PAYMENT);
        commission.setPaymentBatchId(payment.getId());
        commission.setPaymentReference(payment.getBatchNumber());
        commission.setPaymentDueDate(payment.getScheduledPaymentDate());

        commissionRepository.save(commission);
    }

    private void markCommissionAsPaid(Commission commission, CommissionPayment payment) {
        commission.setStatus(CommissionStatus.PAID);
        commission.setPaymentDate(LocalDateTime.now());

        commissionRepository.save(commission);

        // Publish commission paid event
        publishCommissionPaidEvent(commission, payment);
    }

    private String generateBatchNumber(Integer year, Integer month) {
        String yearMonth = String.format("%d%02d", year, month);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));

        return String.format("PAY-%s-%s-%s", yearMonth, timestamp, random);
    }

    private void publishPaymentBatchCreatedEvent(CommissionPayment payment) {
        try {
            PaymentBatchCreatedEvent event = PaymentBatchCreatedEvent.builder()
                    .paymentBatchId(payment.getId())
                    .batchNumber(payment.getBatchNumber())
                    .batchName(payment.getBatchName())
                    .paymentYear(payment.getPaymentYear())
                    .paymentMonth(payment.getPaymentMonth())
                    .paymentPeriod(payment.getPaymentPeriod())
                    .businessUnitId(payment.getBusinessUnitId())
                    .businessUnitName(payment.getBusinessUnitName())
                    .totalCommissions(payment.getTotalCommissions())
                    .totalAmountExclTax(payment.getTotalAmountExclTax())
                    .totalAmountInclTax(payment.getTotalAmountInclTax())
                    .scheduledPaymentDate(payment.getScheduledPaymentDate())
                    .createdBy(payment.getCreatedBy())
                    .eventId(UUID.randomUUID().toString())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(TOPIC_PAYMENT_CREATED, event);
            log.debug("Published PaymentBatchCreatedEvent for payment: {}", payment.getId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentBatchCreatedEvent", e);
        }
    }

    private void publishCommissionPaidEvent(Commission commission, CommissionPayment payment) {
        try {
            CommissionPaidEvent event = CommissionPaidEvent.builder()
                    .commissionId(commission.getId())
                    .orderId(commission.getOrderId())
                    .orderNumber(commission.getOrderNumber())
                    .salespersonId(commission.getSalespersonId())
                    .salespersonName(commission.getSalespersonName())
                    .totalCommissionExclTax(commission.getTotalCommissionExclTax())
                    .totalCommissionInclTax(commission.getTotalCommissionInclTax())
                    .paymentBatchId(payment.getId())
                    .paymentBatchNumber(payment.getBatchNumber())
                    .paymentReference(commission.getPaymentReference())
                    .paymentDate(commission.getPaymentDate())
                    .eventId(UUID.randomUUID().toString())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(TOPIC_COMMISSION_PAID, event);
            log.debug("Published CommissionPaidEvent for commission: {}", commission.getId());
        } catch (Exception e) {
            log.error("Failed to publish CommissionPaidEvent", e);
        }
    }
}
