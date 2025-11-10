package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.AccessLevel;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO containing complete document information.
 * Used for detailed document retrieval operations.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    /**
     * Internal database ID of the document.
     */
    private Long id;

    /**
     * Unique UUID identifier for the document.
     */
    private UUID documentUuid;

    // Document Type Information

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

    // Associated Order

    /**
     * Associated order ID.
     */
    private Long orderId;

    /**
     * Associated order number.
     */
    private String orderNumber;

    // File Information

    /**
     * Original filename as uploaded.
     */
    private String originalFilename;

    /**
     * Filename as stored in the system.
     */
    private String storedFilename;

    /**
     * File extension (e.g., pdf, jpg).
     */
    private String fileExtension;

    /**
     * MIME type of the file.
     */
    private String mimeType;

    /**
     * File size in bytes.
     */
    private Long fileSizeBytes;

    /**
     * File size in megabytes (calculated field).
     */
    private Double fileSizeMB;

    /**
     * Hash of the file content for integrity verification.
     */
    private String fileHash;

    // Storage Information

    /**
     * Storage information including bucket, path, and URL.
     */
    private StorageInfo storageInfo;

    // Version Information

    /**
     * Document version information.
     */
    private DocumentVersion versionInfo;

    /**
     * Parent document ID if this is a version.
     */
    private Long parentDocumentId;

    /**
     * ID of the document that replaced this one.
     */
    private Long replacedByDocumentId;

    // Status Information

    /**
     * Current status of the document.
     */
    private DocumentStatus status;

    /**
     * Reason for the current status (especially for rejections).
     */
    private String statusReason;

    // Validation Information

    /**
     * Validation timestamp.
     */
    private LocalDateTime validatedAt;

    /**
     * User who validated the document.
     */
    private String validatedBy;

    /**
     * Comments from validation.
     */
    private String validationComments;

    // Expiration Information

    /**
     * Expiration information including date and status.
     */
    private ExpirationInfo expirationInfo;

    // Virus Scan Information

    /**
     * Virus scan result information.
     */
    private ScanResult scanResult;

    // Metadata

    /**
     * Custom metadata as key-value pairs.
     */
    private Map<String, Object> customMetadata;

    /**
     * Tags associated with the document.
     */
    private List<String> tags;

    /**
     * Document description.
     */
    private String description;

    // Security

    /**
     * Flag indicating if document is confidential.
     */
    private Boolean isConfidential;

    /**
     * Access level of the document.
     */
    private AccessLevel accessLevel;

    /**
     * Flag indicating if encryption is enabled.
     */
    private Boolean encryptionEnabled;

    // Business Context

    /**
     * Business unit ID.
     */
    private Long businessUnitId;

    /**
     * User who uploaded the document.
     */
    private String uploadedBy;

    /**
     * Upload timestamp.
     */
    private LocalDateTime uploadedAt;

    // Audit Information

    /**
     * User who created the record.
     */
    private String createdBy;

    /**
     * Record creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * User who last updated the record.
     */
    private String updatedBy;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp.
     */
    private LocalDateTime deletedAt;

    /**
     * User who deleted the document.
     */
    private String deletedBy;

    // Computed Fields

    /**
     * Flag indicating if this is the latest version.
     */
    private Boolean isLatestVersion;

    /**
     * Flag indicating if document is active.
     */
    private Boolean isActive;

    /**
     * Flag indicating if document is expired.
     */
    private Boolean isExpired;

    /**
     * Flag indicating if document has restricted access.
     */
    private Boolean hasRestrictedAccess;

    /**
     * Download URL for the document (if available).
     */
    private String downloadUrl;

    /**
     * Preview URL for the document (if available).
     */
    private String previewUrl;
}
