package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkflowTaskDependencyRepository
 *
 * Repository for managing workflow task dependencies.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskDependencyRepository extends JpaRepository<WorkflowTaskDependency, Long> {

    /**
     * Find dependencies by dependent task ID
     */
    @Query("SELECT d FROM WorkflowTaskDependency d WHERE d.dependentTask.id = :taskId")
    List<WorkflowTaskDependency> findByDependentTaskId(@Param("taskId") Long dependentTaskId);

    /**
     * Find dependencies by required task ID
     */
    @Query("SELECT d FROM WorkflowTaskDependency d WHERE d.requiredTask.id = :taskId")
    List<WorkflowTaskDependency> findByRequiredTaskId(@Param("taskId") Long requiredTaskId);

    /**
     * Find all dependencies for a task (both as dependent and required)
     */
    @Query("SELECT d FROM WorkflowTaskDependency d " +
           "WHERE d.dependentTask.id = :taskId " +
           "OR d.requiredTask.id = :taskId")
    List<WorkflowTaskDependency> findAllDependenciesForTask(@Param("taskId") Long taskId);
}
