package com.eflo.order.domain.repository;

import com.eflo.order.domain.entity.DocumentType;
import com.eflo.order.domain.entity.OrderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {

    /**
     * Find all documents for an order (excluding deleted)
     */
    @Query("SELECT d FROM OrderDocument d WHERE d.order.id = :orderId AND d.deletedAt IS NULL")
    List<OrderDocument> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find documents by order and type
     */
    @Query("SELECT d FROM OrderDocument d WHERE d.order.id = :orderId AND d.documentType = :documentType AND d.deletedAt IS NULL")
    List<OrderDocument> findByOrderIdAndDocumentType(@Param("orderId") Long orderId,
                                                      @Param("documentType") DocumentType documentType);

    /**
     * Find document by ID and order ID (for security check)
     */
    @Query("SELECT d FROM OrderDocument d WHERE d.id = :documentId AND d.order.id = :orderId AND d.deletedAt IS NULL")
    Optional<OrderDocument> findByIdAndOrderId(@Param("documentId") Long documentId,
                                                @Param("orderId") Long orderId);

    /**
     * Check if document exists for order
     */
    @Query("SELECT COUNT(d) > 0 FROM OrderDocument d WHERE d.order.id = :orderId AND d.fileName = :fileName AND d.deletedAt IS NULL")
    boolean existsByOrderIdAndFileName(@Param("orderId") Long orderId,
                                       @Param("fileName") String fileName);
}
