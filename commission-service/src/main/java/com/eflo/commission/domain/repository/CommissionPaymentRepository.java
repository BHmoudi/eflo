package com.eflo.commission.domain.repository;

import com.eflo.commission.domain.entity.CommissionPayment;
import com.eflo.commission.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, Long> {

    Optional<CommissionPayment> findByBatchNumber(String batchNumber);

    List<CommissionPayment> findByPaymentYearAndPaymentMonth(Integer year, Integer month);

    List<CommissionPayment> findByBusinessUnitId(Long businessUnitId);

    List<CommissionPayment> findByStatus(PaymentStatus status);

    @Query("SELECT cp FROM CommissionPayment cp WHERE cp.businessUnitId = :businessUnitId " +
           "AND cp.paymentYear = :year AND cp.paymentMonth = :month")
    List<CommissionPayment> findByBusinessUnitAndPeriod(
        @Param("businessUnitId") Long businessUnitId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );

    @Query("SELECT cp FROM CommissionPayment cp WHERE cp.status = 'PENDING' " +
           "ORDER BY cp.scheduledPaymentDate ASC")
    List<CommissionPayment> findPendingPayments();

    boolean existsByBatchNumber(String batchNumber);
}
