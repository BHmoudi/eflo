package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderConditionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderConditionRuleRepository extends JpaRepository<OrderConditionRule, Long> {

    @Query("SELECT r FROM OrderConditionRule r WHERE r.condition.id = :conditionId AND r.isActive = true ORDER BY r.priority ASC")
    List<OrderConditionRule> findActiveByConditionId(@Param("conditionId") Long conditionId);

    @Query("SELECT r FROM OrderConditionRule r LEFT JOIN FETCH r.criteria WHERE r.id = :ruleId")
    OrderConditionRule findByIdWithCriteria(@Param("ruleId") Long ruleId);
}
