package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowProcessRepository
 *
 * Repository for managing workflow process definitions.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowProcessRepository extends JpaRepository<WorkflowProcess, Long> {

    /**
     * Find process by code
     */
    Optional<WorkflowProcess> findByProcessCode(String processCode);

    /**
     * Find processes by order type
     */
    List<WorkflowProcess> findByOrderType(String orderType);

    /**
     * Find active processes by order type
     */
    List<WorkflowProcess> findByOrderTypeAndIsActiveTrue(String orderType);

    /**
     * Find all active processes
     */
    List<WorkflowProcess> findByIsActiveTrue();

    /**
     * Find process by code and order type
     */
    Optional<WorkflowProcess> findByProcessCodeAndOrderType(String processCode, String orderType);

    /**
     * Find latest version of a process
     */
    @Query("SELECT p FROM WorkflowProcess p " +
           "WHERE p.processCode = :processCode " +
           "ORDER BY p.processVersion DESC " +
           "LIMIT 1")
    Optional<WorkflowProcess> findLatestVersionByCode(@Param("processCode") String processCode);

    /**
     * Check if process code exists
     */
    boolean existsByProcessCode(String processCode);

    /**
     * Find processes with auto-progress enabled
     */
    List<WorkflowProcess> findByAutoProgressEnabledTrue();

    /**
     * Count active processes by order type
     */
    long countByOrderTypeAndIsActiveTrue(String orderType);
}
