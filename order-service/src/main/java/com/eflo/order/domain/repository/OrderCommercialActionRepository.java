package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderCommercialAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderCommercialActionRepository extends JpaRepository<OrderCommercialAction, Long> {

    @Query("SELECT oca FROM OrderCommercialAction oca WHERE oca.order.id = :orderId")
    List<OrderCommercialAction> findByOrderId(@Param("orderId") Long orderId);

    @Query("DELETE FROM OrderCommercialAction oca WHERE oca.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
