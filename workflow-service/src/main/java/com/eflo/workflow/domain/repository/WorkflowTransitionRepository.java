package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.entity.WorkflowTransition;
import com.eflo.workflow.domain.enums.TransitionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkflowTransitionRepository
 *
 * Repository for managing workflow transitions between states.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    /**
     * Find transitions by process ID
     */
    List<WorkflowTransition> findByProcessId(Long processId);

    /**
     * Find transitions from a specific state
     */
    List<WorkflowTransition> findByFromStateId(Long fromStateId);

    /**
     * Find transitions to a specific state
     */
    List<WorkflowTransition> findByToStateId(Long toStateId);

    /**
     * Find transition between two states
     */
    Optional<WorkflowTransition> findByFromStateIdAndToStateId(Long fromStateId, Long toStateId);

    /**
     * Find auto transitions from a state
     */
    List<WorkflowTransition> findByFromStateIdAndAutoTransitionTrue(Long fromStateId);

    /**
     * Find transitions by type
     */
    List<WorkflowTransition> findByTransitionType(TransitionType transitionType);

    /**
     * Find available transitions from a state
     */
    @Query("SELECT t FROM WorkflowTransition t " +
           "WHERE t.fromState.id = :fromStateId " +
           "AND t.transitionType = :transitionType")
    List<WorkflowTransition> findAvailableTransitions(@Param("fromStateId") Long fromStateId,
                                                       @Param("transitionType") TransitionType transitionType);

    /**
     * Check if transition exists between states
     */
    boolean existsByFromStateIdAndToStateId(Long fromStateId, Long toStateId);
}
