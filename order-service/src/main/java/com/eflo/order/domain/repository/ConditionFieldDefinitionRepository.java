package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.ConditionFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConditionFieldDefinitionRepository extends JpaRepository<ConditionFieldDefinition, Long> {

    Optional<ConditionFieldDefinition> findByFieldPath(String fieldPath);

    @Query("SELECT f FROM ConditionFieldDefinition f WHERE f.entity = :entity AND f.isActive = true")
    List<ConditionFieldDefinition> findActiveByEntity(@Param("entity") String entity);

    @Query("SELECT f FROM ConditionFieldDefinition f WHERE f.isActive = true")
    List<ConditionFieldDefinition> findAllActive();
}
