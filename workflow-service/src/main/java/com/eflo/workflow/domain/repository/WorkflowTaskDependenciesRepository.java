package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTaskDependenciesRepository extends JpaRepository<WorkflowTaskDependency, Long> {
    List<WorkflowTaskDependency> findByDependentTaskId(Long dependentTaskId);
    List<WorkflowTaskDependency> findByRequiredTaskId(Long requiredTaskId);
}
