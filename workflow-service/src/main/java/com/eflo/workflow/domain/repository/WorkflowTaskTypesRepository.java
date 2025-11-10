package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowTaskTypesRepository extends JpaRepository<WorkflowTaskType, Long> {
    Optional<WorkflowTaskType> findByTypeCode(String typeCode);
}
