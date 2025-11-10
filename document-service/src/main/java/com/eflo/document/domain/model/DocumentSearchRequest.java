package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.AccessLevel;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for searching documents with various filter criteria.
 * Supports filtering, sorting, and pagination.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {

    // Basic Filters

    /**
     * Search term for filename, description, or tags.
     */
    @Size(max = 255, message = "Search term must not exceed 255 characters")
    private String searchTerm;

    /**
     * Filter by document type ID.
     */
    private Long documentTypeId;

    /**
     * Filter by document type code.
     */
    @Size(max = 100, message = "Type code must not exceed 100 characters")
    private String typeCode;

    /**
     * Filter by order ID.
     */
    private Long orderId;

    /**
     * Filter by order number.
     */
    @Size(max = 100, message = "Order number must not exceed 100 characters")
    private String orderNumber;

    /**
     * Filter by document status.
     */
    private DocumentStatus status;

    /**
     * Filter by multiple document statuses.
     */
    private List<DocumentStatus> statuses;

    // User Filters

    /**
     * Filter by uploaded by user.
     */
    @Size(max = 255, message = "Uploaded by must not exceed 255 characters")
    private String uploadedBy;

    /**
     * Filter by validated by user.
     */
    @Size(max = 255, message = "Validated by must not exceed 255 characters")
    private String validatedBy;

    // Date Range Filters

    /**
     * Filter by upload date from (inclusive).
     */
    private LocalDateTime uploadedFrom;

    /**
     * Filter by upload date to (inclusive).
     */
    private LocalDateTime uploadedTo;

    /**
     * Filter by validation date from (inclusive).
     */
    private LocalDateTime validatedFrom;

    /**
     * Filter by validation date to (inclusive).
     */
    private LocalDateTime validatedTo;

    /**
     * Filter by expiration date from (inclusive).
     */
    private LocalDate expirationFrom;

    /**
     * Filter by expiration date to (inclusive).
     */
    private LocalDate expirationTo;

    // Security Filters

    /**
     * Filter by confidential flag.
     */
    private Boolean isConfidential;

    /**
     * Filter by access level.
     */
    private AccessLevel accessLevel;

    /**
     * Filter by virus scan status.
     */
    private VirusScanStatus virusScanStatus;

    // Version Filters

    /**
     * Filter by latest version only.
     */
    @Builder.Default
    private Boolean latestVersionOnly = true;

    /**
     * Filter by specific version number.
     */
    @Min(value = 1, message = "Version must be at least 1")
    private Integer version;

    // Tag Filters

    /**
     * Filter by tags (documents must have all specified tags).
     */
    @Size(max = 10, message = "Cannot filter by more than 10 tags")
    private List<@Size(max = 50) String> tags;

    /**
     * Filter by any of the specified tags (OR operation).
     */
    @Size(max = 10, message = "Cannot filter by more than 10 tags")
    private List<@Size(max = 50) String> anyTags;

    // File Filters

    /**
     * Filter by file extension.
     */
    @Size(max = 20, message = "File extension must not exceed 20 characters")
    private String fileExtension;

    /**
     * Filter by MIME type.
     */
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;

    /**
     * Filter by minimum file size in bytes.
     */
    @Min(value = 0, message = "Minimum file size must be 0 or greater")
    private Long minFileSizeBytes;

    /**
     * Filter by maximum file size in bytes.
     */
    @Min(value = 1, message = "Maximum file size must be at least 1")
    private Long maxFileSizeBytes;

    // Business Context Filters

    /**
     * Filter by business unit ID.
     */
    private Long businessUnitId;

    // Special Filters

    /**
     * Filter documents that are expiring soon (within specified days).
     */
    @Min(value = 1, message = "Expiring within days must be at least 1")
    private Integer expiringWithinDays;

    /**
     * Include deleted documents in search results.
     */
    @Builder.Default
    private Boolean includeDeleted = false;

    /**
     * Include archived documents in search results.
     */
    @Builder.Default
    private Boolean includeArchived = false;

    // Sorting

    /**
     * Field to sort by (e.g., uploadedAt, fileName, fileSize).
     */
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*$", message = "Sort by field must be a valid field name")
    private String sortBy;

    /**
     * Sort direction (ASC or DESC).
     */
    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be ASC or DESC")
    @Builder.Default
    private String sortDirection = "DESC";

    // Pagination

    /**
     * Page number (0-based).
     */
    @Min(value = 0, message = "Page must be 0 or greater")
    @Builder.Default
    private Integer page = 0;

    /**
     * Page size (number of results per page).
     */
    @Min(value = 1, message = "Size must be at least 1")
    @Builder.Default
    private Integer size = 20;

    /**
     * Check if any date range filter is applied.
     *
     * @return true if any date range filter is set
     */
    public boolean hasDateRangeFilter() {
        return uploadedFrom != null || uploadedTo != null ||
               validatedFrom != null || validatedTo != null ||
               expirationFrom != null || expirationTo != null;
    }

    /**
     * Check if any tag filter is applied.
     *
     * @return true if any tag filter is set
     */
    public boolean hasTagFilter() {
        return (tags != null && !tags.isEmpty()) ||
               (anyTags != null && !anyTags.isEmpty());
    }

    /**
     * Check if file size filter is applied.
     *
     * @return true if file size filter is set
     */
    public boolean hasFileSizeFilter() {
        return minFileSizeBytes != null || maxFileSizeBytes != null;
    }
}
