package com.eflo.commission.domain.repository;

import com.eflo.commission.domain.entity.CommissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionHistoryRepository extends JpaRepository<CommissionHistory, Long> {

    List<CommissionHistory> findByCommissionIdOrderByChangedAtDesc(Long commissionId);

    List<CommissionHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);

    List<CommissionHistory> findByChangeType(String changeType);

    List<CommissionHistory> findByChangedBy(String changedBy);

    @Query("SELECT ch FROM CommissionHistory ch WHERE ch.commissionId = :commissionId " +
           "AND ch.changeType = :changeType ORDER BY ch.changedAt DESC")
    List<CommissionHistory> findByCommissionIdAndChangeType(
        @Param("commissionId") Long commissionId,
        @Param("changeType") String changeType
    );
}
