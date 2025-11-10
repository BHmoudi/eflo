package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderSupplement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderSupplementRepository extends JpaRepository<OrderSupplement, Long> {

    List<OrderSupplement> findByOrderId(Long orderId);

    List<OrderSupplement> findByOrderIdAndSupplementType(Long orderId, String supplementType);
}
