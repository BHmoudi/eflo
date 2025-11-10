package com.eflo.document.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "document_metadata",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_document_metadata_key", columnNames = {"document_id", "metadata_key"})
    },
    indexes = {
        @Index(name = "idx_metadata_document", columnList = "document_id"),
        @Index(name = "idx_metadata_key", columnList = "metadata_key"),
        @Index(name = "idx_metadata_key_value", columnList = "metadata_key, metadata_value")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Metadata Key-Value
    @Column(name = "metadata_key", nullable = false, length = 100)
    private String metadataKey;

    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private String metadataValue;

    @Column(name = "value_type", nullable = false, length = 50)
    @Builder.Default
    private String valueType = "STRING"; // STRING, NUMBER, DATE, BOOLEAN, JSON

    // Metadata Classification
    @Column(name = "is_searchable")
    @Builder.Default
    private Boolean isSearchable = true;

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = false;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;

    // Validation
    @Column(name = "validation_regex", length = 500)
    private String validationRegex;

    @Column(name = "allowed_values", columnDefinition = "text[]")
    private List<String> allowedValues;

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
     * Check if metadata is searchable
     */
    public boolean canBeSearched() {
        return Boolean.TRUE.equals(isSearchable);
    }

    /**
     * Check if metadata is encrypted
     */
    public boolean isValueEncrypted() {
        return Boolean.TRUE.equals(isEncrypted);
    }

    /**
     * Check if metadata is required
     */
    public boolean isValueRequired() {
        return Boolean.TRUE.equals(isRequired);
    }

    /**
     * Check if value type is STRING
     */
    public boolean isStringType() {
        return "STRING".equalsIgnoreCase(valueType);
    }

    /**
     * Check if value type is NUMBER
     */
    public boolean isNumberType() {
        return "NUMBER".equalsIgnoreCase(valueType);
    }

    /**
     * Check if value type is DATE
     */
    public boolean isDateType() {
        return "DATE".equalsIgnoreCase(valueType);
    }

    /**
     * Check if value type is BOOLEAN
     */
    public boolean isBooleanType() {
        return "BOOLEAN".equalsIgnoreCase(valueType);
    }

    /**
     * Check if value type is JSON
     */
    public boolean isJsonType() {
        return "JSON".equalsIgnoreCase(valueType);
    }

    /**
     * Get value as String (default)
     */
    public String getStringValue() {
        return metadataValue;
    }

    /**
     * Get value as Integer
     */
    public Integer getIntegerValue() {
        if (metadataValue == null || metadataValue.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(metadataValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as Long
     */
    public Long getLongValue() {
        if (metadataValue == null || metadataValue.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(metadataValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as Double
     */
    public Double getDoubleValue() {
        if (metadataValue == null || metadataValue.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(metadataValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as Boolean
     */
    public Boolean getBooleanValue() {
        if (metadataValue == null || metadataValue.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(metadataValue);
    }

    /**
     * Get value as LocalDate
     */
    public LocalDate getDateValue() {
        if (metadataValue == null || metadataValue.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(metadataValue);
        } catch (DateTimeParseException e) {
            // Try different formats
            try {
                return LocalDate.parse(metadataValue, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    /**
     * Get value as LocalDateTime
     */
    public LocalDateTime getDateTimeValue() {
        if (metadataValue == null || metadataValue.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(metadataValue);
        } catch (DateTimeParseException e) {
            // Try different formats
            try {
                return LocalDateTime.parse(metadataValue, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    /**
     * Set value from Object (auto-converts to String)
     */
    public void setValue(Object value) {
        if (value == null) {
            this.metadataValue = null;
        } else if (value instanceof LocalDate) {
            this.metadataValue = ((LocalDate) value).toString();
            this.valueType = "DATE";
        } else if (value instanceof LocalDateTime) {
            this.metadataValue = ((LocalDateTime) value).toString();
            this.valueType = "DATE";
        } else if (value instanceof Number) {
            this.metadataValue = value.toString();
            this.valueType = "NUMBER";
        } else if (value instanceof Boolean) {
            this.metadataValue = value.toString();
            this.valueType = "BOOLEAN";
        } else {
            this.metadataValue = value.toString();
            // Keep existing type or default to STRING
            if (this.valueType == null) {
                this.valueType = "STRING";
            }
        }
    }

    /**
     * Validate the current value against validation rules
     */
    public boolean isValid() {
        // Check if value is required
        if (Boolean.TRUE.equals(isRequired) && (metadataValue == null || metadataValue.isEmpty())) {
            return false;
        }

        // If value is empty and not required, it's valid
        if (metadataValue == null || metadataValue.isEmpty()) {
            return true;
        }

        // Check against allowed values
        if (allowedValues != null && !allowedValues.isEmpty()) {
            if (!allowedValues.contains(metadataValue)) {
                return false;
            }
        }

        // Check against regex pattern
        if (validationRegex != null && !validationRegex.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(validationRegex);
                if (!pattern.matcher(metadataValue).matches()) {
                    return false;
                }
            } catch (Exception e) {
                // Invalid regex pattern
                return false;
            }
        }

        // Type-specific validation
        switch (valueType.toUpperCase()) {
            case "NUMBER":
                return getDoubleValue() != null;
            case "DATE":
                return getDateValue() != null;
            case "BOOLEAN":
                return metadataValue.equalsIgnoreCase("true") ||
                       metadataValue.equalsIgnoreCase("false");
            case "JSON":
                // Basic JSON validation (starts with { or [)
                return metadataValue.trim().startsWith("{") ||
                       metadataValue.trim().startsWith("[");
            default:
                return true;
        }
    }

    /**
     * Get validation error message
     */
    public String getValidationError() {
        if (isValid()) {
            return null;
        }

        if (Boolean.TRUE.equals(isRequired) && (metadataValue == null || metadataValue.isEmpty())) {
            return String.format("Metadata '%s' is required", metadataKey);
        }

        if (allowedValues != null && !allowedValues.isEmpty() && !allowedValues.contains(metadataValue)) {
            return String.format("Value '%s' is not in allowed values: %s", metadataValue, allowedValues);
        }

        if (validationRegex != null && !validationRegex.isEmpty()) {
            return String.format("Value '%s' does not match pattern '%s'", metadataValue, validationRegex);
        }

        return String.format("Invalid value for type '%s'", valueType);
    }

    /**
     * Check if value is in allowed values
     */
    public boolean isValueAllowed(String value) {
        if (allowedValues == null || allowedValues.isEmpty()) {
            return true;
        }
        return allowedValues.contains(value);
    }

    /**
     * Add an allowed value
     */
    public void addAllowedValue(String value) {
        if (allowedValues == null) {
            allowedValues = new ArrayList<>();
        }
        if (!allowedValues.contains(value)) {
            allowedValues.add(value);
        }
    }

    /**
     * Remove an allowed value
     */
    public void removeAllowedValue(String value) {
        if (allowedValues != null) {
            allowedValues.remove(value);
        }
    }

    /**
     * Set allowed values from array
     */
    public void setAllowedValues(String... values) {
        this.allowedValues = Arrays.asList(values);
    }

    /**
     * Check if metadata has validation rules
     */
    public boolean hasValidationRules() {
        return (validationRegex != null && !validationRegex.isEmpty()) ||
               (allowedValues != null && !allowedValues.isEmpty()) ||
               Boolean.TRUE.equals(isRequired);
    }

    /**
     * Get a display-friendly representation of the value
     */
    public String getDisplayValue() {
        if (metadataValue == null) {
            return "";
        }
        if (Boolean.TRUE.equals(isEncrypted)) {
            return "***ENCRYPTED***";
        }
        return metadataValue;
    }

    /**
     * Clone metadata for another document
     */
    public DocumentMetadata cloneForDocument(Document newDocument, String createdBy) {
        return DocumentMetadata.builder()
            .document(newDocument)
            .metadataKey(this.metadataKey)
            .metadataValue(this.metadataValue)
            .valueType(this.valueType)
            .isSearchable(this.isSearchable)
            .isEncrypted(this.isEncrypted)
            .isRequired(this.isRequired)
            .validationRegex(this.validationRegex)
            .allowedValues(this.allowedValues != null ? new ArrayList<>(this.allowedValues) : null)
            .createdBy(createdBy)
            .build();
    }

    /**
     * Compare metadata by key
     */
    public int compareByKey(DocumentMetadata other) {
        if (this.metadataKey == null && other.metadataKey == null) {
            return 0;
        }
        if (this.metadataKey == null) {
            return 1;
        }
        if (other.metadataKey == null) {
            return -1;
        }
        return this.metadataKey.compareTo(other.metadataKey);
    }
}
