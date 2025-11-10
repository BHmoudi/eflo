package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a validation result.
 * Contains validation status, errors, warnings, and blocking issues.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * Flag indicating if the validation passed.
     */
    @Builder.Default
    private Boolean isValid = true;

    /**
     * List of validation errors.
     * Errors indicate critical issues that prevent the operation from succeeding.
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    /**
     * List of validation warnings.
     * Warnings indicate potential issues but don't prevent the operation.
     */
    @Builder.Default
    private List<ValidationError> warnings = new ArrayList<>();

    /**
     * List of blocking issues that must be resolved.
     * Blocking issues are critical errors that prevent further processing.
     */
    @Builder.Default
    private List<ValidationError> blockingIssues = new ArrayList<>();

    /**
     * General validation message.
     */
    private String message;

    /**
     * Validation context or category.
     */
    private String validationContext;

    /**
     * Timestamp of the validation.
     */
    private Long validationTimestamp;

    /**
     * Check if there are any errors.
     *
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if there are any warnings.
     *
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Check if there are any blocking issues.
     *
     * @return true if blocking issues exist
     */
    public boolean hasBlockingIssues() {
        return blockingIssues != null && !blockingIssues.isEmpty();
    }

    /**
     * Check if validation passed without any issues.
     *
     * @return true if valid with no errors, warnings, or blocking issues
     */
    public boolean isClean() {
        return Boolean.TRUE.equals(isValid) &&
               !hasErrors() &&
               !hasWarnings() &&
               !hasBlockingIssues();
    }

    /**
     * Add an error to the validation result.
     *
     * @param error the validation error to add
     */
    public void addError(ValidationError error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
        this.isValid = false;
    }

    /**
     * Add an error with field and message.
     *
     * @param field the field with the error
     * @param message the error message
     */
    public void addError(String field, String message) {
        addError(ValidationError.builder()
                .field(field)
                .message(message)
                .errorCode("VALIDATION_ERROR")
                .build());
    }

    /**
     * Add a warning to the validation result.
     *
     * @param warning the validation warning to add
     */
    public void addWarning(ValidationError warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * Add a warning with field and message.
     *
     * @param field the field with the warning
     * @param message the warning message
     */
    public void addWarning(String field, String message) {
        addWarning(ValidationError.builder()
                .field(field)
                .message(message)
                .errorCode("VALIDATION_WARNING")
                .build());
    }

    /**
     * Add a blocking issue to the validation result.
     *
     * @param issue the blocking issue to add
     */
    public void addBlockingIssue(ValidationError issue) {
        if (blockingIssues == null) {
            blockingIssues = new ArrayList<>();
        }
        blockingIssues.add(issue);
        this.isValid = false;
    }

    /**
     * Add a blocking issue with field and message.
     *
     * @param field the field with the blocking issue
     * @param message the blocking issue message
     */
    public void addBlockingIssue(String field, String message) {
        addBlockingIssue(ValidationError.builder()
                .field(field)
                .message(message)
                .errorCode("BLOCKING_ISSUE")
                .build());
    }

    /**
     * Get total count of all issues (errors + warnings + blocking issues).
     *
     * @return total issue count
     */
    public int getTotalIssueCount() {
        int count = 0;
        if (errors != null) count += errors.size();
        if (warnings != null) count += warnings.size();
        if (blockingIssues != null) count += blockingIssues.size();
        return count;
    }

    /**
     * Get all error messages as a single string.
     *
     * @return concatenated error messages
     */
    public String getErrorMessages() {
        if (!hasErrors()) {
            return "";
        }
        return errors.stream()
                .map(ValidationError::getMessage)
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

    /**
     * Get all warning messages as a single string.
     *
     * @return concatenated warning messages
     */
    public String getWarningMessages() {
        if (!hasWarnings()) {
            return "";
        }
        return warnings.stream()
                .map(ValidationError::getMessage)
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

    /**
     * Create a valid result with no issues.
     *
     * @return ValidationResult indicating success
     */
    public static ValidationResult valid() {
        return ValidationResult.builder()
                .isValid(true)
                .message("Validation passed")
                .build();
    }

    /**
     * Create an invalid result with an error message.
     *
     * @param errorMessage the error message
     * @return ValidationResult indicating failure
     */
    public static ValidationResult invalid(String errorMessage) {
        ValidationResult result = ValidationResult.builder()
                .isValid(false)
                .message(errorMessage)
                .build();
        result.addError("general", errorMessage);
        return result;
    }

    /**
     * Create an invalid result with a field-specific error.
     *
     * @param field the field with the error
     * @param errorMessage the error message
     * @return ValidationResult indicating failure
     */
    public static ValidationResult invalidField(String field, String errorMessage) {
        ValidationResult result = ValidationResult.builder()
                .isValid(false)
                .build();
        result.addError(field, errorMessage);
        return result;
    }
}
