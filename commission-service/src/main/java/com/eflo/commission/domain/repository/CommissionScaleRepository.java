package com.eflo.commission.domain.repository;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CalculationMethod;
import com.eflo.commission.domain.enums.CommissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionScaleRepository extends JpaRepository<CommissionScale, Long> {

    Optional<CommissionScale> findByScaleCode(String scaleCode);

    List<CommissionScale> findByBusinessUnitId(Long businessUnitId);

    List<CommissionScale> findByBusinessUnitIdAndIsActive(Long businessUnitId, Boolean isActive);

    List<CommissionScale> findByOrderType(String orderType);

    List<CommissionScale> findByCommissionType(CommissionType commissionType);

    List<CommissionScale> findByCalculationMethod(CalculationMethod calculationMethod);

    @Query("SELECT cs FROM CommissionScale cs WHERE cs.businessUnitId = :businessUnitId " +
           "AND cs.orderType = :orderType AND cs.isActive = true " +
           "AND cs.validFrom <= :date AND (cs.validTo IS NULL OR cs.validTo >= :date)")
    List<CommissionScale> findActiveScalesForBusinessUnitAndOrderType(
        @Param("businessUnitId") Long businessUnitId,
        @Param("orderType") String orderType,
        @Param("date") LocalDate date
    );

    @Query("SELECT cs FROM CommissionScale cs WHERE cs.businessUnitId = :businessUnitId " +
           "AND cs.orderType = :orderType AND cs.isActive = true AND cs.isDefault = true " +
           "AND cs.validFrom <= :date AND (cs.validTo IS NULL OR cs.validTo >= :date)")
    Optional<CommissionScale> findDefaultScaleForBusinessUnitAndOrderType(
        @Param("businessUnitId") Long businessUnitId,
        @Param("orderType") String orderType,
        @Param("date") LocalDate date
    );

    @Query("SELECT cs FROM CommissionScale cs LEFT JOIN FETCH cs.tiers " +
           "WHERE cs.id = :id")
    Optional<CommissionScale> findByIdWithTiers(@Param("id") Long id);

    @Query("SELECT cs FROM CommissionScale cs LEFT JOIN FETCH cs.tiers " +
           "WHERE cs.scaleCode = :scaleCode")
    Optional<CommissionScale> findByScaleCodeWithTiers(@Param("scaleCode") String scaleCode);

    @Query("SELECT cs FROM CommissionScale cs WHERE cs.isActive = true " +
           "AND cs.validFrom <= :date AND (cs.validTo IS NULL OR cs.validTo >= :date)")
    List<CommissionScale> findAllActiveScales(@Param("date") LocalDate date);

    boolean existsByScaleCode(String scaleCode);

    // Additional methods for active scales
    List<CommissionScale> findByIsActiveTrue();

    List<CommissionScale> findByBusinessUnitIdAndIsActiveTrue(Long businessUnitId);

    Optional<CommissionScale> findByBusinessUnitIdAndOrderTypeAndIsActiveTrue(Long businessUnitId, String orderType);
}
