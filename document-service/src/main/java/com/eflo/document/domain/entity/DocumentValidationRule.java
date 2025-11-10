package com.eflo.document.domain.entity;

import com.eflo.document.domain.enums.ValidationRuleType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "document_validation_rules", indexes = {
    @Index(name = "idx_validation_rules_type", columnList = "rule_type"),
    @Index(name = "idx_validation_rules_document_type", columnList = "document_type_id"),
    @Index(name = "idx_validation_rules_active", columnList = "is_active"),
    @Index(name = "idx_validation_rules_execution_order", columnList = "execution_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Rule Configuration
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private ValidationRuleType ruleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentType documentType;

    @Column(name = "applies_to_all_types")
    @Builder.Default
    private Boolean appliesToAllTypes = false;

    // Rule Definition
    @Type(JsonBinaryType.class)
    @Column(name = "rule_configuration", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> ruleConfiguration = new HashMap<>();

    @Column(name = "validation_script", columnDefinition = "TEXT")
    private String validationScript;

    // Rule Behavior
    @Column(name = "is_blocking", nullable = false)
    @Builder.Default
    private Boolean isBlocking = true;

    @Column(name = "execution_order")
    @Builder.Default
    private Integer executionOrder = 0;

    @Column(name = "error_message_template", length = 500)
    private String errorMessageTemplate;

    // Conditions
    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Type(JsonBinaryType.class)
    @Column(name = "enabled_when", columnDefinition = "jsonb")
    private Map<String, Object> enabledWhen;

    // Status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Audit Fields
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    /**
     * Check if rule is currently active
     */
    public boolean isRuleActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Check if rule is blocking (prevents document acceptance on failure)
     */
    public boolean isBlockingRule() {
        return Boolean.TRUE.equals(isBlocking);
    }

    /**
     * Check if rule applies to all document types
     */
    public boolean appliesToAllDocumentTypes() {
        return Boolean.TRUE.equals(appliesToAllTypes);
    }

    /**
     * Check if rule applies to a specific document type
     */
    public boolean appliesTo(DocumentType type) {
        if (appliesToAllDocumentTypes()) {
            return true;
        }
        return documentType != null && documentType.equals(type);
    }

    /**
     * Check if rule applies to a document type by ID
     */
    public boolean appliesToTypeId(Long typeId) {
        if (appliesToAllDocumentTypes()) {
            return true;
        }
        return documentType != null && documentType.getId().equals(typeId);
    }

    /**
     * Get configuration value by key
     */
    public Object getConfigValue(String key) {
        return ruleConfiguration != null ? ruleConfiguration.get(key) : null;
    }

    /**
     * Get configuration value as String
     */
    public String getConfigString(String key) {
        Object value = getConfigValue(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get configuration value as Integer
     */
    public Integer getConfigInteger(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Get configuration value as Long
     */
    public Long getConfigLong(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * Get configuration value as Boolean
     */
    public Boolean getConfigBoolean(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    /**
     * Set configuration value
     */
    public void setConfigValue(String key, Object value) {
        if (ruleConfiguration == null) {
            ruleConfiguration = new HashMap<>();
        }
        ruleConfiguration.put(key, value);
    }

    /**
     * Check if rule has a configuration key
     */
    public boolean hasConfigKey(String key) {
        return ruleConfiguration != null && ruleConfiguration.containsKey(key);
    }

    /**
     * Get the error message, replacing placeholders if needed
     */
    public String getErrorMessage(Map<String, String> placeholders) {
        if (errorMessageTemplate == null) {
            return String.format("Validation rule '%s' failed", ruleName);
        }

        String message = errorMessageTemplate;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }

    /**
     * Get the default error message
     */
    public String getErrorMessage() {
        return getErrorMessage(null);
    }

    /**
     * Check if rule has a validation script
     */
    public boolean hasValidationScript() {
        return validationScript != null && !validationScript.trim().isEmpty();
    }

    /**
     * Check if rule has conditions
     */
    public boolean hasConditions() {
        return (conditionExpression != null && !conditionExpression.trim().isEmpty()) ||
               (enabledWhen != null && !enabledWhen.isEmpty());
    }

    /**
     * Activate the rule
     */
    public void activate(String updatedBy) {
        this.isActive = true;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivate the rule
     */
    public void deactivate(String updatedBy) {
        this.isActive = false;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this is a file size validation rule
     */
    public boolean isFileSizeRule() {
        return ruleType == ValidationRuleType.FILE_SIZE;
    }

    /**
     * Check if this is a file format validation rule
     */
    public boolean isFileFormatRule() {
        return ruleType == ValidationRuleType.FILE_FORMAT;
    }

    /**
     * Check if this is a content check rule
     */
    public boolean isContentCheckRule() {
        return ruleType == ValidationRuleType.CONTENT_CHECK;
    }

    /**
     * Check if this is a business rule
     */
    public boolean isBusinessRule() {
        return ruleType == ValidationRuleType.BUSINESS_RULE;
    }

    /**
     * Check if this is a custom script rule
     */
    public boolean isCustomScriptRule() {
        return ruleType == ValidationRuleType.CUSTOM_SCRIPT;
    }

    /**
     * Compare rules by execution order
     */
    public int compareByExecutionOrder(DocumentValidationRule other) {
        if (this.executionOrder == null && other.executionOrder == null) {
            return 0;
        }
        if (this.executionOrder == null) {
            return 1;
        }
        if (other.executionOrder == null) {
            return -1;
        }
        return this.executionOrder.compareTo(other.executionOrder);
    }
}
