package com.eflo.workflow.domain.repository;

import com.eflo.workflow.domain.WorkflowTaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkflowTaskCommentRepository
 *
 * Repository for managing workflow task comments.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Repository
public interface WorkflowTaskCommentRepository extends JpaRepository<WorkflowTaskComment, Long> {

    /**
     * Find non-deleted comments by instance task ID
     */
    @Query("SELECT c FROM WorkflowTaskComment c WHERE c.instanceTask.id = :instanceTaskId AND c.deletedAt IS NULL")
    List<WorkflowTaskComment> findByInstanceTaskIdAndDeletedAtIsNull(@Param("instanceTaskId") Long instanceTaskId);

    /**
     * Find comments by parent comment ID
     */
    @Query("SELECT c FROM WorkflowTaskComment c WHERE c.parentComment.id = :parentCommentId")
    List<WorkflowTaskComment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    /**
     * Find comments by user ID
     */
    List<WorkflowTaskComment> findByUserId(Long userId);

    /**
     * Count comments by instance task ID
     */
    @Query("SELECT COUNT(c) FROM WorkflowTaskComment c WHERE c.instanceTask.id = :instanceTaskId")
    long countByInstanceTaskId(@Param("instanceTaskId") Long instanceTaskId);
}
