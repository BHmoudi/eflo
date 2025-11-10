package com.eflo.document.domain.repository;

import com.eflo.document.domain.entity.DocumentPlaceholder;
import com.eflo.document.domain.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DocumentPlaceholder entity
 *
 * @author Document Service
 * @version 1.0.0
 */
@Repository
public interface DocumentPlaceholderRepository extends JpaRepository<DocumentPlaceholder, Long> {

    /**
     * Find all placeholders for an order
     */
    List<DocumentPlaceholder> findByOrderId(Long orderId);

    /**
     * Find all placeholders for a workflow instance
     */
    List<DocumentPlaceholder> findByWorkflowInstanceId(Long workflowInstanceId);

    /**
     * Find all placeholders for an order by status
     */
    List<DocumentPlaceholder> findByOrderIdAndStatus(Long orderId, DocumentStatus status);

    /**
     * Find all mandatory pending placeholders for an order
     */
    @Query("SELECT p FROM DocumentPlaceholder p " +
           "WHERE p.orderId = :orderId " +
           "AND p.isMandatory = true " +
           "AND p.status = 'PENDING_UPLOAD'")
    List<DocumentPlaceholder> findMandatoryPendingByOrderId(@Param("orderId") Long orderId);

    /**
     * Find placeholder by order, instance, and document type
     */
    Optional<DocumentPlaceholder> findByOrderIdAndWorkflowInstanceIdAndDocumentTypeCode(
        Long orderId,
        Long workflowInstanceId,
        String documentTypeCode
    );

    /**
     * Find all overdue placeholders
     */
    @Query("SELECT p FROM DocumentPlaceholder p " +
           "WHERE p.deadline < :now " +
           "AND p.status = 'PENDING_UPLOAD'")
    List<DocumentPlaceholder> findOverduePlaceholders(@Param("now") LocalDateTime now);

    /**
     * Count pending mandatory documents for an order
     */
    @Query("SELECT COUNT(p) FROM DocumentPlaceholder p " +
           "WHERE p.orderId = :orderId " +
           "AND p.isMandatory = true " +
           "AND p.status = 'PENDING_UPLOAD'")
    long countMandatoryPendingByOrderId(@Param("orderId") Long orderId);

    /**
     * Check if all mandatory documents are uploaded for an order
     */
    @Query("SELECT CASE WHEN COUNT(p) = 0 THEN true ELSE false END " +
           "FROM DocumentPlaceholder p " +
           "WHERE p.orderId = :orderId " +
           "AND p.isMandatory = true " +
           "AND p.status = 'PENDING_UPLOAD'")
    boolean areAllMandatoryDocumentsUploaded(@Param("orderId") Long orderId);

    /**
     * Delete placeholders for a workflow instance
     */
    void deleteByWorkflowInstanceId(Long workflowInstanceId);
}
