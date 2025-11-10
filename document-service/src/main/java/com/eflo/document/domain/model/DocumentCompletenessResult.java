package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result DTO for document completeness check for an order.
 * Contains information about missing and incomplete document types.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCompletenessResult {

    /**
     * Order ID being checked.
     */
    private Long orderId;

    /**
     * Flag indicating if all required documents are complete.
     */
    private Boolean isComplete;

    /**
     * Total number of required document types.
     */
    private Integer totalRequiredTypes;

    /**
     * Number of satisfied document types.
     */
    private Integer satisfiedTypes;

    /**
     * List of missing mandatory document type codes.
     */
    private List<String> missingDocumentTypes;

    /**
     * List of incomplete document type codes (minimum count not met).
     */
    private List<String> incompleteDocumentTypes;

    /**
     * Total number of documents uploaded for the order.
     */
    private Integer totalDocuments;

    /**
     * Number of validated documents.
     */
    private Integer validatedDocuments;

    /**
     * Number of pending documents.
     */
    private Integer pendingDocuments;

    /**
     * Number of rejected documents.
     */
    private Integer rejectedDocuments;

    /**
     * Overall message about completeness status.
     */
    private String message;

    /**
     * Completion percentage (0-100).
     */
    private Double completionPercentage;

    /**
     * Check if there are missing document types.
     *
     * @return true if any mandatory types are missing
     */
    public boolean hasMissingTypes() {
        return missingDocumentTypes != null && !missingDocumentTypes.isEmpty();
    }

    /**
     * Check if there are incomplete document types.
     *
     * @return true if any types don't meet minimum requirements
     */
    public boolean hasIncompleteTypes() {
        return incompleteDocumentTypes != null && !incompleteDocumentTypes.isEmpty();
    }

    /**
     * Calculate completion percentage.
     *
     * @return completion percentage
     */
    public Double calculateCompletionPercentage() {
        if (totalRequiredTypes == null || totalRequiredTypes == 0) {
            return 100.0;
        }
        if (satisfiedTypes == null) {
            return 0.0;
        }
        return (satisfiedTypes * 100.0) / totalRequiredTypes;
    }

    /**
     * Get count of issues (missing + incomplete).
     *
     * @return total issue count
     */
    public int getIssueCount() {
        int count = 0;
        if (missingDocumentTypes != null) count += missingDocumentTypes.size();
        if (incompleteDocumentTypes != null) count += incompleteDocumentTypes.size();
        return count;
    }
}
