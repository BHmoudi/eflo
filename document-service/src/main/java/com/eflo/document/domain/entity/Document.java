package com.eflo.document.domain.entity;

import com.eflo.document.domain.enums.AccessLevel;
import com.eflo.document.domain.enums.DocumentStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.eflo.document.domain.enums.VirusScanStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_uuid", columnList = "document_uuid"),
    @Index(name = "idx_documents_type", columnList = "document_type_id"),
    @Index(name = "idx_documents_order", columnList = "order_id"),
    @Index(name = "idx_documents_order_number", columnList = "order_number"),
    @Index(name = "idx_documents_status", columnList = "status"),
    @Index(name = "idx_documents_uploaded_by", columnList = "uploaded_by"),
    @Index(name = "idx_documents_uploaded_at", columnList = "uploaded_at"),
    @Index(name = "idx_documents_business_unit", columnList = "business_unit_id"),
    @Index(name = "idx_documents_file_hash", columnList = "file_hash"),
    @Index(name = "idx_documents_order_type", columnList = "order_id, document_type_id"),
    @Index(name = "idx_documents_order_status", columnList = "order_id, status"),
    @Index(name = "idx_documents_type_status", columnList = "document_type_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_uuid", nullable = false, unique = true)
    @Builder.Default
    private UUID documentUuid = UUID.randomUUID();

    // Document Type Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "type_code", nullable = false, length = 100)
    private String typeCode;

    // Associated Order
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    // File Information
    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 500)
    private String storedFilename;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "file_hash", length = 128)
    private String fileHash;

    // Storage Information
    @Column(name = "storage_bucket", nullable = false, length = 100)
    private String storageBucket;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @Column(name = "storage_region", length = 50)
    private String storageRegion;

    // Version Control
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_latest_version", nullable = false)
    @Builder.Default
    private Boolean isLatestVersion = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_document_id")
    private Document parentDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_document_id")
    private Document replacedByDocument;

    // Document Status
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "document_status")
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;

    // Validation
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validated_by", length = 255)
    private String validatedBy;

    @Column(name = "validation_comments", columnDefinition = "TEXT")
    private String validationComments;

    // Expiration
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "expiration_notified")
    @Builder.Default
    private Boolean expirationNotified = false;

    @Column(name = "expiration_notification_sent_at")
    private LocalDateTime expirationNotificationSentAt;

    // Virus Scanning
    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status", length = 50)
    private VirusScanStatus virusScanStatus;

    @Column(name = "virus_scan_date")
    private LocalDateTime virusScanDate;

    @Type(JsonBinaryType.class)
    @Column(name = "virus_scan_result", columnDefinition = "jsonb")
    private Map<String, Object> virusScanResult;

    // Metadata
    @Type(JsonBinaryType.class)
    @Column(name = "custom_metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> customMetadata = new HashMap<>();

    @Column(name = "tags", columnDefinition = "text[]")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Security
    @Column(name = "is_confidential")
    @Builder.Default
    private Boolean isConfidential = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 50)
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.STANDARD;

    @Column(name = "encryption_enabled")
    @Builder.Default
    private Boolean encryptionEnabled = false;

    // Business Context
    @Column(name = "business_unit_id")
    private Long businessUnitId;

    @Column(name = "uploaded_by", nullable = false, length = 255)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // Audit Fields
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 255)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (documentUuid == null) {
            documentUuid = UUID.randomUUID();
        }
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    /**
     * Check if document is in pending status
     */
    public boolean isPending() {
        return status == DocumentStatus.PENDING;
    }

    /**
     * Check if document is validated
     */
    public boolean isValidated() {
        return status == DocumentStatus.VALIDATED;
    }

    /**
     * Check if document is rejected
     */
    public boolean isRejected() {
        return status == DocumentStatus.REJECTED;
    }

    /**
     * Check if document is expired
     */
    public boolean isExpired() {
        return status == DocumentStatus.EXPIRED ||
               (expirationDate != null && expirationDate.isBefore(LocalDate.now()));
    }

    /**
     * Check if document is archived
     */
    public boolean isArchived() {
        return status == DocumentStatus.ARCHIVED;
    }

    /**
     * Check if document is soft deleted
     */
    public boolean isDeleted() {
        return status == DocumentStatus.DELETED || deletedAt != null;
    }

    /**
     * Check if document is active (not deleted or archived)
     */
    public boolean isActive() {
        return status != null && status.isActive() && deletedAt == null;
    }

    /**
     * Check if this is the latest version
     */
    public boolean isLatest() {
        return Boolean.TRUE.equals(isLatestVersion);
    }

    /**
     * Check if document has been scanned for viruses
     */
    public boolean isVirusScanned() {
        return virusScanStatus != null && virusScanStatus != VirusScanStatus.PENDING;
    }

    /**
     * Check if document is safe (virus scan clean)
     */
    public boolean isVirusSafe() {
        return virusScanStatus != null && virusScanStatus.isSafe();
    }

    /**
     * Check if document requires virus scan action
     */
    public boolean requiresVirusScanAction() {
        return virusScanStatus != null && virusScanStatus.requiresAction();
    }

    /**
     * Check if document is expiring soon (within configured warning days)
     */
    public boolean isExpiringSoon(int warningDays) {
        if (expirationDate == null) {
            return false;
        }
        LocalDate warningDate = LocalDate.now().plusDays(warningDays);
        return expirationDate.isBefore(warningDate) && !expirationDate.isBefore(LocalDate.now());
    }

    /**
     * Check if expiration notification is needed
     */
    public boolean needsExpirationNotification(int warningDays) {
        return !Boolean.TRUE.equals(expirationNotified) && isExpiringSoon(warningDays);
    }

    /**
     * Check if document is confidential or has restricted access
     */
    public boolean hasRestrictedAccess() {
        return Boolean.TRUE.equals(isConfidential) ||
               accessLevel != AccessLevel.STANDARD;
    }

    /**
     * Get file size in megabytes
     */
    public double getFileSizeMB() {
        return fileSizeBytes != null ? fileSizeBytes / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * Add a tag to the document
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Remove a tag from the document
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    /**
     * Check if document has a specific tag
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    /**
     * Add custom metadata entry
     */
    public void addMetadata(String key, Object value) {
        if (customMetadata == null) {
            customMetadata = new HashMap<>();
        }
        customMetadata.put(key, value);
    }

    /**
     * Get custom metadata value
     */
    public Object getMetadata(String key) {
        return customMetadata != null ? customMetadata.get(key) : null;
    }

    /**
     * Mark document as validated
     */
    public void markAsValidated(String validatedBy, String comments) {
        this.status = DocumentStatus.VALIDATED;
        this.validatedBy = validatedBy;
        this.validatedAt = LocalDateTime.now();
        this.validationComments = comments;
    }

    /**
     * Mark document as rejected
     */
    public void markAsRejected(String validatedBy, String reason) {
        this.status = DocumentStatus.REJECTED;
        this.validatedBy = validatedBy;
        this.validatedAt = LocalDateTime.now();
        this.statusReason = reason;
    }

    /**
     * Mark document as deleted (soft delete)
     */
    public void markAsDeleted(String deletedBy) {
        this.status = DocumentStatus.DELETED;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Mark document as archived
     */
    public void markAsArchived() {
        this.status = DocumentStatus.ARCHIVED;
    }

    /**
     * Record virus scan result
     */
    public void recordVirusScan(VirusScanStatus scanStatus, Map<String, Object> scanResult) {
        this.virusScanStatus = scanStatus;
        this.virusScanDate = LocalDateTime.now();
        this.virusScanResult = scanResult;
    }

    /**
     * Mark expiration notification as sent
     */
    public void markExpirationNotificationSent() {
        this.expirationNotified = true;
        this.expirationNotificationSentAt = LocalDateTime.now();
    }
}
