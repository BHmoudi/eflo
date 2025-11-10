package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.ConditionDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionDependencyRepository extends JpaRepository<ConditionDependency, Long> {

    @Query("SELECT d FROM ConditionDependency d WHERE d.condition.id = :conditionId")
    List<ConditionDependency> findByConditionId(@Param("conditionId") Long conditionId);

    @Query("SELECT d FROM ConditionDependency d WHERE d.condition.id = :conditionId AND d.dependencyType = :dependencyType")
    List<ConditionDependency> findByConditionIdAndDependencyType(
        @Param("conditionId") Long conditionId,
        @Param("dependencyType") ConditionDependency.DependencyType dependencyType
    );
}
