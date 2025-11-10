package com.eflo.document.service;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.exception.DocumentStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing document expiration.
 * Handles expiration checking, notifications, and auto-archiving with scheduled tasks.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpirationManagementService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentEventPublisher eventPublisher;

    @Value("${eflo.document.expiration.default-warning-days:30}")
    private int defaultWarningDays;

    @Value("${eflo.document.expiration.auto-archive-enabled:false}")
    private boolean autoArchiveEnabled;

    /**
     * Scheduled task to check for expiring documents.
     * Runs based on the configured cron expression.
     */
    @Scheduled(cron = "${eflo.document.expiration.check-cron:0 0 2 * * ?}")
    @Transactional
    public void checkExpiringDocuments() {
        log.info("Starting scheduled expiration check");

        try {
            // Get document types with expiration enabled
            List<DocumentType> expiringTypes = documentTypeRepository.findWithExpiration();

            if (expiringTypes.isEmpty()) {
                log.debug("No document types with expiration enabled");
                return;
            }

            int totalProcessed = 0;
            int totalNotified = 0;
            int totalArchived = 0;

            for (DocumentType documentType : expiringTypes) {
                int warningDays = documentType.getExpirationWarningDays() != null
                        ? documentType.getExpirationWarningDays()
                        : defaultWarningDays;

                // Get expiring documents for this type
                List<Document> expiringDocs = getExpiringDocuments(warningDays);

                // Filter by document type
                List<Document> typeExpiringDocs = expiringDocs.stream()
                        .filter(doc -> doc.getDocumentType().getId().equals(documentType.getId()))
                        .collect(Collectors.toList());

                if (!typeExpiringDocs.isEmpty()) {
                    notifyExpiringDocuments(typeExpiringDocs);
                    totalNotified += typeExpiringDocs.size();
                }

                totalProcessed += typeExpiringDocs.size();
            }

            // Check for expired documents
            List<Document> expiredDocs = getExpiredDocuments();
            if (!expiredDocs.isEmpty()) {
                if (autoArchiveEnabled) {
                    archiveExpiredDocuments(expiredDocs);
                    totalArchived = expiredDocs.size();
                } else {
                    log.info("Found {} expired documents (auto-archive disabled)", expiredDocs.size());
                }
            }

            log.info("Expiration check completed: {} documents processed, {} notifications sent, {} archived",
                    totalProcessed, totalNotified, totalArchived);

        } catch (Exception e) {
            log.error("Error during scheduled expiration check", e);
        }
    }

    /**
     * Retrieves documents expiring within the specified number of days.
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

            // Find all documents with expiration dates in the future but within warning period
            List<Document> allDocuments = documentRepository.findAll();

            return allDocuments.stream()
                    .filter(doc -> doc.getExpirationDate() != null)
                    .filter(doc -> doc.getStatus() == DocumentStatus.VALIDATED)
                    .filter(doc -> !doc.getExpirationDate().isBefore(today))
                    .filter(doc -> doc.getExpirationDate().isBefore(futureDate) || doc.getExpirationDate().isEqual(futureDate))
                    .filter(doc -> !Boolean.TRUE.equals(doc.getExpirationNotified()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving expiring documents", e);
            throw new DocumentStorageException("Failed to retrieve expiring documents", e);
        }
    }

    /**
     * Retrieves documents that have already expired.
     *
     * @return list of expired documents
     */
    @Transactional(readOnly = true)
    public List<Document> getExpiredDocuments() {
        log.debug("Retrieving expired documents");

        try {
            LocalDate today = LocalDate.now();

            List<Document> allDocuments = documentRepository.findAll();

            return allDocuments.stream()
                    .filter(doc -> doc.getExpirationDate() != null)
                    .filter(doc -> doc.getExpirationDate().isBefore(today))
                    .filter(doc -> doc.getStatus() != DocumentStatus.EXPIRED)
                    .filter(doc -> doc.getStatus() != DocumentStatus.ARCHIVED)
                    .filter(doc -> doc.getStatus() != DocumentStatus.DELETED)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving expired documents", e);
            throw new DocumentStorageException("Failed to retrieve expired documents", e);
        }
    }

    /**
     * Sends notifications for expiring documents.
     *
     * @param documents list of expiring documents
     */
    @Transactional
    public void notifyExpiringDocuments(List<Document> documents) {
        log.info("Sending expiration notifications for {} documents", documents.size());

        for (Document document : documents) {
            try {
                // Publish expiration warning event
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("documentId", document.getId());
                eventData.put("documentUuid", document.getDocumentUuid().toString());
                eventData.put("typeCode", document.getTypeCode());
                eventData.put("orderId", document.getOrderId());
                eventData.put("orderNumber", document.getOrderNumber());
                eventData.put("expirationDate", document.getExpirationDate());
                eventData.put("daysUntilExpiration",
                        java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), document.getExpirationDate()));
                eventData.put("uploadedBy", document.getUploadedBy());

                // eventPublisher.publishEvent("DOCUMENT_EXPIRING", eventData);

                // Mark notification as sent
                document.markExpirationNotificationSent();
                documentRepository.save(document);

                log.debug("Expiration notification sent for document ID: {}", document.getId());

            } catch (Exception e) {
                log.error("Error sending expiration notification for document ID: {}", document.getId(), e);
            }
        }
    }

    /**
     * Archives expired documents.
     *
     * @param documents list of expired documents
     */
    @Transactional
    public void archiveExpiredDocuments(List<Document> documents) {
        log.info("Archiving {} expired documents", documents.size());

        for (Document document : documents) {
            try {
                // Mark as expired
                document.setStatus(DocumentStatus.EXPIRED);
                document.setStatusReason("Automatically expired on " + LocalDate.now());
                document.setUpdatedAt(LocalDateTime.now());

                // Archive the document
                document.markAsArchived();
                documentRepository.save(document);

                // Publish expiration event
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("documentId", document.getId());
                eventData.put("documentUuid", document.getDocumentUuid().toString());
                eventData.put("typeCode", document.getTypeCode());
                eventData.put("orderId", document.getOrderId());
                eventData.put("orderNumber", document.getOrderNumber());
                eventData.put("expirationDate", document.getExpirationDate());

                // eventPublisher.publishEvent("DOCUMENT_EXPIRED", eventData);

                log.debug("Archived expired document ID: {}", document.getId());

            } catch (Exception e) {
                log.error("Error archiving expired document ID: {}", document.getId(), e);
            }
        }
    }

    /**
     * Sets expiration date for a document.
     *
     * @param documentId the document identifier
     * @param expirationDate the expiration date to set
     */
    @Transactional
    public void setDocumentExpiration(Long documentId, LocalDate expirationDate) {
        log.info("Setting expiration date for document ID: {} to {}", documentId, expirationDate);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentStorageException("Document not found with ID: " + documentId));

        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new DocumentStorageException("Expiration date must be in the future");
        }

        try {
            document.setExpirationDate(expirationDate);
            document.setExpirationNotified(false);
            document.setExpirationNotificationSentAt(null);
            document.setUpdatedAt(LocalDateTime.now());

            documentRepository.save(document);

            log.info("Successfully set expiration date for document ID: {}", documentId);

        } catch (Exception e) {
            log.error("Error setting expiration date for document ID: {}", documentId, e);
            throw new DocumentStorageException("Failed to set document expiration date", e);
        }
    }

    /**
     * Extends document expiration by additional days.
     *
     * @param documentId the document identifier
     * @param additionalDays number of days to extend
     */
    @Transactional
    public void extendDocumentExpiration(Long documentId, int additionalDays) {
        log.info("Extending expiration for document ID: {} by {} days", documentId, additionalDays);

        if (additionalDays < 1) {
            throw new DocumentStorageException("Additional days must be at least 1");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentStorageException("Document not found with ID: " + documentId));

        if (document.getExpirationDate() == null) {
            throw new DocumentStorageException("Document does not have an expiration date set");
        }

        try {
            LocalDate newExpirationDate = document.getExpirationDate().plusDays(additionalDays);
            document.setExpirationDate(newExpirationDate);
            document.setExpirationNotified(false);
            document.setExpirationNotificationSentAt(null);
            document.setUpdatedAt(LocalDateTime.now());

            documentRepository.save(document);

            // Publish extension event
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("documentId", document.getId());
            eventData.put("documentUuid", document.getDocumentUuid().toString());
            eventData.put("additionalDays", additionalDays);
            eventData.put("newExpirationDate", newExpirationDate);

            // eventPublisher.publishEvent("DOCUMENT_EXPIRATION_EXTENDED", eventData);

            log.info("Successfully extended expiration for document ID: {} to {}", documentId, newExpirationDate);

        } catch (Exception e) {
            log.error("Error extending expiration for document ID: {}", documentId, e);
            throw new DocumentStorageException("Failed to extend document expiration", e);
        }
    }

    /**
     * Cancels document expiration (removes expiration date).
     *
     * @param documentId the document identifier
     */
    @Transactional
    public void cancelDocumentExpiration(Long documentId) {
        log.info("Canceling expiration for document ID: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentStorageException("Document not found with ID: " + documentId));

        if (document.getExpirationDate() == null) {
            log.warn("Document ID: {} does not have an expiration date set", documentId);
            return;
        }

        try {
            document.setExpirationDate(null);
            document.setExpirationNotified(false);
            document.setExpirationNotificationSentAt(null);
            document.setUpdatedAt(LocalDateTime.now());

            documentRepository.save(document);

            log.info("Successfully canceled expiration for document ID: {}", documentId);

        } catch (Exception e) {
            log.error("Error canceling expiration for document ID: {}", documentId, e);
            throw new DocumentStorageException("Failed to cancel document expiration", e);
        }
    }

    /**
     * Generates an expiration report for a business unit.
     *
     * @param businessUnitId the business unit identifier (null for all)
     * @return expiration report
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateExpirationReport(Long businessUnitId) {
        log.debug("Generating expiration report for business unit: {}", businessUnitId);

        try {
            Map<String, Object> report = new HashMap<>();
            report.put("generatedAt", LocalDateTime.now());
            report.put("businessUnitId", businessUnitId);

            // Get all documents (optionally filtered by business unit)
            List<Document> allDocuments = documentRepository.findAll();
            if (businessUnitId != null) {
                allDocuments = allDocuments.stream()
                        .filter(doc -> businessUnitId.equals(doc.getBusinessUnitId()))
                        .collect(Collectors.toList());
            }

            // Count documents with expiration
            long totalWithExpiration = allDocuments.stream()
                    .filter(doc -> doc.getExpirationDate() != null)
                    .count();

            // Expiring within 7 days
            List<Document> expiring7Days = filterExpiringDocuments(allDocuments, 7);

            // Expiring within 30 days
            List<Document> expiring30Days = filterExpiringDocuments(allDocuments, 30);

            // Expiring within 90 days
            List<Document> expiring90Days = filterExpiringDocuments(allDocuments, 90);

            // Already expired
            List<Document> expired = allDocuments.stream()
                    .filter(doc -> doc.getExpirationDate() != null)
                    .filter(doc -> doc.getExpirationDate().isBefore(LocalDate.now()))
                    .collect(Collectors.toList());

            report.put("totalDocumentsWithExpiration", totalWithExpiration);
            report.put("expiringWithin7Days", expiring7Days.size());
            report.put("expiringWithin30Days", expiring30Days.size());
            report.put("expiringWithin90Days", expiring90Days.size());
            report.put("expired", expired.size());

            // Group by document type
            Map<String, Long> byType = allDocuments.stream()
                    .filter(doc -> doc.getExpirationDate() != null)
                    .collect(Collectors.groupingBy(
                            doc -> doc.getDocumentType().getTypeName(),
                            Collectors.counting()
                    ));

            report.put("countsByDocumentType", byType);

            // Summary lists
            report.put("expiring7DaysList", createSummaryList(expiring7Days));
            report.put("expiring30DaysList", createSummaryList(expiring30Days));
            report.put("expiredList", createSummaryList(expired));

            return report;

        } catch (Exception e) {
            log.error("Error generating expiration report", e);
            throw new DocumentStorageException("Failed to generate expiration report", e);
        }
    }

    // Private helper methods

    /**
     * Filters documents expiring within specified days.
     */
    private List<Document> filterExpiringDocuments(List<Document> documents, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        return documents.stream()
                .filter(doc -> doc.getExpirationDate() != null)
                .filter(doc -> !doc.getExpirationDate().isBefore(today))
                .filter(doc -> doc.getExpirationDate().isBefore(futureDate) || doc.getExpirationDate().isEqual(futureDate))
                .collect(Collectors.toList());
    }

    /**
     * Creates summary list for report.
     */
    private List<Map<String, Object>> createSummaryList(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("documentId", doc.getId());
                    summary.put("documentUuid", doc.getDocumentUuid().toString());
                    summary.put("typeCode", doc.getTypeCode());
                    summary.put("typeName", doc.getDocumentType().getTypeName());
                    summary.put("orderId", doc.getOrderId());
                    summary.put("orderNumber", doc.getOrderNumber());
                    summary.put("expirationDate", doc.getExpirationDate());
                    summary.put("daysUntilExpiration",
                            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), doc.getExpirationDate()));
                    summary.put("uploadedBy", doc.getUploadedBy());
                    return summary;
                })
                .collect(Collectors.toList());
    }
}
