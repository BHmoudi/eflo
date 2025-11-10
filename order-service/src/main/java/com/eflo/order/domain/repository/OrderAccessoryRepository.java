package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAccessoryRepository extends JpaRepository<OrderAccessory, Long> {

    List<OrderAccessory> findByOrderId(Long orderId);

    List<OrderAccessory> findByOrderIdAndAccessoryCategory(Long orderId, String category);
}
