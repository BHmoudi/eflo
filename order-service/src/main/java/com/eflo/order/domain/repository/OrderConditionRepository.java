package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderConditionRepository extends JpaRepository<OrderCondition, Long> {

    Optional<OrderCondition> findByCode(String code);

    List<OrderCondition> findByIsActive(Boolean isActive);

    @Query("SELECT c FROM OrderCondition c WHERE c.category.code = :categoryCode AND c.isActive = true ORDER BY c.priority ASC")
    List<OrderCondition> findActiveByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT c FROM OrderCondition c WHERE c.category.id = :categoryId AND c.isActive = true ORDER BY c.priority ASC")
    List<OrderCondition> findActiveByCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByCode(String code);
}
