package com.eflo.docgen.domain.repository;

import com.eflo.docgen.domain.entity.GeneratedDocument;
import com.eflo.docgen.domain.enums.GenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {

    Optional<GeneratedDocument> findByDocumentUuid(String documentUuid);

    List<GeneratedDocument> findByOrderId(Long orderId);

    List<GeneratedDocument> findByWorkflowInstanceId(Long workflowInstanceId);

    List<GeneratedDocument> findByOrderIdAndGenerationStatus(Long orderId, GenerationStatus status);

    @Query("SELECT g FROM GeneratedDocument g WHERE g.orderId = :orderId AND g.requiresApproval = true AND g.isApproved = false")
    List<GeneratedDocument> findPendingApprovalByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT g FROM GeneratedDocument g WHERE g.requiresApproval = true AND g.isApproved = false")
    List<GeneratedDocument> findAllPendingApproval();

    @Query("SELECT COUNT(g) FROM GeneratedDocument g WHERE g.orderId = :orderId AND g.requiresApproval = true AND g.isApproved = false")
    long countPendingApprovalByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT CASE WHEN COUNT(g) = 0 THEN true ELSE false END FROM GeneratedDocument g " +
           "WHERE g.orderId = :orderId AND g.requiresApproval = true AND g.isApproved = false")
    boolean areAllRequiredDocsApproved(@Param("orderId") Long orderId);
}
