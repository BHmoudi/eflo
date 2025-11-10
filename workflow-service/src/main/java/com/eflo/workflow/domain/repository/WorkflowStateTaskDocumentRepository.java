package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowStateTaskDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for WorkflowStateTaskDocument entity
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowStateTaskDocumentRepository extends JpaRepository<WorkflowStateTaskDocument, Long> {

    /**
     * Find all document requirements for a specific task
     */
    List<WorkflowStateTaskDocument> findByStateTaskId(Long stateTaskId);

    /**
     * Find all document requirements for a specific task, ordered by display order
     */
    List<WorkflowStateTaskDocument> findByStateTaskIdOrderByDisplayOrderAsc(Long stateTaskId);

    /**
     * Find all mandatory document requirements for a task
     */
    List<WorkflowStateTaskDocument> findByStateTaskIdAndIsMandatoryTrue(Long stateTaskId);

    /**
     * Find document requirement by task and document type code
     */
    @Query("SELECT d FROM WorkflowStateTaskDocument d WHERE d.stateTask.id = :taskId AND d.documentTypeCode = :typeCode")
    WorkflowStateTaskDocument findByTaskIdAndDocumentTypeCode(
        @Param("taskId") Long taskId,
        @Param("typeCode") String typeCode
    );

    /**
     * Find all document requirements for all tasks in a state
     */
    @Query("SELECT DISTINCT d FROM WorkflowStateTaskDocument d " +
           "JOIN d.stateTask t " +
           "WHERE t.state.id = :stateId " +
           "ORDER BY d.displayOrder ASC")
    List<WorkflowStateTaskDocument> findAllByStateId(@Param("stateId") Long stateId);

    /**
     * Find all mandatory document requirements for a state
     */
    @Query("SELECT DISTINCT d FROM WorkflowStateTaskDocument d " +
           "JOIN d.stateTask t " +
           "WHERE t.state.id = :stateId AND d.isMandatory = true " +
           "ORDER BY d.displayOrder ASC")
    List<WorkflowStateTaskDocument> findMandatoryByStateId(@Param("stateId") Long stateId);

    /**
     * Delete all document requirements for a task
     */
    void deleteByStateTaskId(Long stateTaskId);
}
