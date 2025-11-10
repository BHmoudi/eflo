package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderAssignedCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAssignedConditionRepository extends JpaRepository<OrderAssignedCondition, Long> {

    @Query("SELECT ac FROM OrderAssignedCondition ac WHERE ac.order.id = :orderId")
    List<OrderAssignedCondition> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT ac FROM OrderAssignedCondition ac WHERE ac.order.id = :orderId AND ac.condition.id = :conditionId")
    Optional<OrderAssignedCondition> findByOrderIdAndConditionId(
        @Param("orderId") Long orderId,
        @Param("conditionId") Long conditionId
    );

    @Query("SELECT ac FROM OrderAssignedCondition ac WHERE ac.order.id = :orderId AND ac.isManual = :isManual")
    List<OrderAssignedCondition> findByOrderIdAndIsManual(
        @Param("orderId") Long orderId,
        @Param("isManual") Boolean isManual
    );

    @Query("DELETE FROM OrderAssignedCondition ac WHERE ac.order.id = :orderId AND ac.isManual = false")
    void deleteAutoAssignedByOrderId(@Param("orderId") Long orderId);
}
