package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Summary DTO for validation operations.
 * Contains aggregated results of validation operations for multiple documents.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationSummary {

    /**
     * Order ID for which validation was performed.
     */
    private Long orderId;

    /**
     * Total number of documents processed.
     */
    private Integer totalDocuments;

    /**
     * Number of successfully validated documents.
     */
    private Integer validatedCount;

    /**
     * Number of documents that failed validation.
     */
    private Integer failedCount;

    /**
     * Number of documents that were rejected.
     */
    private Integer rejectedCount;

    /**
     * Number of documents that are still pending.
     */
    private Integer pendingCount;

    /**
     * Flag indicating if all documents were validated successfully.
     */
    private Boolean allValidated;

    /**
     * User who performed the validation.
     */
    private String validatedBy;

    /**
     * Timestamp when validation was performed.
     */
    private LocalDateTime validatedAt;

    /**
     * List of validation errors encountered.
     */
    private List<String> errors;

    /**
     * List of validation warnings.
     */
    private List<String> warnings;

    /**
     * Overall message about the validation result.
     */
    private String message;

    /**
     * Processing time in milliseconds.
     */
    private Long processingTimeMs;

    /**
     * Check if there were any failures.
     *
     * @return true if any documents failed validation
     */
    public boolean hasFailures() {
        return failedCount != null && failedCount > 0;
    }

    /**
     * Check if there were any errors.
     *
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if there were any warnings.
     *
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Calculate success rate percentage.
     *
     * @return success rate (0-100)
     */
    public Double getSuccessRate() {
        if (totalDocuments == null || totalDocuments == 0) {
            return 0.0;
        }
        return (validatedCount != null ? validatedCount : 0) * 100.0 / totalDocuments;
    }

    /**
     * Get the first error message.
     *
     * @return first error or empty string
     */
    public String getFirstError() {
        return errors != null && !errors.isEmpty() ? errors.get(0) : "";
    }

    /**
     * Get all errors as a single concatenated string.
     *
     * @return concatenated error messages
     */
    public String getAllErrors() {
        if (errors == null || errors.isEmpty()) {
            return "";
        }
        return String.join("; ", errors);
    }
}
