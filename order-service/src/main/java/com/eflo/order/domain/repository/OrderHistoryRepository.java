package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);

    List<OrderHistory> findByOrderIdAndEventType(Long orderId, String eventType);

    @Query("SELECT h FROM OrderHistory h WHERE h.changedByUserId = :userId AND h.changedAt BETWEEN :startDate AND :endDate ORDER BY h.changedAt DESC")
    List<OrderHistory> findByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
