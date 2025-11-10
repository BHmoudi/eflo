package com.eflo.docgen.domain.entity;

import com.eflo.docgen.domain.enums.TemplateFormat;
import com.eflo.docgen.domain.enums.TemplateType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Document Template Entity
 *
 * Stores reusable document templates (DOCX, HTML, PDF)
 * with placeholder variables for dynamic content generation
 */
@Entity
@Table(name = "document_templates", indexes = {
    @Index(name = "idx_template_code", columnList = "template_code"),
    @Index(name = "idx_template_type", columnList = "template_type"),
    @Index(name = "idx_template_active", columnList = "is_active"),
    @Index(name = "idx_template_language", columnList = "language")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 50)
    private TemplateType templateType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_format", nullable = false, length = 20)
    private TemplateFormat templateFormat;

    /**
     * Template file content stored as binary
     */
    @Lob
    @Column(name = "template_content", columnDefinition = "BYTEA")
    private byte[] templateContent;

    @Column(name = "template_size_bytes")
    private Long templateSizeBytes;

    @Column(name = "original_filename")
    private String originalFilename;

    /**
     * List of placeholder variables with metadata
     * Example: [
     *   {"name": "orderNumber", "type": "string", "description": "Order number"},
     *   {"name": "customerName", "type": "string", "description": "Customer full name"},
     *   {"name": "totalPrice", "type": "number", "format": "currency"}
     * ]
     */
    @Type(JsonBinaryType.class)
    @Column(name = "variables", columnDefinition = "jsonb")
    private List<Map<String, Object>> variables;

    /**
     * Configuration for generation behavior
     */
    @Type(JsonBinaryType.class)
    @Column(name = "generation_config", columnDefinition = "jsonb")
    private Map<String, Object> generationConfig;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "language", length = 5)
    @Builder.Default
    private String language = "FR";

    /**
     * Order types this template applies to (VN, VO, EVO)
     */
    @Column(name = "applicable_order_types")
    private String applicableOrderTypes;

    /**
     * Workflow states that trigger auto-generation
     */
    @Type(JsonBinaryType.class)
    @Column(name = "auto_generate_on_states", columnDefinition = "jsonb")
    private List<String> autoGenerateOnStates;

    /**
     * Whether approval is required from specific roles
     */
    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    /**
     * Comma-separated list of roles that can approve this document
     * Example: "CDV,ADMIN_LOCAL,SALES_MANAGER"
     * Fetched from user-service at runtime
     */
    @Column(name = "approval_roles")
    private String approvalRoles;

    /**
     * Whether to block workflow progression until validated
     */
    @Column(name = "blocks_workflow_progression", nullable = false)
    @Builder.Default
    private Boolean blocksWorkflowProgression = false;

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
}
