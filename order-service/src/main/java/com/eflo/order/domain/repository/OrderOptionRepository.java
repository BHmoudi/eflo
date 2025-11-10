package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.OrderOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOptionRepository extends JpaRepository<OrderOption, Long> {

    List<OrderOption> findByOrderId(Long orderId);

    List<OrderOption> findByOrderIdAndOptionCategory(Long orderId, String category);

    List<OrderOption> findByOrderIdAndIsMandatory(Long orderId, Boolean isMandatory);
}
