package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for bulk document upload operations.
 * Contains results for multiple file uploads including successes and failures.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResultResponse {

    /**
     * Overall success flag for the bulk operation.
     * True if all uploads succeeded, false if any failed.
     */
    private Boolean overallSuccess;

    /**
     * Total number of files in the bulk upload request.
     */
    private Integer totalFiles;

    /**
     * Number of files successfully uploaded.
     */
    private Integer successfulUploads;

    /**
     * Number of files that failed to upload.
     */
    private Integer failedUploads;

    /**
     * Timestamp when the bulk upload operation started.
     */
    private LocalDateTime startTime;

    /**
     * Timestamp when the bulk upload operation completed.
     */
    private LocalDateTime endTime;

    /**
     * Total processing time in milliseconds.
     */
    private Long totalProcessingTimeMs;

    /**
     * Associated order ID for all documents.
     */
    private Long orderId;

    /**
     * Associated order number for all documents.
     */
    private String orderNumber;

    /**
     * List of individual upload results for each file.
     */
    private List<FileUploadResult> results;

    /**
     * Summary of errors encountered during bulk upload.
     */
    private List<String> errorSummary;

    /**
     * Summary of warnings generated during bulk upload.
     */
    private List<String> warningSummary;

    /**
     * Overall message about the bulk upload operation.
     */
    private String message;

    /**
     * Result for an individual file upload in bulk operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadResult {

        /**
         * Original filename.
         */
        private String filename;

        /**
         * Flag indicating if this file upload was successful.
         */
        private Boolean success;

        /**
         * ID of the uploaded document (if successful).
         */
        private Long documentId;

        /**
         * UUID of the uploaded document (if successful).
         */
        private UUID documentUuid;

        /**
         * Document type code.
         */
        private String typeCode;

        /**
         * File size in megabytes.
         */
        private Double fileSizeMB;

        /**
         * Upload timestamp.
         */
        private LocalDateTime uploadedAt;

        /**
         * Processing time for this file in milliseconds.
         */
        private Long processingTimeMs;

        /**
         * Error message (if upload failed).
         */
        private String errorMessage;

        /**
         * List of specific errors for this file.
         */
        private List<String> errors;

        /**
         * List of warnings for this file.
         */
        private List<String> warnings;

        /**
         * Validation result for this file.
         */
        private ValidationResult validationResult;

        /**
         * Virus scan result for this file.
         */
        private ScanResult scanResult;

        /**
         * Download URL for the uploaded document (if available).
         */
        private String downloadUrl;

        /**
         * Check if this file upload was successful.
         *
         * @return true if upload succeeded
         */
        public boolean isSuccessful() {
            return Boolean.TRUE.equals(success);
        }

        /**
         * Check if this file upload failed.
         *
         * @return true if upload failed
         */
        public boolean isFailed() {
            return !isSuccessful();
        }

        /**
         * Check if there are errors for this file.
         *
         * @return true if errors exist
         */
        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }

        /**
         * Check if there are warnings for this file.
         *
         * @return true if warnings exist
         */
        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }
    }

    /**
     * Check if all uploads were successful.
     *
     * @return true if all uploads succeeded
     */
    public boolean allUploadsSuccessful() {
        return Boolean.TRUE.equals(overallSuccess) &&
               successfulUploads != null &&
               successfulUploads.equals(totalFiles);
    }

    /**
     * Check if any uploads failed.
     *
     * @return true if any uploads failed
     */
    public boolean hasFailures() {
        return failedUploads != null && failedUploads > 0;
    }

    /**
     * Check if all uploads failed.
     *
     * @return true if all uploads failed
     */
    public boolean allUploadsFailed() {
        return failedUploads != null &&
               totalFiles != null &&
               failedUploads.equals(totalFiles);
    }

    /**
     * Check if operation was partially successful.
     *
     * @return true if some uploads succeeded and some failed
     */
    public boolean isPartialSuccess() {
        return successfulUploads != null &&
               failedUploads != null &&
               successfulUploads > 0 &&
               failedUploads > 0;
    }

    /**
     * Get the success rate as a percentage.
     *
     * @return success rate percentage
     */
    public Double getSuccessRate() {
        if (totalFiles == null || totalFiles == 0) {
            return 0.0;
        }
        return (successfulUploads != null ? successfulUploads : 0) * 100.0 / totalFiles;
    }

    /**
     * Get list of successful uploads.
     *
     * @return list of successful file upload results
     */
    public List<FileUploadResult> getSuccessfulResults() {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .filter(FileUploadResult::isSuccessful)
                .toList();
    }

    /**
     * Get list of failed uploads.
     *
     * @return list of failed file upload results
     */
    public List<FileUploadResult> getFailedResults() {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .filter(FileUploadResult::isFailed)
                .toList();
    }

    /**
     * Create a success response for bulk upload.
     *
     * @param totalFiles total number of files
     * @param results individual file results
     * @return BulkUploadResultResponse
     */
    public static BulkUploadResultResponse success(Integer totalFiles, List<FileUploadResult> results) {
        return BulkUploadResultResponse.builder()
                .overallSuccess(true)
                .totalFiles(totalFiles)
                .successfulUploads(totalFiles)
                .failedUploads(0)
                .results(results)
                .message("All files uploaded successfully")
                .build();
    }

    /**
     * Create a partial success response for bulk upload.
     *
     * @param totalFiles total number of files
     * @param successCount number of successful uploads
     * @param failureCount number of failed uploads
     * @param results individual file results
     * @return BulkUploadResultResponse
     */
    public static BulkUploadResultResponse partialSuccess(Integer totalFiles, Integer successCount,
                                                          Integer failureCount, List<FileUploadResult> results) {
        return BulkUploadResultResponse.builder()
                .overallSuccess(false)
                .totalFiles(totalFiles)
                .successfulUploads(successCount)
                .failedUploads(failureCount)
                .results(results)
                .message(String.format("Bulk upload completed with %d successes and %d failures",
                        successCount, failureCount))
                .build();
    }

    /**
     * Create a failure response for bulk upload.
     *
     * @param totalFiles total number of files
     * @param message error message
     * @param errors list of errors
     * @return BulkUploadResultResponse
     */
    public static BulkUploadResultResponse failure(Integer totalFiles, String message, List<String> errors) {
        return BulkUploadResultResponse.builder()
                .overallSuccess(false)
                .totalFiles(totalFiles)
                .successfulUploads(0)
                .failedUploads(totalFiles)
                .message(message)
                .errorSummary(errors)
                .build();
    }
}
