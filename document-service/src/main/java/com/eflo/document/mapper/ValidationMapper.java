package com.eflo.document.mapper;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentValidationResponse;
import com.eflo.document.domain.model.ValidationError;
import com.eflo.document.domain.model.ValidationResult;
import org.mapstruct.*;

/**
 * MapStruct mapper for validation-related conversions.
 * Handles mapping between validation results and response DTOs.
 *
 * <p>This mapper provides methods to convert ValidationResult to DocumentValidationResponse
 * and to create validation errors from field and message combinations.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ValidationMapper {

    /**
     * Maps a ValidationResult to a DocumentValidationResponse.
     *
     * @param validationResult the validation result
     * @return the document validation response
     */
    @Mapping(target = "documentId", ignore = true)
    @Mapping(target = "documentUuid", ignore = true)
    @Mapping(target = "validationSuccessful", source = "isValid")
    @Mapping(target = "isApproved", source = "isValid")
    @Mapping(target = "status", expression = "java(determineStatus(validationResult))")
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "validatedBy", ignore = true)
    @Mapping(target = "validationComments", source = "message")
    @Mapping(target = "statusReason", expression = "java(getStatusReason(validationResult))")
    @Mapping(target = "validationResult", source = ".")
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "message", source = "message")
    DocumentValidationResponse toResponse(ValidationResult validationResult);

    /**
     * Maps a ValidationResult and Document to a DocumentValidationResponse.
     * Includes document-specific information.
     *
     * @param validationResult the validation result
     * @param document the validated document
     * @return the document validation response
     */
    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "documentUuid", source = "document.documentUuid")
    @Mapping(target = "validationSuccessful", source = "validationResult.isValid")
    @Mapping(target = "isApproved", source = "validationResult.isValid")
    @Mapping(target = "status", source = "document.status")
    @Mapping(target = "validatedAt", source = "document.validatedAt")
    @Mapping(target = "validatedBy", source = "document.validatedBy")
    @Mapping(target = "validationComments", source = "document.validationComments")
    @Mapping(target = "statusReason", source = "document.statusReason")
    @Mapping(target = "validationResult", source = "validationResult")
    @Mapping(target = "originalFilename", source = "document.originalFilename")
    @Mapping(target = "typeCode", source = "document.typeCode")
    @Mapping(target = "orderNumber", source = "document.orderNumber")
    @Mapping(target = "message", expression = "java(buildValidationMessage(validationResult, document))")
    DocumentValidationResponse toResponse(ValidationResult validationResult, Document document);

    /**
     * Creates a ValidationError from field name and error message.
     *
     * @param field the field with the error
     * @param message the error message
     * @return the validation error
     */
    default ValidationError toValidationError(String field, String message) {
        return ValidationError.builder()
            .field(field)
            .message(message)
            .errorCode("VALIDATION_ERROR")
            .severity(ValidationError.ErrorSeverity.ERROR)
            .build();
    }

    /**
     * Creates a ValidationError with custom error code.
     *
     * @param field the field with the error
     * @param message the error message
     * @param errorCode the error code
     * @return the validation error
     */
    default ValidationError toValidationError(String field, String message, String errorCode) {
        return ValidationError.builder()
            .field(field)
            .message(message)
            .errorCode(errorCode)
            .severity(ValidationError.ErrorSeverity.ERROR)
            .build();
    }

    /**
     * Creates a ValidationError with custom severity.
     *
     * @param field the field with the error
     * @param message the error message
     * @param errorCode the error code
     * @param severity the error severity
     * @return the validation error
     */
    default ValidationError toValidationError(
        String field,
        String message,
        String errorCode,
        ValidationError.ErrorSeverity severity
    ) {
        return ValidationError.builder()
            .field(field)
            .message(message)
            .errorCode(errorCode)
            .severity(severity)
            .build();
    }

    /**
     * Determines the document status based on validation result.
     *
     * @param validationResult the validation result
     * @return the document status
     */
    default DocumentStatus determineStatus(ValidationResult validationResult) {
        if (validationResult == null) {
            return DocumentStatus.PENDING;
        }

        if (Boolean.TRUE.equals(validationResult.getIsValid())) {
            return DocumentStatus.VALIDATED;
        } else {
            return DocumentStatus.REJECTED;
        }
    }

    /**
     * Gets the status reason from validation result.
     *
     * @param validationResult the validation result
     * @return the status reason
     */
    default String getStatusReason(ValidationResult validationResult) {
        if (validationResult == null || Boolean.TRUE.equals(validationResult.getIsValid())) {
            return null;
        }

        // Get all error messages concatenated
        return validationResult.getErrorMessages();
    }

    /**
     * Builds a comprehensive validation message.
     *
     * @param validationResult the validation result
     * @param document the document
     * @return the validation message
     */
    default String buildValidationMessage(ValidationResult validationResult, Document document) {
        if (validationResult == null) {
            return "Validation pending";
        }

        if (Boolean.TRUE.equals(validationResult.getIsValid())) {
            return String.format("Document '%s' has been validated successfully",
                document != null ? document.getOriginalFilename() : "unknown");
        } else {
            int errorCount = validationResult.hasErrors() ?
                validationResult.getErrors().size() : 0;
            return String.format("Document validation failed with %d error(s)", errorCount);
        }
    }

    /**
     * Creates a successful validation response for a document.
     *
     * @param document the validated document
     * @param validatedBy the user who validated
     * @param comments validation comments
     * @return the validation response
     */
    default DocumentValidationResponse createSuccessResponse(
        Document document,
        String validatedBy,
        String comments
    ) {
        return DocumentValidationResponse.builder()
            .documentId(document.getId())
            .documentUuid(document.getDocumentUuid())
            .validationSuccessful(true)
            .isApproved(true)
            .status(DocumentStatus.VALIDATED)
            .validatedAt(java.time.LocalDateTime.now())
            .validatedBy(validatedBy)
            .validationComments(comments)
            .validationResult(ValidationResult.valid())
            .originalFilename(document.getOriginalFilename())
            .typeCode(document.getTypeCode())
            .orderNumber(document.getOrderNumber())
            .message(String.format("Document '%s' has been validated successfully",
                document.getOriginalFilename()))
            .build();
    }

    /**
     * Creates a rejection validation response for a document.
     *
     * @param document the rejected document
     * @param validatedBy the user who validated
     * @param reason rejection reason
     * @return the validation response
     */
    default DocumentValidationResponse createRejectionResponse(
        Document document,
        String validatedBy,
        String reason
    ) {
        ValidationResult result = ValidationResult.invalid(reason);

        return DocumentValidationResponse.builder()
            .documentId(document.getId())
            .documentUuid(document.getDocumentUuid())
            .validationSuccessful(false)
            .isApproved(false)
            .status(DocumentStatus.REJECTED)
            .validatedAt(java.time.LocalDateTime.now())
            .validatedBy(validatedBy)
            .validationComments(reason)
            .statusReason(reason)
            .validationResult(result)
            .originalFilename(document.getOriginalFilename())
            .typeCode(document.getTypeCode())
            .orderNumber(document.getOrderNumber())
            .message(String.format("Document '%s' has been rejected: %s",
                document.getOriginalFilename(), reason))
            .build();
    }
}
