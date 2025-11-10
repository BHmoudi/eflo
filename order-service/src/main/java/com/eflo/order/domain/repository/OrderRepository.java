package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByVin(String vin);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findBySalespersonId(Long salespersonId);

    List<Order> findByBusinessUnitId(Long businessUnitId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByOrderType(Order.OrderType orderType);

    @Query("SELECT o FROM Order o WHERE o.expectedDeliveryDate BETWEEN :startDate AND :endDate")
    List<Order> findByExpectedDeliveryDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT o FROM Order o WHERE o.workflowInstanceId = :workflowInstanceId")
    Optional<Order> findByWorkflowInstanceId(@Param("workflowInstanceId") Long workflowInstanceId);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL")
    List<Order> findAllActive();

    boolean existsByOrderNumber(String orderNumber);

    boolean existsByVin(String vin);
}
