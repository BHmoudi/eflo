package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderConditionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderConditionCriteriaRepository extends JpaRepository<OrderConditionCriteria, Long> {

    @Query("SELECT c FROM OrderConditionCriteria c WHERE c.rule.id = :ruleId ORDER BY c.sequence ASC")
    List<OrderConditionCriteria> findByRuleIdOrderBySequence(@Param("ruleId") Long ruleId);
}
