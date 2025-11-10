package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing lightweight document information.
 * Used for list views and search results to improve performance.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummaryResponse {

    /**
     * Internal database ID of the document.
     */
    private Long id;

    /**
     * Unique UUID identifier for the document.
     */
    private UUID documentUuid;

    /**
     * Document type ID.
     */
    private Long documentTypeId;

    /**
     * Document type code.
     */
    private String typeCode;

    /**
     * Document type name.
     */
    private String typeName;

    /**
     * Associated order ID.
     */
    private Long orderId;

    /**
     * Associated order number.
     */
    private String orderNumber;

    /**
     * Original filename.
     */
    private String originalFilename;

    /**
     * File extension.
     */
    private String fileExtension;

    /**
     * File size in megabytes.
     */
    private Double fileSizeMB;

    /**
     * Current document status.
     */
    private DocumentStatus status;

    /**
     * Document version number.
     */
    private Integer version;

    /**
     * Flag indicating if this is the latest version.
     */
    private Boolean isLatestVersion;

    /**
     * Expiration date of the document.
     */
    private LocalDate expirationDate;

    /**
     * Flag indicating if document is expired.
     */
    private Boolean isExpired;

    /**
     * Flag indicating if document is expiring soon.
     */
    private Boolean isExpiringSoon;

    /**
     * Tags associated with the document.
     */
    private List<String> tags;

    /**
     * Brief description of the document.
     */
    private String description;

    /**
     * Flag indicating if document is confidential.
     */
    private Boolean isConfidential;

    /**
     * User who uploaded the document.
     */
    private String uploadedBy;

    /**
     * Upload timestamp.
     */
    private LocalDateTime uploadedAt;

    /**
     * Validation timestamp.
     */
    private LocalDateTime validatedAt;

    /**
     * User who validated the document.
     */
    private String validatedBy;

    /**
     * Download URL for the document (if available).
     */
    private String downloadUrl;

    /**
     * Thumbnail URL for preview (if available).
     */
    private String thumbnailUrl;
}
