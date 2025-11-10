package com.eflo.document.domain.entity;

import com.eflo.document.domain.enums.AccessAction;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "document_access_log", indexes = {
    @Index(name = "idx_access_log_document", columnList = "document_id"),
    @Index(name = "idx_access_log_document_uuid", columnList = "document_uuid"),
    @Index(name = "idx_access_log_user", columnList = "user_id"),
    @Index(name = "idx_access_log_action", columnList = "action"),
    @Index(name = "idx_access_log_accessed_at", columnList = "accessed_at"),
    @Index(name = "idx_access_log_business_unit", columnList = "business_unit_id"),
    @Index(name = "idx_access_log_doc_user", columnList = "document_id, user_id"),
    @Index(name = "idx_access_log_user_action", columnList = "user_id, action, accessed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_uuid", nullable = false, unique = true)
    @Builder.Default
    private UUID logUuid = UUID.randomUUID();

    // Document Reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "document_uuid")
    private UUID documentUuid;

    @Column(name = "document_filename", length = 500)
    private String documentFilename;

    // Access Details
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AccessAction action;

    @Column(name = "action_description", columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name = "action_result", nullable = false, length = 50)
    private String actionResult; // SUCCESS, FAILURE, PARTIAL

    // User Context
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "user_role", length = 100)
    private String userRole;

    @Column(name = "business_unit_id")
    private Long businessUnitId;

    // Technical Context
    @Column(name = "ip_address", length = 45) // IPv6 compatible
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    // Request Details
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_path", length = 500)
    private String requestPath;

    @Column(name = "response_status")
    private Integer responseStatus;

    // Metadata
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Timestamp
    @Column(name = "accessed_at", nullable = false)
    @Builder.Default
    private LocalDateTime accessedAt = LocalDateTime.now();

    // Data Retention
    @Column(name = "retention_until")
    private LocalDate retentionUntil;

    @PrePersist
    protected void onCreate() {
        if (accessedAt == null) {
            accessedAt = LocalDateTime.now();
        }
        if (logUuid == null) {
            logUuid = UUID.randomUUID();
        }
    }

    // Helper Methods

    /**
     * Check if the action was successful
     */
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(actionResult);
    }

    /**
     * Check if the action failed
     */
    public boolean isFailure() {
        return "FAILURE".equalsIgnoreCase(actionResult);
    }

    /**
     * Check if the action was partially successful
     */
    public boolean isPartial() {
        return "PARTIAL".equalsIgnoreCase(actionResult);
    }

    /**
     * Check if this is a modifying action
     */
    public boolean isModifyingAction() {
        return action != null && action.isModifyingAction();
    }

    /**
     * Check if this is a read-only action
     */
    public boolean isReadOnlyAction() {
        return action != null && action.isReadOnlyAction();
    }

    /**
     * Check if retention period has expired
     */
    public boolean isRetentionExpired() {
        return retentionUntil != null && retentionUntil.isBefore(LocalDate.now());
    }

    /**
     * Check if log entry should be retained
     */
    public boolean shouldRetain() {
        return retentionUntil == null || !isRetentionExpired();
    }

    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Set retention period from now (in days)
     */
    public void setRetentionDays(int days) {
        this.retentionUntil = LocalDate.now().plusDays(days);
    }

    /**
     * Check if the access was from a specific IP
     */
    public boolean isFromIp(String ip) {
        return ipAddress != null && ipAddress.equals(ip);
    }

    /**
     * Check if the access was by a specific user
     */
    public boolean isBy(String userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    /**
     * Get a summary of the log entry
     */
    public String getSummary() {
        return String.format("%s - %s by %s (%s)",
            action != null ? action.name() : "UNKNOWN",
            actionResult,
            userId,
            accessedAt);
    }

    /**
     * Check if this log entry has error information
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    /**
     * Create a success log entry
     */
    public static DocumentAccessLog success(Document document, AccessAction action, String userId) {
        return DocumentAccessLog.builder()
            .document(document)
            .documentUuid(document != null ? document.getDocumentUuid() : null)
            .documentFilename(document != null ? document.getOriginalFilename() : null)
            .action(action)
            .actionResult("SUCCESS")
            .userId(userId)
            .build();
    }

    /**
     * Create a failure log entry
     */
    public static DocumentAccessLog failure(Document document, AccessAction action, String userId, String errorMessage) {
        return DocumentAccessLog.builder()
            .document(document)
            .documentUuid(document != null ? document.getDocumentUuid() : null)
            .documentFilename(document != null ? document.getOriginalFilename() : null)
            .action(action)
            .actionResult("FAILURE")
            .userId(userId)
            .errorMessage(errorMessage)
            .build();
    }
}
