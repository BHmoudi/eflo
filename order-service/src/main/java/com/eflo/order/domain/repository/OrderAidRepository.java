package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderAid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAidRepository extends JpaRepository<OrderAid, Long> {

    List<OrderAid> findByOrderId(Long orderId);

    List<OrderAid> findByOrderIdAndApprovalStatus(Long orderId, OrderAid.ApprovalStatus status);
}
