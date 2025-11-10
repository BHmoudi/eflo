package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a single validation error or warning.
 * Contains field name, error message, and error code.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /**
     * The field or property that has the validation error.
     * Can be a JSON path for nested fields (e.g., "metadata.author").
     */
    private String field;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Machine-readable error code for programmatic handling.
     * Examples: REQUIRED_FIELD, INVALID_FORMAT, SIZE_EXCEEDED, etc.
     */
    private String errorCode;

    /**
     * The rejected value that caused the error (optional).
     */
    private Object rejectedValue;

    /**
     * Additional context or details about the error (optional).
     */
    private String additionalInfo;

    /**
     * Severity level of the error.
     * Values: ERROR, WARNING, INFO
     */
    @Builder.Default
    private ErrorSeverity severity = ErrorSeverity.ERROR;

    /**
     * Suggested fix or action to resolve the error (optional).
     */
    private String suggestedFix;

    /**
     * Error severity levels.
     */
    public enum ErrorSeverity {
        /**
         * Critical error that prevents the operation.
         */
        ERROR,

        /**
         * Warning that should be addressed but doesn't prevent the operation.
         */
        WARNING,

        /**
         * Informational message.
         */
        INFO
    }

    /**
     * Check if this is an error (as opposed to warning or info).
     *
     * @return true if severity is ERROR
     */
    public boolean isError() {
        return severity == ErrorSeverity.ERROR;
    }

    /**
     * Check if this is a warning.
     *
     * @return true if severity is WARNING
     */
    public boolean isWarning() {
        return severity == ErrorSeverity.WARNING;
    }

    /**
     * Check if this is informational.
     *
     * @return true if severity is INFO
     */
    public boolean isInfo() {
        return severity == ErrorSeverity.INFO;
    }

    /**
     * Get a formatted error message including field name.
     *
     * @return formatted error message
     */
    public String getFormattedMessage() {
        if (field != null && !field.isEmpty()) {
            return String.format("[%s] %s", field, message);
        }
        return message;
    }

    /**
     * Create a required field error.
     *
     * @param field the required field name
     * @return ValidationError
     */
    public static ValidationError requiredField(String field) {
        return ValidationError.builder()
                .field(field)
                .message(String.format("%s is required", field))
                .errorCode("REQUIRED_FIELD")
                .severity(ErrorSeverity.ERROR)
                .build();
    }

    /**
     * Create an invalid format error.
     *
     * @param field the field with invalid format
     * @param expectedFormat the expected format
     * @return ValidationError
     */
    public static ValidationError invalidFormat(String field, String expectedFormat) {
        return ValidationError.builder()
                .field(field)
                .message(String.format("%s has invalid format. Expected: %s", field, expectedFormat))
                .errorCode("INVALID_FORMAT")
                .severity(ErrorSeverity.ERROR)
                .build();
    }

    /**
     * Create a size exceeded error.
     *
     * @param field the field that exceeded size
     * @param maxSize the maximum allowed size
     * @return ValidationError
     */
    public static ValidationError sizeExceeded(String field, String maxSize) {
        return ValidationError.builder()
                .field(field)
                .message(String.format("%s exceeds maximum size of %s", field, maxSize))
                .errorCode("SIZE_EXCEEDED")
                .severity(ErrorSeverity.ERROR)
                .build();
    }

    /**
     * Create an invalid value error.
     *
     * @param field the field with invalid value
     * @param value the invalid value
     * @param reason the reason it's invalid
     * @return ValidationError
     */
    public static ValidationError invalidValue(String field, Object value, String reason) {
        return ValidationError.builder()
                .field(field)
                .message(String.format("%s has invalid value: %s", field, reason))
                .errorCode("INVALID_VALUE")
                .rejectedValue(value)
                .severity(ErrorSeverity.ERROR)
                .build();
    }

    /**
     * Create a warning.
     *
     * @param field the field with the warning
     * @param message the warning message
     * @return ValidationError with WARNING severity
     */
    public static ValidationError warning(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .errorCode("WARNING")
                .severity(ErrorSeverity.WARNING)
                .build();
    }

    /**
     * Create an info message.
     *
     * @param field the field with the info
     * @param message the info message
     * @return ValidationError with INFO severity
     */
    public static ValidationError info(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .errorCode("INFO")
                .severity(ErrorSeverity.INFO)
                .build();
    }
}
