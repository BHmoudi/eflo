package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTaskConditionsRepository extends JpaRepository<WorkflowTaskCondition, Long> {
    List<WorkflowTaskCondition> findByTaskId(Long taskId);
    List<WorkflowTaskCondition> findByTaskIdAndIsActiveTrue(Long taskId);
}
