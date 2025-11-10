package com.eflo.document.domain.entity;

import com.eflo.document.domain.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * DocumentPlaceholder Entity
 *
 * Represents a required document that must be uploaded for an order's workflow.
 * Placeholders are created automatically when a workflow instance is created.
 *
 * @author Document Service
 * @version 1.0.0
 */
@Entity
@Table(name = "document_placeholders",
       uniqueConstraints = @UniqueConstraint(
           name = "unique_placeholder_per_order_workflow_type",
           columnNames = {"order_id", "workflow_instance_id", "document_type_code"}
       ),
       indexes = {
           @Index(name = "idx_doc_placeholders_order", columnList = "order_id"),
           @Index(name = "idx_doc_placeholders_instance", columnList = "workflow_instance_id"),
           @Index(name = "idx_doc_placeholders_type", columnList = "document_type_code"),
           @Index(name = "idx_doc_placeholders_status", columnList = "status"),
           @Index(name = "idx_doc_placeholders_mandatory", columnList = "is_mandatory"),
           @Index(name = "idx_doc_placeholders_deadline", columnList = "deadline")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentPlaceholder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Order ID this document belongs to
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * Workflow instance ID this document is required for
     */
    @Column(name = "workflow_instance_id", nullable = false)
    private Long workflowInstanceId;

    /**
     * Document type ID from document_types table
     */
    @Column(name = "document_type_id", nullable = false)
    private Long documentTypeId;

    /**
     * Document type code for quick reference
     */
    @Column(name = "document_type_code", nullable = false, length = 50)
    private String documentTypeCode;

    /**
     * Document type name for display
     */
    @Column(name = "document_type_name")
    private String documentTypeName;

    /**
     * Whether this document is mandatory for workflow progression
     */
    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    /**
     * Current status of the placeholder/document
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING_UPLOAD;

    /**
     * Reference to actual uploaded document (when uploaded)
     */
    @Column(name = "uploaded_document_id")
    private Long uploadedDocumentId;

    /**
     * Deadline for uploading this document
     */
    @Column(name = "deadline")
    private LocalDateTime deadline;

    /**
     * Whether validation is required
     */
    @Column(name = "validation_required", nullable = false)
    @Builder.Default
    private Boolean validationRequired = true;

    /**
     * Description or instructions for this document
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Task code from workflow this document is associated with
     */
    @Column(name = "task_code", length = 50)
    private String taskCode;

    /**
     * Task name for reference
     */
    @Column(name = "task_name")
    private String taskName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Mark as uploaded
     */
    public void markAsUploaded(Long documentId) {
        this.uploadedDocumentId = documentId;
        this.status = DocumentStatus.UPLOADED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark as validated
     */
    public void markAsValidated() {
        this.status = DocumentStatus.VALIDATED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark as rejected
     */
    public void markAsRejected() {
        this.status = DocumentStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if overdue
     */
    public boolean isOverdue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline) &&
               status == DocumentStatus.PENDING_UPLOAD;
    }
}
