package com.eflo.document.domain.repository;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Document entity.
 * Provides CRUD operations and extensive custom query methods for document management.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Finds a document by its unique UUID.
     *
     * @param uuid the document UUID
     * @return Optional containing the Document if found
     */
    Optional<Document> findByDocumentUuid(UUID uuid);

    /**
     * Finds all documents associated with an order.
     *
     * @param orderId the order identifier
     * @return list of documents for the order
     */
    List<Document> findByOrderId(Long orderId);

    /**
     * Finds documents by order and document type.
     *
     * @param orderId the order identifier
     * @param typeId the document type identifier
     * @return list of documents matching the criteria
     */
    List<Document> findByOrderIdAndDocumentTypeId(Long orderId, Long typeId);

    /**
     * Finds documents by order and status.
     *
     * @param orderId the order identifier
     * @param status the document status
     * @return list of documents matching the criteria
     */
    List<Document> findByOrderIdAndStatus(Long orderId, DocumentStatus status);

    /**
     * Finds documents uploaded by a specific user.
     *
     * @param uploadedBy the user identifier
     * @return list of documents uploaded by the user
     */
    List<Document> findByUploadedBy(String uploadedBy);

    /**
     * Finds documents with a specific status and expiring before a given date.
     *
     * @param status the document status
     * @param date the expiration date threshold
     * @return list of expiring documents
     */
    List<Document> findByStatusAndExpirationDateBefore(DocumentStatus status, LocalDate date);

    /**
     * Finds documents by virus scan status.
     *
     * @param status the virus scan status
     * @return list of documents with the given scan status
     */
    List<Document> findByVirusScanStatus(VirusScanStatus status);

    /**
     * Finds all documents that are marked as the latest version.
     *
     * @return list of latest version documents
     */
    List<Document> findByIsLatestVersionTrue();

    /**
     * Finds documents by file hash (for duplicate detection).
     *
     * @param fileHash the SHA-256 file hash
     * @return list of documents with the same file hash
     */
    List<Document> findByFileHash(String fileHash);

    /**
     * Advanced search for documents with multiple criteria.
     *
     * @param orderId optional order identifier
     * @param documentTypeId optional document type identifier
     * @param status optional document status
     * @param uploadedBy optional uploader identifier
     * @param fromDate optional start date
     * @param toDate optional end date
     * @return list of documents matching all provided criteria
     */
    @Query("SELECT d FROM Document d WHERE " +
           "(:orderId IS NULL OR d.orderId = :orderId) AND " +
           "(:documentTypeId IS NULL OR d.documentType.id = :documentTypeId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:uploadedBy IS NULL OR d.uploadedBy = :uploadedBy) AND " +
           "(:fromDate IS NULL OR d.uploadedAt >= :fromDate) AND " +
           "(:toDate IS NULL OR d.uploadedAt <= :toDate) " +
           "ORDER BY d.uploadedAt DESC")
    List<Document> searchDocuments(
            @Param("orderId") Long orderId,
            @Param("documentTypeId") Long documentTypeId,
            @Param("status") DocumentStatus status,
            @Param("uploadedBy") String uploadedBy,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Finds documents that are expiring within a specified number of days.
     *
     * @param status the document status (typically VALIDATED)
     * @param days number of days in the future
     * @return list of documents expiring soon
     */
    @Query(value = "SELECT * FROM documents d WHERE d.status = CAST(:status AS VARCHAR) " +
           "AND d.expiration_date IS NOT NULL " +
           "AND d.expiration_date <= CURRENT_DATE + CAST(:days AS INTEGER) * INTERVAL '1 day' " +
           "AND d.expiration_date > CURRENT_DATE", nativeQuery = true)
    List<Document> findExpiringWithinDays(
            @Param("status") String status,
            @Param("days") int days);

    /**
     * Finds the latest version of a document by order and document type.
     *
     * @param orderId the order identifier
     * @param documentTypeId the document type identifier
     * @return Optional containing the latest document version if found
     */
    @Query("SELECT d FROM Document d WHERE d.orderId = :orderId " +
           "AND d.documentType.id = :documentTypeId " +
           "AND d.isLatestVersion = true")
    Optional<Document> findLatestVersionByOrderAndType(
            @Param("orderId") Long orderId,
            @Param("documentTypeId") Long documentTypeId);

    /**
     * Finds all versions of a document by parent document ID.
     *
     * @param parentDocumentId the parent document identifier
     * @return list of document versions ordered by version number descending
     */
    @Query("SELECT d FROM Document d WHERE d.parentDocument.id = :parentDocumentId " +
           "OR d.id = :parentDocumentId ORDER BY d.version DESC")
    List<Document> findAllVersionsByParentId(@Param("parentDocumentId") Long parentDocumentId);

    /**
     * Finds documents pending virus scan.
     *
     * @return list of documents with pending virus scan
     */
    @Query("SELECT d FROM Document d WHERE d.virusScanStatus = 'PENDING' " +
           "ORDER BY d.uploadedAt ASC")
    List<Document> findPendingVirusScan();

    /**
     * Counts documents by status.
     *
     * @param status the document status
     * @return count of documents with the given status
     */
    long countByStatus(DocumentStatus status);

    /**
     * Counts documents for a specific order.
     *
     * @param orderId the order identifier
     * @return count of documents for the order
     */
    long countByOrderId(Long orderId);

    /**
     * Counts documents by order and status.
     *
     * @param orderId the order identifier
     * @param status the document status
     * @return count of documents matching the criteria
     */
    long countByOrderIdAndStatus(Long orderId, DocumentStatus status);

    /**
     * Counts documents by order and document type.
     *
     * @param orderId the order identifier
     * @param documentTypeId the document type identifier
     * @return count of documents matching the criteria
     */
    long countByOrderIdAndDocumentTypeId(Long orderId, Long documentTypeId);

    /**
     * Gets document statistics by status.
     *
     * @return map of status to count
     */
    @Query("SELECT d.status, COUNT(d) FROM Document d GROUP BY d.status")
    List<Object[]> getDocumentStatsByStatus();

    /**
     * Gets document statistics by document type.
     *
     * @param orderId optional order identifier filter
     * @return list of document type statistics
     */
    @Query("SELECT d.documentType.id, COUNT(d) FROM Document d WHERE " +
           "(:orderId IS NULL OR d.orderId = :orderId) GROUP BY d.documentType.id")
    List<Object[]> getDocumentStatsByType(@Param("orderId") Long orderId);

    /**
     * Finds documents uploaded within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of documents uploaded in the date range
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY d.uploadedAt DESC")
    List<Document> findByUploadedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds duplicate documents by file hash for a specific order.
     *
     * @param orderId the order identifier
     * @param fileHash the file hash
     * @return list of potential duplicate documents
     */
    @Query("SELECT d FROM Document d WHERE d.orderId = :orderId " +
           "AND d.fileHash = :fileHash AND d.status != 'DELETED'")
    List<Document> findDuplicatesByOrderAndHash(
            @Param("orderId") Long orderId,
            @Param("fileHash") String fileHash);

    /**
     * Checks if a document exists by UUID.
     *
     * @param uuid the document UUID
     * @return true if exists, false otherwise
     */
    boolean existsByDocumentUuid(UUID uuid);

    /**
     * Finds documents requiring validation (pending status and requires validation).
     *
     * @return list of documents pending validation
     */
    @Query("SELECT d FROM Document d JOIN DocumentType dt ON d.documentType.id = dt.id " +
           "WHERE d.status = 'PENDING' AND dt.requiresValidation = true " +
           "ORDER BY d.uploadedAt ASC")
    List<Document> findRequiringValidation();

    /**
     * Calculates total storage size for an order.
     *
     * @param orderId the order identifier
     * @return total file size in bytes
     */
    @Query("SELECT COALESCE(SUM(d.fileSizeBytes), 0) FROM Document d " +
           "WHERE d.orderId = :orderId AND d.status != 'DELETED'")
    Long calculateTotalStorageByOrder(@Param("orderId") Long orderId);

    /**
     * Finds documents by order ID ordered by upload date descending.
     *
     * @param orderId the order identifier
     * @return list of documents ordered by upload date
     */
    List<Document> findByOrderIdOrderByUploadedAtDesc(Long orderId);
}
