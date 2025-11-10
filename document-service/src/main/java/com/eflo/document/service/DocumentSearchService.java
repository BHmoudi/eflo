package com.eflo.document.service;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentSearchRequest;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.exception.DocumentStorageException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for searching and retrieving documents with advanced filtering.
 * Provides flexible search capabilities with dynamic query building.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final DocumentRepository documentRepository;
    private final EntityManager entityManager;

    /**
     * Searches documents with advanced filtering and pagination.
     *
     * @param searchRequest the search criteria
     * @param pageable pagination information
     * @return page of matching documents
     */
    @Transactional(readOnly = true)
    public Page<Document> searchDocuments(DocumentSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching documents with criteria: {}", searchRequest);

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Document> query = cb.createQuery(Document.class);
            Root<Document> root = query.from(Document.class);

            // Build predicates
            List<Predicate> predicates = buildPredicates(searchRequest, cb, root);

            // Apply predicates
            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            // Apply sorting
            if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty()) {
                Sort.Direction direction = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

                Order order = direction == Sort.Direction.ASC
                        ? cb.asc(root.get(searchRequest.getSortBy()))
                        : cb.desc(root.get(searchRequest.getSortBy()));

                query.orderBy(order);
            } else {
                // Default sort by uploaded date descending
                query.orderBy(cb.desc(root.get("uploadedAt")));
            }

            // Execute query
            TypedQuery<Document> typedQuery = entityManager.createQuery(query);

            // Get total count
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Document> countRoot = countQuery.from(Document.class);
            countQuery.select(cb.count(countRoot));
            List<Predicate> countPredicates = buildPredicates(searchRequest, cb, countRoot);
            if (!countPredicates.isEmpty()) {
                countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
            }
            Long total = entityManager.createQuery(countQuery).getSingleResult();

            // Apply pagination
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());

            List<Document> results = typedQuery.getResultList();

            log.debug("Search returned {} results out of {} total", results.size(), total);

            return new PageImpl<>(results, pageable, total);

        } catch (Exception e) {
            log.error("Error searching documents", e);
            throw new DocumentStorageException("Failed to search documents", e);
        }
    }

    /**
     * Searches documents by content (filename, description, tags).
     *
     * @param searchTerm the search term
     * @param orderId optional order ID filter
     * @return list of matching documents
     */
    @Transactional(readOnly = true)
    public List<Document> searchByContent(String searchTerm, Long orderId) {
        log.debug("Searching documents by content: {} for order: {}", searchTerm, orderId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Document> query = cb.createQuery(Document.class);
            Root<Document> root = query.from(Document.class);

            List<Predicate> predicates = new ArrayList<>();

            // Search in filename, description
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate filenamePredicate = cb.like(cb.lower(root.get("originalFilename")), likePattern);
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), likePattern);

            predicates.add(cb.or(filenamePredicate, descriptionPredicate));

            // Filter by order if provided
            if (orderId != null) {
                predicates.add(cb.equal(root.get("orderId"), orderId));
            }

            // Exclude deleted documents
            predicates.add(cb.notEqual(root.get("status"), DocumentStatus.DELETED));

            query.where(cb.and(predicates.toArray(new Predicate[0])));
            query.orderBy(cb.desc(root.get("uploadedAt")));

            return entityManager.createQuery(query).getResultList();

        } catch (Exception e) {
            log.error("Error searching documents by content", e);
            throw new DocumentStorageException("Failed to search documents by content", e);
        }
    }

    /**
     * Searches documents by metadata fields.
     *
     * @param metadata metadata key-value pairs to search
     * @return list of matching documents
     */
    @Transactional(readOnly = true)
    public List<Document> searchByMetadata(Map<String, Object> metadata) {
        log.debug("Searching documents by metadata: {}", metadata);

        if (metadata == null || metadata.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // This is a simplified implementation
            // Actual implementation would use JSONB queries for PostgreSQL
            List<Document> allDocuments = documentRepository.findAll();

            return allDocuments.stream()
                    .filter(doc -> matchesMetadata(doc, metadata))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching documents by metadata", e);
            throw new DocumentStorageException("Failed to search documents by metadata", e);
        }
    }

    /**
     * Retrieves all documents for an order.
     *
     * @param orderId the order identifier
     * @return list of order documents
     */
    @Transactional(readOnly = true)
    public List<Document> getOrderDocuments(Long orderId) {
        log.debug("Retrieving documents for order: {}", orderId);

        if (orderId == null) {
            throw new DocumentStorageException("Order ID cannot be null");
        }

        try {
            return documentRepository.findByOrderIdOrderByUploadedAtDesc(orderId);
        } catch (Exception e) {
            log.error("Error retrieving documents for order: {}", orderId, e);
            throw new DocumentStorageException("Failed to retrieve order documents", e);
        }
    }

    /**
     * Retrieves documents for an order filtered by document type.
     *
     * @param orderId the order identifier
     * @param typeCode the document type code
     * @return list of matching documents
     */
    @Transactional(readOnly = true)
    public List<Document> getOrderDocumentsByType(Long orderId, String typeCode) {
        log.debug("Retrieving documents for order: {} with type: {}", orderId, typeCode);

        if (orderId == null || typeCode == null) {
            throw new DocumentStorageException("Order ID and type code cannot be null");
        }

        try {
            List<Document> orderDocuments = documentRepository.findByOrderId(orderId);

            return orderDocuments.stream()
                    .filter(doc -> typeCode.equals(doc.getTypeCode()))
                    .sorted(Comparator.comparing(Document::getUploadedAt).reversed())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving documents for order: {} with type: {}", orderId, typeCode, e);
            throw new DocumentStorageException("Failed to retrieve order documents by type", e);
        }
    }

    /**
     * Gets document validation status summary for an order.
     *
     * @param orderId the order identifier
     * @return validation status summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDocumentStatus(Long orderId) {
        log.debug("Retrieving document status for order: {}", orderId);

        if (orderId == null) {
            throw new DocumentStorageException("Order ID cannot be null");
        }

        try {
            List<Document> documents = documentRepository.findByOrderId(orderId);

            Map<String, Object> status = new HashMap<>();
            status.put("orderId", orderId);
            status.put("totalDocuments", documents.size());

            // Count by status
            Map<DocumentStatus, Long> statusCounts = documents.stream()
                    .collect(Collectors.groupingBy(Document::getStatus, Collectors.counting()));

            status.put("statusCounts", statusCounts);
            status.put("pendingCount", statusCounts.getOrDefault(DocumentStatus.PENDING, 0L));
            status.put("validatedCount", statusCounts.getOrDefault(DocumentStatus.VALIDATED, 0L));
            status.put("rejectedCount", statusCounts.getOrDefault(DocumentStatus.REJECTED, 0L));
            status.put("expiredCount", statusCounts.getOrDefault(DocumentStatus.EXPIRED, 0L));

            // All validated check
            boolean allValidated = documents.stream()
                    .filter(doc -> doc.getDocumentType().getIsMandatory())
                    .allMatch(doc -> doc.getStatus() == DocumentStatus.VALIDATED);

            status.put("allMandatoryValidated", allValidated);

            // Latest uploads
            List<Document> latestUploads = documents.stream()
                    .sorted(Comparator.comparing(Document::getUploadedAt).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            status.put("latestUploads", latestUploads);

            return status;

        } catch (Exception e) {
            log.error("Error retrieving document status for order: {}", orderId, e);
            throw new DocumentStorageException("Failed to retrieve order document status", e);
        }
    }

    /**
     * Retrieves documents uploaded by a user.
     *
     * @param userId the user identifier
     * @return list of user's documents
     */
    @Transactional(readOnly = true)
    public List<Document> getUserUploads(String userId) {
        log.debug("Retrieving uploads for user: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new DocumentStorageException("User ID cannot be empty");
        }

        try {
            return documentRepository.findByUploadedBy(userId);
        } catch (Exception e) {
            log.error("Error retrieving uploads for user: {}", userId, e);
            throw new DocumentStorageException("Failed to retrieve user uploads", e);
        }
    }

    /**
     * Retrieves documents accessible by a user.
     * This is a simplified version - actual implementation would check permissions.
     *
     * @param userId the user identifier
     * @return list of accessible documents
     */
    @Transactional(readOnly = true)
    public List<Document> getUserAccessibleDocuments(String userId) {
        log.debug("Retrieving accessible documents for user: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new DocumentStorageException("User ID cannot be empty");
        }

        try {
            // Simplified: return documents uploaded by user
            // Actual implementation would check access permissions, roles, etc.
            return documentRepository.findByUploadedBy(userId);

        } catch (Exception e) {
            log.error("Error retrieving accessible documents for user: {}", userId, e);
            throw new DocumentStorageException("Failed to retrieve accessible documents", e);
        }
    }

    /**
     * Retrieves documents pending validation by a user.
     *
     * @param userId the user identifier
     * @return list of documents pending user validation
     */
    @Transactional(readOnly = true)
    public List<Document> getPendingUserValidation(String userId) {
        log.debug("Retrieving pending validation documents for user: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new DocumentStorageException("User ID cannot be empty");
        }

        try {
            // Get all pending documents
            List<Document> pendingDocuments = documentRepository.findByOrderIdAndStatus(null, DocumentStatus.PENDING);

            // Filter by user's validator role
            // This is simplified - actual implementation would check user roles
            return pendingDocuments.stream()
                    .filter(doc -> doc.getDocumentType().getRequiresValidation())
                    .sorted(Comparator.comparing(Document::getUploadedAt))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving pending validation documents for user: {}", userId, e);
            throw new DocumentStorageException("Failed to retrieve pending validation documents", e);
        }
    }

    /**
     * Retrieves all documents pending validation.
     *
     * @return list of documents pending validation
     */
    @Transactional(readOnly = true)
    public List<Document> getPendingValidation() {
        log.debug("Retrieving all pending validation documents");

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Document> query = cb.createQuery(Document.class);
            Root<Document> root = query.from(Document.class);

            Predicate statusPredicate = cb.equal(root.get("status"), DocumentStatus.PENDING);

            query.where(statusPredicate);
            query.orderBy(cb.asc(root.get("uploadedAt")));

            return entityManager.createQuery(query).getResultList();

        } catch (Exception e) {
            log.error("Error retrieving pending validation documents", e);
            throw new DocumentStorageException("Failed to retrieve pending validation documents", e);
        }
    }

    /**
     * Retrieves documents expiring within specified days.
     *
     * @param daysAhead number of days to look ahead
     * @return list of expiring documents
     */
    @Transactional(readOnly = true)
    public List<Document> getExpiringDocuments(int daysAhead) {
        log.debug("Retrieving documents expiring within {} days", daysAhead);

        if (daysAhead < 1) {
            throw new DocumentStorageException("Days ahead must be at least 1");
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(daysAhead);

            List<Document> allDocuments = documentRepository.findAll();

            return allDocuments.stream()
                    .filter(doc -> doc.getExpirationDate() != null)
                    .filter(doc -> !doc.getExpirationDate().isBefore(today))
                    .filter(doc -> doc.getExpirationDate().isBefore(futureDate) || doc.getExpirationDate().isEqual(futureDate))
                    .sorted(Comparator.comparing(Document::getExpirationDate))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving expiring documents", e);
            throw new DocumentStorageException("Failed to retrieve expiring documents", e);
        }
    }

    // Private helper methods

    /**
     * Builds predicates for dynamic query.
     */
    private List<Predicate> buildPredicates(DocumentSearchRequest request, CriteriaBuilder cb, Root<Document> root) {
        List<Predicate> predicates = new ArrayList<>();

        // Search term (filename, description)
        if (request.getSearchTerm() != null && !request.getSearchTerm().isEmpty()) {
            String likePattern = "%" + request.getSearchTerm().toLowerCase() + "%";
            Predicate filenamePredicate = cb.like(cb.lower(root.get("originalFilename")), likePattern);
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), likePattern);
            predicates.add(cb.or(filenamePredicate, descriptionPredicate));
        }

        // Document type ID
        if (request.getDocumentTypeId() != null) {
            predicates.add(cb.equal(root.get("documentType").get("id"), request.getDocumentTypeId()));
        }

        // Type code
        if (request.getTypeCode() != null && !request.getTypeCode().isEmpty()) {
            predicates.add(cb.equal(root.get("typeCode"), request.getTypeCode()));
        }

        // Order ID
        if (request.getOrderId() != null) {
            predicates.add(cb.equal(root.get("orderId"), request.getOrderId()));
        }

        // Order number
        if (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty()) {
            predicates.add(cb.equal(root.get("orderNumber"), request.getOrderNumber()));
        }

        // Status
        if (request.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), request.getStatus()));
        }

        // Multiple statuses
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(request.getStatuses()));
        }

        // Uploaded by
        if (request.getUploadedBy() != null && !request.getUploadedBy().isEmpty()) {
            predicates.add(cb.equal(root.get("uploadedBy"), request.getUploadedBy()));
        }

        // Validated by
        if (request.getValidatedBy() != null && !request.getValidatedBy().isEmpty()) {
            predicates.add(cb.equal(root.get("validatedBy"), request.getValidatedBy()));
        }

        // Date ranges
        if (request.getUploadedFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("uploadedAt"), request.getUploadedFrom()));
        }

        if (request.getUploadedTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("uploadedAt"), request.getUploadedTo()));
        }

        // Expiration dates
        if (request.getExpirationFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("expirationDate"), request.getExpirationFrom()));
        }

        if (request.getExpirationTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("expirationDate"), request.getExpirationTo()));
        }

        // File extension
        if (request.getFileExtension() != null && !request.getFileExtension().isEmpty()) {
            predicates.add(cb.equal(root.get("fileExtension"), request.getFileExtension().toLowerCase()));
        }

        // File size range
        if (request.getMinFileSizeBytes() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fileSizeBytes"), request.getMinFileSizeBytes()));
        }

        if (request.getMaxFileSizeBytes() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fileSizeBytes"), request.getMaxFileSizeBytes()));
        }

        // Business unit
        if (request.getBusinessUnitId() != null) {
            predicates.add(cb.equal(root.get("businessUnitId"), request.getBusinessUnitId()));
        }

        // Latest version only
        if (Boolean.TRUE.equals(request.getLatestVersionOnly())) {
            predicates.add(cb.equal(root.get("isLatestVersion"), true));
        }

        // Confidential
        if (request.getIsConfidential() != null) {
            predicates.add(cb.equal(root.get("isConfidential"), request.getIsConfidential()));
        }

        // Access level
        if (request.getAccessLevel() != null) {
            predicates.add(cb.equal(root.get("accessLevel"), request.getAccessLevel()));
        }

        // Virus scan status
        if (request.getVirusScanStatus() != null) {
            predicates.add(cb.equal(root.get("virusScanStatus"), request.getVirusScanStatus()));
        }

        // Exclude deleted/archived by default using IN allowed list to avoid enum<>varchar issues
        boolean hasExplicitStatus = request.getStatus() != null || (request.getStatuses() != null && !request.getStatuses().isEmpty());
        if (!hasExplicitStatus) {
            java.util.EnumSet<DocumentStatus> allowed = java.util.EnumSet.allOf(DocumentStatus.class);
            if (!Boolean.TRUE.equals(request.getIncludeDeleted())) {
                allowed.remove(DocumentStatus.DELETED);
            }
            if (!Boolean.TRUE.equals(request.getIncludeArchived())) {
                allowed.remove(DocumentStatus.ARCHIVED);
            }
            predicates.add(root.get("status").in(allowed));
        }

        // Expiring within days
        if (request.getExpiringWithinDays() != null) {
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(request.getExpiringWithinDays());
            predicates.add(cb.between(root.get("expirationDate"), today, futureDate));
        }

        return predicates;
    }

    /**
     * Checks if document metadata matches search criteria.
     */
    private boolean matchesMetadata(Document document, Map<String, Object> searchMetadata) {
        Map<String, Object> docMetadata = document.getCustomMetadata();

        if (docMetadata == null || docMetadata.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Object> entry : searchMetadata.entrySet()) {
            Object docValue = docMetadata.get(entry.getKey());
            Object searchValue = entry.getValue();

            if (!Objects.equals(docValue, searchValue)) {
                return false;
            }
        }

        return true;
    }
}
