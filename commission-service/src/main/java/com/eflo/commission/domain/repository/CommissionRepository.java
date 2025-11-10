package com.eflo.commission.domain.repository;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long>, JpaSpecificationExecutor<Commission> {

    Optional<Commission> findByOrderId(Long orderId);

    Optional<Commission> findByOrderNumber(String orderNumber);

    List<Commission> findBySalespersonId(Long salespersonId);

    List<Commission> findByManagerId(Long managerId);

    List<Commission> findByBusinessUnitId(Long businessUnitId);

    List<Commission> findByStatus(CommissionStatus status);

    List<Commission> findByPaymentBatchId(Long paymentBatchId);

    @Query("SELECT c FROM Commission c WHERE c.salespersonId = :salespersonId AND c.status = :status")
    List<Commission> findBySalespersonIdAndStatus(
        @Param("salespersonId") Long salespersonId,
        @Param("status") CommissionStatus status
    );

    @Query("SELECT c FROM Commission c WHERE c.businessUnitId = :businessUnitId " +
           "AND EXTRACT(YEAR FROM c.calculationDate) = :year " +
           "AND EXTRACT(MONTH FROM c.calculationDate) = :month")
    List<Commission> findByBusinessUnitAndMonth(
        @Param("businessUnitId") Long businessUnitId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );

    @Query("SELECT c FROM Commission c WHERE c.salespersonId = :salespersonId " +
           "AND EXTRACT(YEAR FROM c.calculationDate) = :year " +
           "AND EXTRACT(MONTH FROM c.calculationDate) = :month")
    List<Commission> findBySalespersonAndMonth(
        @Param("salespersonId") Long salespersonId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );

    @Query("SELECT c FROM Commission c WHERE c.status IN :statuses " +
           "AND c.calculationDate BETWEEN :startDate AND :endDate")
    List<Commission> findByStatusInAndCalculationDateBetween(
        @Param("statuses") List<CommissionStatus> statuses,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c FROM Commission c WHERE c.status IN ('VALIDATED', 'PENDING_PAYMENT') " +
           "AND c.businessUnitId = :businessUnitId " +
           "AND EXTRACT(YEAR FROM c.calculationDate) = :year " +
           "AND EXTRACT(MONTH FROM c.calculationDate) = :month")
    List<Commission> findPendingPaymentsByBusinessUnitAndMonth(
        @Param("businessUnitId") Long businessUnitId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );

    @Query("SELECT c FROM Commission c WHERE c.status = 'PENDING_PAYMENT' " +
           "AND c.paymentDueDate < :date")
    List<Commission> findOverdueCommissions(@Param("date") LocalDate date);

    @Query("SELECT c FROM Commission c WHERE c.status = 'PENDING_CALCULATION'")
    List<Commission> findPendingCalculations();

    @Query("SELECT COUNT(c) FROM Commission c WHERE c.salespersonId = :salespersonId " +
           "AND c.status = 'PAID' " +
           "AND EXTRACT(YEAR FROM c.paymentDate) = :year")
    Long countPaidCommissionsBySalespersonAndYear(
        @Param("salespersonId") Long salespersonId,
        @Param("year") Integer year
    );

    @Query("SELECT SUM(c.totalCommissionExclTax) FROM Commission c " +
           "WHERE c.salespersonId = :salespersonId AND c.status = 'PAID' " +
           "AND EXTRACT(YEAR FROM c.paymentDate) = :year")
    Optional<java.math.BigDecimal> sumPaidCommissionsBySalespersonAndYear(
        @Param("salespersonId") Long salespersonId,
        @Param("year") Integer year
    );

    boolean existsByOrderId(Long orderId);

    @Query("SELECT c FROM Commission c WHERE c.businessUnitId = :businessUnitId " +
           "AND c.status = :status " +
           "AND c.calculationDate BETWEEN :startDate AND :endDate")
    List<Commission> findByBusinessUnitIdAndStatusAndCalculationDateBetween(
        @Param("businessUnitId") Long businessUnitId,
        @Param("status") CommissionStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
