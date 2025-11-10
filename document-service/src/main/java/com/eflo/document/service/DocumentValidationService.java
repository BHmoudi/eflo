package com.eflo.document.service;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.entity.DocumentValidationRule;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.ValidationError;
import com.eflo.document.domain.model.ValidationResult;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentValidationRuleRepository;
import com.eflo.document.exception.DocumentStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for validating documents against configured rules and business requirements.
 * Handles file format, size, business rules, and order requirement validation.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentValidationService {

    private final DocumentRepository documentRepository;
    private final DocumentValidationRuleRepository validationRuleRepository;
    private final DocumentTypeService documentTypeService;

    /**
     * Validates a document against its document type configuration.
     *
     * @param document the document to validate
     * @param documentType the document type
     * @return validation result
     */
    @Transactional(readOnly = true)
    public ValidationResult validateDocument(Document document, DocumentType documentType) {
        log.debug("Validating document ID: {} against type: {}", document.getId(), documentType.getTypeCode());

        ValidationResult result = ValidationResult.builder()
                .isValid(true)
                .validationContext("DOCUMENT_VALIDATION")
                .validationTimestamp(System.currentTimeMillis())
                .build();

        try {
            // Validate file format
            if (!documentType.isFileFormatAllowed(document.getFileExtension())) {
                result.addError("fileExtension",
                        String.format("File format '%s' is not allowed. Allowed formats: %s",
                                document.getFileExtension(),
                                String.join(", ", documentType.getAllowedFormats())));
            }

            // Validate file size
            if (!documentType.isFileSizeAllowed(document.getFileSizeBytes())) {
                result.addError("fileSize",
                        String.format("File size %.2f MB exceeds maximum allowed size of %.2f MB",
                                document.getFileSizeMB(),
                                documentType.getMaxFileSizeMb().doubleValue()));
            }

            // Validate expiration date if required
            if (documentType.getHasExpiration() && document.getExpirationDate() == null) {
                result.addError("expirationDate", "Expiration date is required for this document type");
            }

            // Validate expiration date is in the future
            if (document.getExpirationDate() != null && document.getExpirationDate().isBefore(LocalDate.now())) {
                result.addError("expirationDate", "Expiration date must be in the future");
            }

            // Validate required fields based on metadata schema
            if (documentType.getMetadataSchema() != null && !documentType.getMetadataSchema().isEmpty()) {
                validateMetadataSchema(document, documentType, result);
            }

            // Execute configured validation rules
            List<DocumentValidationRule> rules = validationRuleRepository
                    .findByDocumentTypeIdAndIsActiveTrueOrderByExecutionOrder(documentType.getId());

            if (!rules.isEmpty()) {
                ValidationResult rulesResult = executeValidationRules(document, rules);
                mergeValidationResults(result, rulesResult);
            }

            log.debug("Document validation completed. Valid: {}, Errors: {}",
                    result.getIsValid(), result.getErrors().size());

            return result;
        } catch (Exception e) {
            log.error("Error validating document ID: {}", document.getId(), e);
            result.setIsValid(false);
            result.addError("validation", "Validation failed: " + e.getMessage());
            return result;
        }
    }

    /**
     * Validates file format against document type requirements.
     *
     * @param file the uploaded file
     * @param documentType the document type
     * @return validation result
     */
    public ValidationResult validateFileFormat(MultipartFile file, DocumentType documentType) {
        log.debug("Validating file format for: {}", file.getOriginalFilename());

        ValidationResult result = ValidationResult.valid();
        result.setValidationContext("FILE_FORMAT_VALIDATION");

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            result.addError("filename", "Filename cannot be empty");
            return result;
        }

        String extension = getFileExtension(filename);
        if (extension.isEmpty()) {
            result.addError("fileExtension", "File must have an extension");
            return result;
        }

        if (!documentType.isFileFormatAllowed(extension)) {
            result.addError("fileExtension",
                    String.format("File format '%s' is not allowed. Allowed formats: %s",
                            extension,
                            String.join(", ", documentType.getAllowedFormats())));
        }

        return result;
    }

    /**
     * Validates file size against document type requirements.
     *
     * @param file the uploaded file
     * @param documentType the document type
     * @return validation result
     */
    public ValidationResult validateFileSize(MultipartFile file, DocumentType documentType) {
        log.debug("Validating file size: {} bytes", file.getSize());

        ValidationResult result = ValidationResult.valid();
        result.setValidationContext("FILE_SIZE_VALIDATION");

        if (!documentType.isFileSizeAllowed(file.getSize())) {
            double fileSizeMB = file.getSize() / (1024.0 * 1024.0);
            result.addError("fileSize",
                    String.format("File size %.2f MB exceeds maximum allowed size of %.2f MB",
                            fileSizeMB,
                            documentType.getMaxFileSizeMb().doubleValue()));
        }

        return result;
    }

    /**
     * Validates business rules for a document with context.
     *
     * @param document the document to validate
     * @param context business context for validation
     * @return validation result
     */
    @Transactional(readOnly = true)
    public ValidationResult validateBusinessRules(Document document, Map<String, Object> context) {
        log.debug("Validating business rules for document ID: {}", document.getId());

        ValidationResult result = ValidationResult.builder()
                .isValid(true)
                .validationContext("BUSINESS_RULES_VALIDATION")
                .validationTimestamp(System.currentTimeMillis())
                .build();

        try {
            DocumentType documentType = document.getDocumentType();

            // Check custom validation rules from document type
            if (documentType.getCustomValidationRules() != null && !documentType.getCustomValidationRules().isEmpty()) {
                validateCustomRules(document, documentType.getCustomValidationRules(), context, result);
            }

            // Check auto-validate conditions
            if (documentType.getAutoValidateConditions() != null && !documentType.getAutoValidateConditions().isEmpty()) {
                evaluateAutoValidateConditions(document, documentType.getAutoValidateConditions(), context, result);
            }

            return result;
        } catch (Exception e) {
            log.error("Error validating business rules for document ID: {}", document.getId(), e);
            result.addError("businessRules", "Business rules validation failed: " + e.getMessage());
            return result;
        }
    }

    /**
     * Validates that all required documents for an order are present.
     *
     * @param orderId the order identifier
     * @return validation result
     */
    @Transactional(readOnly = true)
    public ValidationResult validateOrderRequirements(Long orderId) {
        log.debug("Validating order requirements for order ID: {}", orderId);

        ValidationResult result = ValidationResult.builder()
                .isValid(true)
                .validationContext("ORDER_REQUIREMENTS_VALIDATION")
                .validationTimestamp(System.currentTimeMillis())
                .build();

        try {
            // Get all documents for the order
            List<Document> orderDocuments = documentRepository.findByOrderId(orderId);

            // Get all mandatory document types (simplified - should filter by business unit)
            List<DocumentType> mandatoryTypes = documentTypeService.getMandatoryDocumentTypes(null);

            // Check each mandatory type
            for (DocumentType mandatoryType : mandatoryTypes) {
                List<Document> typeDocuments = orderDocuments.stream()
                        .filter(doc -> doc.getDocumentType().getId().equals(mandatoryType.getId()))
                        .filter(doc -> doc.getStatus() == DocumentStatus.VALIDATED)
                        .collect(Collectors.toList());

                // Check minimum documents requirement
                if (typeDocuments.size() < mandatoryType.getMinDocuments()) {
                    result.addBlockingIssue("mandatoryDocuments",
                            String.format("Missing required documents for '%s'. Required: %d, Found: %d",
                                    mandatoryType.getTypeName(),
                                    mandatoryType.getMinDocuments(),
                                    typeDocuments.size()));
                }

                // Check maximum documents requirement
                if (typeDocuments.size() > mandatoryType.getMaxDocuments()) {
                    result.addError("documentCount",
                            String.format("Too many documents for '%s'. Maximum: %d, Found: %d",
                                    mandatoryType.getTypeName(),
                                    mandatoryType.getMaxDocuments(),
                                    typeDocuments.size()));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error validating order requirements for order ID: {}", orderId, e);
            result.addError("orderRequirements", "Order requirements validation failed: " + e.getMessage());
            return result;
        }
    }

    /**
     * Validates document content (placeholder for content-based validation).
     *
     * @param document the document to validate
     * @return validation result
     */
    public ValidationResult validateDocumentContent(Document document) {
        log.debug("Validating document content for ID: {}", document.getId());

        ValidationResult result = ValidationResult.valid();
        result.setValidationContext("CONTENT_VALIDATION");

        // Placeholder for content validation logic
        // Could include OCR, text extraction, pattern matching, etc.

        return result;
    }

    /**
     * Validates required fields based on document type configuration.
     *
     * @param document the document to validate
     * @param documentType the document type
     * @return validation result
     */
    public ValidationResult validateRequiredFields(Document document, DocumentType documentType) {
        log.debug("Validating required fields for document ID: {}", document.getId());

        ValidationResult result = ValidationResult.valid();
        result.setValidationContext("REQUIRED_FIELDS_VALIDATION");

        // Check basic required fields
        if (document.getOriginalFilename() == null || document.getOriginalFilename().isEmpty()) {
            result.addError("originalFilename", "Original filename is required");
        }

        if (document.getOrderId() == null) {
            result.addError("orderId", "Order ID is required");
        }

        if (document.getUploadedBy() == null || document.getUploadedBy().isEmpty()) {
            result.addError("uploadedBy", "Uploaded by is required");
        }

        // Validate metadata schema requirements
        if (documentType.getMetadataSchema() != null && !documentType.getMetadataSchema().isEmpty()) {
            validateMetadataSchema(document, documentType, result);
        }

        return result;
    }

    /**
     * Validates a batch of documents.
     *
     * @param documents list of documents to validate
     * @return list of validation results
     */
    @Transactional(readOnly = true)
    public List<ValidationResult> validateBatch(List<Document> documents) {
        log.info("Validating batch of {} documents", documents.size());

        return documents.stream()
                .map(doc -> {
                    try {
                        return validateDocument(doc, doc.getDocumentType());
                    } catch (Exception e) {
                        log.error("Error validating document ID: {}", doc.getId(), e);
                        return ValidationResult.invalid("Validation error: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Validates all documents for an order and returns a summary.
     *
     * @param orderId the order identifier
     * @return validation summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validateOrderDocuments(Long orderId) {
        log.info("Validating all documents for order ID: {}", orderId);

        Map<String, Object> summary = new HashMap<>();
        List<Document> documents = documentRepository.findByOrderId(orderId);

        List<ValidationResult> results = validateBatch(documents);

        long validCount = results.stream().filter(r -> r.getIsValid()).count();
        long invalidCount = results.size() - validCount;
        long totalErrors = results.stream().mapToInt(r -> r.getErrors().size()).sum();
        long totalWarnings = results.stream().mapToInt(r -> r.getWarnings().size()).sum();

        summary.put("orderId", orderId);
        summary.put("totalDocuments", documents.size());
        summary.put("validDocuments", validCount);
        summary.put("invalidDocuments", invalidCount);
        summary.put("totalErrors", totalErrors);
        summary.put("totalWarnings", totalWarnings);
        summary.put("validationResults", results);

        // Add order requirements validation
        ValidationResult requirementsResult = validateOrderRequirements(orderId);
        summary.put("requirementsValidation", requirementsResult);
        summary.put("orderValid", requirementsResult.getIsValid());

        return summary;
    }

    /**
     * Executes validation rules for a document.
     *
     * @param document the document to validate
     * @param rules list of validation rules to execute
     * @return validation result
     */
    public ValidationResult executeValidationRules(Document document, List<DocumentValidationRule> rules) {
        log.debug("Executing {} validation rules for document ID: {}", rules.size(), document.getId());

        ValidationResult result = ValidationResult.builder()
                .isValid(true)
                .validationContext("RULES_EXECUTION")
                .validationTimestamp(System.currentTimeMillis())
                .build();

        for (DocumentValidationRule rule : rules) {
            try {
                // Execute rule based on rule type
                ValidationResult ruleResult = executeRule(document, rule);

                // Merge results
                if (!ruleResult.getIsValid()) {
                    result.setIsValid(false);
                }

                if (ruleResult.hasErrors()) {
                    ruleResult.getErrors().forEach(result::addError);
                }

                if (ruleResult.hasWarnings()) {
                    ruleResult.getWarnings().forEach(result::addWarning);
                }

                if (ruleResult.hasBlockingIssues()) {
                    ruleResult.getBlockingIssues().forEach(result::addBlockingIssue);
                }
            } catch (Exception e) {
                log.error("Error executing validation rule: {}", rule.getRuleName(), e);
                result.addError("ruleExecution", "Failed to execute rule: " + rule.getRuleName());
            }
        }

        return result;
    }

    // Private helper methods

    /**
     * Executes a single validation rule.
     */
    private ValidationResult executeRule(Document document, DocumentValidationRule rule) {
        ValidationResult result = ValidationResult.valid();

        // Placeholder for rule execution logic
        // Actual implementation would execute based on rule.getRuleType()
        // and rule.getRuleDefinition()

        log.debug("Executing rule: {} ({})", rule.getRuleName(), rule.getRuleType());

        return result;
    }

    /**
     * Validates metadata against schema.
     */
    private void validateMetadataSchema(Document document, DocumentType documentType, ValidationResult result) {
        Map<String, Object> schema = documentType.getMetadataSchema();
        Map<String, Object> metadata = document.getCustomMetadata();

        if (metadata == null) {
            metadata = new HashMap<>();
        }

        for (Map.Entry<String, Object> schemaEntry : schema.entrySet()) {
            String fieldName = schemaEntry.getKey();
            Object fieldConfig = schemaEntry.getValue();

            if (fieldConfig instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) fieldConfig;
                Boolean required = (Boolean) config.get("required");

                if (Boolean.TRUE.equals(required) && !metadata.containsKey(fieldName)) {
                    result.addError("metadata." + fieldName,
                            "Required metadata field '" + fieldName + "' is missing");
                }
            }
        }
    }

    /**
     * Validates custom rules.
     */
    private void validateCustomRules(Document document, Map<String, Object> customRules,
                                     Map<String, Object> context, ValidationResult result) {
        // Placeholder for custom rules validation
        log.debug("Validating custom rules for document ID: {}", document.getId());
    }

    /**
     * Evaluates auto-validate conditions.
     */
    private void evaluateAutoValidateConditions(Document document, Map<String, Object> conditions,
                                                Map<String, Object> context, ValidationResult result) {
        // Placeholder for auto-validate conditions evaluation
        log.debug("Evaluating auto-validate conditions for document ID: {}", document.getId());
    }

    /**
     * Merges two validation results.
     */
    private void mergeValidationResults(ValidationResult target, ValidationResult source) {
        if (!source.getIsValid()) {
            target.setIsValid(false);
        }

        if (source.hasErrors()) {
            source.getErrors().forEach(target::addError);
        }

        if (source.hasWarnings()) {
            source.getWarnings().forEach(target::addWarning);
        }

        if (source.hasBlockingIssues()) {
            source.getBlockingIssues().forEach(target::addBlockingIssue);
        }
    }

    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
