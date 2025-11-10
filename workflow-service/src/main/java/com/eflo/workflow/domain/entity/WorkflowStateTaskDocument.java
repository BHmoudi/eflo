package com.eflo.workflow.domain.entity;

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
 * WorkflowStateTaskDocument Entity
 *
 * Defines document requirements for workflow state tasks.
 * Links tasks to document types that must be uploaded for task completion.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_state_task_documents",
       uniqueConstraints = @UniqueConstraint(
           name = "unique_doc_per_task",
           columnNames = {"state_task_id", "document_type_code"}
       ),
       indexes = {
           @Index(name = "idx_task_docs_state_task", columnList = "state_task_id"),
           @Index(name = "idx_task_docs_type_code", columnList = "document_type_code"),
           @Index(name = "idx_task_docs_mandatory", columnList = "is_mandatory")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkflowStateTaskDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_task_id", nullable = false)
    private WorkflowStateTask stateTask;

    /**
     * Document type code from document-service
     * e.g., "INVOICE", "CONTRACT", "IDENTITY_CARD"
     */
    @Column(name = "document_type_code", nullable = false, length = 50)
    private String documentTypeCode;

    /**
     * Whether this document is mandatory for task completion
     */
    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    /**
     * Deadline in hours from when the state is entered
     */
    @Column(name = "upload_deadline_hours")
    private Integer uploadDeadlineHours;

    /**
     * Whether uploaded document requires validation
     */
    @Column(name = "validation_required", nullable = false)
    @Builder.Default
    private Boolean validationRequired = true;

    /**
     * Display order for UI
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Description or instructions for this document requirement
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
