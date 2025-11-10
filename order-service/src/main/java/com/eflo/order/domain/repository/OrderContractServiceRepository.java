package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderContractService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderContractServiceRepository extends JpaRepository<OrderContractService, Long> {

    List<OrderContractService> findByOrderId(Long orderId);

    List<OrderContractService> findByOrderIdAndServiceType(Long orderId, String serviceType);
}
