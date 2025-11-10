package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

    List<OrderDelivery> findByOrderId(Long orderId);

    List<OrderDelivery> findByDeliveryStatus(OrderDelivery.DeliveryStatus status);

    @Query("SELECT d FROM OrderDelivery d WHERE d.scheduledDate = :date")
    List<OrderDelivery> findByScheduledDate(@Param("date") LocalDate date);

    @Query("SELECT d FROM OrderDelivery d WHERE d.scheduledDate BETWEEN :startDate AND :endDate")
    List<OrderDelivery> findByScheduledDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Optional<OrderDelivery> findFirstByOrderIdOrderByScheduledDateDesc(Long orderId);
}
