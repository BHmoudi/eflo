package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Response DTO containing document statistics and metrics.
 * Used for dashboard and reporting purposes.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatisticsResponse {

    /**
     * Total number of documents in the system.
     */
    private Long totalDocuments;

    /**
     * Number of active documents.
     */
    private Long activeDocuments;

    /**
     * Number of documents pending validation.
     */
    private Long pendingDocuments;

    /**
     * Number of validated/approved documents.
     */
    private Long validatedDocuments;

    /**
     * Number of rejected documents.
     */
    private Long rejectedDocuments;

    /**
     * Number of expired documents.
     */
    private Long expiredDocuments;

    /**
     * Number of archived documents.
     */
    private Long archivedDocuments;

    /**
     * Number of deleted documents.
     */
    private Long deletedDocuments;

    /**
     * Number of documents expiring soon (within configured warning period).
     */
    private Long expiringDocuments;

    /**
     * Number of documents with virus scan pending.
     */
    private Long virusScanPending;

    /**
     * Number of documents flagged as infected.
     */
    private Long infectedDocuments;

    /**
     * Number of confidential documents.
     */
    private Long confidentialDocuments;

    /**
     * Total storage size in bytes.
     */
    private Long totalStorageSizeBytes;

    /**
     * Total storage size in gigabytes.
     */
    private Double totalStorageSizeGB;

    /**
     * Average file size in megabytes.
     */
    private Double averageFileSizeMB;

    /**
     * Largest file size in megabytes.
     */
    private Double largestFileSizeMB;

    /**
     * Smallest file size in megabytes.
     */
    private Double smallestFileSizeMB;

    /**
     * Documents by status (status -> count).
     */
    private Map<String, Long> documentsByStatus;

    /**
     * Documents by type (type code -> count).
     */
    private Map<String, Long> documentsByType;

    /**
     * Documents by file extension (extension -> count).
     */
    private Map<String, Long> documentsByExtension;

    /**
     * Documents uploaded per day (date -> count) for recent period.
     */
    private Map<LocalDate, Long> uploadTrend;

    /**
     * Documents validated per day (date -> count) for recent period.
     */
    private Map<LocalDate, Long> validationTrend;

    /**
     * Top uploaders (username -> document count).
     */
    private Map<String, Long> topUploaders;

    /**
     * Top validators (username -> validation count).
     */
    private Map<String, Long> topValidators;

    /**
     * Recent activity summary.
     */
    private ActivitySummary recentActivity;

    /**
     * Business unit specific statistics (if applicable).
     */
    private Map<Long, BusinessUnitStats> businessUnitStats;

    /**
     * Activity summary for recent operations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySummary {

        /**
         * Documents uploaded in the last 24 hours.
         */
        private Long uploadedLast24Hours;

        /**
         * Documents uploaded in the last 7 days.
         */
        private Long uploadedLast7Days;

        /**
         * Documents uploaded in the last 30 days.
         */
        private Long uploadedLast30Days;

        /**
         * Documents validated in the last 24 hours.
         */
        private Long validatedLast24Hours;

        /**
         * Documents validated in the last 7 days.
         */
        private Long validatedLast7Days;

        /**
         * Documents validated in the last 30 days.
         */
        private Long validatedLast30Days;
    }

    /**
     * Statistics specific to a business unit.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessUnitStats {

        /**
         * Business unit ID.
         */
        private Long businessUnitId;

        /**
         * Total documents for this business unit.
         */
        private Long totalDocuments;

        /**
         * Active documents for this business unit.
         */
        private Long activeDocuments;

        /**
         * Pending documents for this business unit.
         */
        private Long pendingDocuments;

        /**
         * Storage size in gigabytes for this business unit.
         */
        private Double storageSizeGB;
    }

    /**
     * Calculate the percentage of validated documents.
     *
     * @return percentage of validated documents
     */
    public Double getValidationRate() {
        if (totalDocuments == null || totalDocuments == 0) {
            return 0.0;
        }
        return (validatedDocuments != null ? validatedDocuments : 0) * 100.0 / totalDocuments;
    }

    /**
     * Calculate the percentage of rejected documents.
     *
     * @return percentage of rejected documents
     */
    public Double getRejectionRate() {
        if (totalDocuments == null || totalDocuments == 0) {
            return 0.0;
        }
        return (rejectedDocuments != null ? rejectedDocuments : 0) * 100.0 / totalDocuments;
    }

    /**
     * Calculate the percentage of documents pending validation.
     *
     * @return percentage of pending documents
     */
    public Double getPendingRate() {
        if (totalDocuments == null || totalDocuments == 0) {
            return 0.0;
        }
        return (pendingDocuments != null ? pendingDocuments : 0) * 100.0 / totalDocuments;
    }
}
