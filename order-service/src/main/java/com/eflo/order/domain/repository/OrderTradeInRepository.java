package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderTradeIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderTradeInRepository extends JpaRepository<OrderTradeIn, Long> {

    @Query("SELECT oti FROM OrderTradeIn oti WHERE oti.order.id = :orderId")
    Optional<OrderTradeIn> findByOrderId(@Param("orderId") Long orderId);

    List<OrderTradeIn> findByOwnerCustomerId(Long ownerCustomerId);

    Optional<OrderTradeIn> findByVin(String vin);

    @Query("DELETE FROM OrderTradeIn oti WHERE oti.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
