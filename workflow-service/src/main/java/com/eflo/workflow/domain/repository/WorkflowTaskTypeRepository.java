package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowTaskTypeRepository
 *
 * Repository for managing workflow task types.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskTypeRepository extends JpaRepository<WorkflowTaskType, Long> {

    /**
     * Find task type by type code
     */
    Optional<WorkflowTaskType> findByTypeCode(String typeCode);

    /**
     * Find all active task types ordered by display order
     */
    List<WorkflowTaskType> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Check if task type exists by type code
     */
    boolean existsByTypeCode(String typeCode);
}
