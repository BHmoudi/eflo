package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.ConditionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConditionCategoryRepository extends JpaRepository<ConditionCategory, Long> {

    Optional<ConditionCategory> findByCode(String code);

    List<ConditionCategory> findByIsActive(Boolean isActive);

    boolean existsByCode(String code);
}
