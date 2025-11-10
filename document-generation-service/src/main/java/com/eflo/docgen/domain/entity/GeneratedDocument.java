package com.eflo.docgen.domain.entity;

import com.eflo.docgen.domain.enums.GenerationStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Generated Document Entity
 *
 * Tracks documents that have been generated from templates
 */
@Entity
@Table(name = "generated_documents", indexes = {
    @Index(name = "idx_generated_order", columnList = "order_id"),
    @Index(name = "idx_generated_workflow", columnList = "workflow_instance_id"),
    @Index(name = "idx_generated_template", columnList = "template_id"),
    @Index(name = "idx_generated_status", columnList = "generation_status"),
    @Index(name = "idx_generated_uuid", columnList = "document_uuid")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GeneratedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_uuid", nullable = false, unique = true)
    @Builder.Default
    private String documentUuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DocumentTemplate template;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "workflow_instance_id")
    private Long workflowInstanceId;

    /**
     * Generated document content (PDF/DOCX)
     */
    @Lob
    @Column(name = "generated_content", columnDefinition = "BYTEA")
    private byte[] generatedContent;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "filename")
    private String filename;

    @Column(name = "mime_type")
    private String mimeType;

    /**
     * Actual variable values used during generation
     */
    @Type(JsonBinaryType.class)
    @Column(name = "variables_used", columnDefinition = "jsonb")
    private Map<String, Object> variablesUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 30)
    @Builder.Default
    private GenerationStatus generationStatus = GenerationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Whether this document has been uploaded to document-service
     */
    @Column(name = "uploaded_to_document_service")
    @Builder.Default
    private Boolean uploadedToDocumentService = false;

    @Column(name = "document_service_id")
    private Long documentServiceId;

    /**
     * Whether approval is required (from configured roles)
     */
    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = false;

    /**
     * Roles allowed to approve this document (from template)
     */
    @Column(name = "approval_roles")
    private String approvalRoles;

    /**
     * Validation/Approval status
     */
    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_by_role")
    private String approvedByRole;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_comments", columnDefinition = "TEXT")
    private String approvalComments;

    @CreatedDate
    @Column(name = "generation_date", nullable = false, updatable = false)
    private LocalDateTime generationDate;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    /**
     * Mark as completed
     */
    public void markAsCompleted(byte[] content, String filename, String mimeType) {
        this.generationStatus = GenerationStatus.COMPLETED;
        this.generatedContent = content;
        this.filename = filename;
        this.mimeType = mimeType;
        this.fileSizeBytes = content != null ? (long) content.length : 0L;
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String errorMessage) {
        this.generationStatus = GenerationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * Mark as approved by authorized role
     */
    public void markAsApproved(String approvedBy, String approvedByRole, String comments) {
        this.isApproved = true;
        this.approvedBy = approvedBy;
        this.approvedByRole = approvedByRole;
        this.approvalDate = LocalDateTime.now();
        this.approvalComments = comments;
    }

    /**
     * Reject approval
     */
    public void markAsRejected(String rejectedBy, String comments) {
        this.isApproved = false;
        this.approvedBy = rejectedBy;
        this.approvalDate = LocalDateTime.now();
        this.approvalComments = comments;
    }
}
