package com.eflo.document.service;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.model.DocumentTypeCreateRequest;
import com.eflo.document.domain.model.DocumentTypeUpdateRequest;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.exception.DocumentStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing document types.
 * Handles document type CRUD operations, validation, and business logic.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentRepository documentRepository;

    /**
     * Retrieves all document types for a business unit.
     *
     * @param businessUnitId the business unit identifier
     * @return list of document types
     */
    @Transactional(readOnly = true)
    public List<DocumentType> getAllDocumentTypes(Long businessUnitId) {
        log.debug("Retrieving all document types for business unit: {}", businessUnitId);

        try {
            if (businessUnitId == null) {
                return documentTypeRepository.findAll();
            }
            return documentTypeRepository.findByBusinessUnitIdOrderByDisplayOrder(businessUnitId);
        } catch (Exception e) {
            log.error("Error retrieving document types for business unit: {}", businessUnitId, e);
            throw new DocumentStorageException("Failed to retrieve document types", e);
        }
    }

    /**
     * Retrieves a document type by ID.
     *
     * @param id the document type identifier
     * @return the document type
     * @throws DocumentStorageException if document type not found
     */
    @Transactional(readOnly = true)
    public DocumentType getDocumentTypeById(Long id) {
        log.debug("Retrieving document type by ID: {}", id);

        if (id == null) {
            throw new DocumentStorageException("Document type ID cannot be null");
        }

        return documentTypeRepository.findById(id)
                .orElseThrow(() -> new DocumentStorageException("Document type not found with ID: " + id));
    }

    /**
     * Retrieves a document type by code.
     *
     * @param code the document type code
     * @return the document type
     * @throws DocumentStorageException if document type not found
     */
    @Transactional(readOnly = true)
    public DocumentType getDocumentTypeByCode(String code) {
        log.debug("Retrieving document type by code: {}", code);

        if (code == null || code.trim().isEmpty()) {
            throw new DocumentStorageException("Document type code cannot be empty");
        }

        return documentTypeRepository.findByTypeCode(code)
                .orElseThrow(() -> new DocumentStorageException("Document type not found with code: " + code));
    }

    /**
     * Retrieves document types by category.
     *
     * @param category the document category
     * @return list of document types in the category
     */
    @Transactional(readOnly = true)
    public List<DocumentType> getDocumentTypesByCategory(DocumentCategory category) {
        log.debug("Retrieving document types by category: {}", category);

        if (category == null) {
            throw new DocumentStorageException("Document category cannot be null");
        }

        try {
            return documentTypeRepository.findByCategory(category);
        } catch (Exception e) {
            log.error("Error retrieving document types for category: {}", category, e);
            throw new DocumentStorageException("Failed to retrieve document types by category", e);
        }
    }

    /**
     * Retrieves mandatory document types for a business unit.
     *
     * @param businessUnitId the business unit identifier
     * @return list of mandatory document types
     */
    @Transactional(readOnly = true)
    public List<DocumentType> getMandatoryDocumentTypes(Long businessUnitId) {
        log.debug("Retrieving mandatory document types for business unit: {}", businessUnitId);

        try {
            if (businessUnitId == null) {
                return documentTypeRepository.findByIsMandatoryTrue();
            }
            return documentTypeRepository.findMandatoryByBusinessUnit(businessUnitId);
        } catch (Exception e) {
            log.error("Error retrieving mandatory document types for business unit: {}", businessUnitId, e);
            throw new DocumentStorageException("Failed to retrieve mandatory document types", e);
        }
    }

    /**
     * Creates a new document type.
     *
     * @param request the document type creation request
     * @param createdBy the user creating the document type
     * @return the created document type
     * @throws DocumentStorageException if validation fails or creation fails
     */
    @Transactional
    public DocumentType createDocumentType(DocumentTypeCreateRequest request, String createdBy) {
        log.info("Creating new document type: {} by user: {}", request.getTypeCode(), createdBy);

        // Validate request
        validateDocumentTypeCreateRequest(request);

        // Check if type code already exists
        if (documentTypeRepository.existsByTypeCode(request.getTypeCode())) {
            log.warn("Document type code already exists: {}", request.getTypeCode());
            throw new DocumentStorageException("Document type with code '" + request.getTypeCode() + "' already exists");
        }

        try {
            DocumentType documentType = DocumentType.builder()
                    .typeCode(request.getTypeCode())
                    .typeName(request.getTypeName())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .isMandatory(request.getIsMandatory())
                    .minDocuments(request.getMinDocuments())
                    .maxDocuments(request.getMaxDocuments())
                    .allowedFormats(request.getAllowedFormats())
                    .maxFileSizeMb(request.getMaxFileSizeMb())
                    .requiresValidation(request.getRequiresValidation())
                    .validatorRoles(request.getValidatorRoles())
                    .autoValidateConditions(request.getAutoValidateConditions() != null ? request.getAutoValidateConditions() : new HashMap<>())
                    .hasExpiration(request.getHasExpiration())
                    .expirationWarningDays(request.getExpirationWarningDays())
                    .defaultValidityDays(request.getDefaultValidityDays())
                    .customValidationRules(request.getCustomValidationRules() != null ? request.getCustomValidationRules() : new HashMap<>())
                    .metadataSchema(request.getMetadataSchema() != null ? request.getMetadataSchema() : new HashMap<>())
                    .isActive(request.getIsActive())
                    .displayOrder(request.getDisplayOrder())
                    .businessUnitId(request.getBusinessUnitId())
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now())
                    .build();

            DocumentType savedType = documentTypeRepository.save(documentType);
            log.info("Successfully created document type: {} with ID: {}", savedType.getTypeCode(), savedType.getId());

            return savedType;
        } catch (Exception e) {
            log.error("Error creating document type: {}", request.getTypeCode(), e);
            throw new DocumentStorageException("Failed to create document type", e);
        }
    }

    /**
     * Updates an existing document type.
     *
     * @param id the document type identifier
     * @param request the update request
     * @param updatedBy the user updating the document type
     * @return the updated document type
     * @throws DocumentStorageException if document type not found or update fails
     */
    @Transactional
    public DocumentType updateDocumentType(Long id, DocumentTypeUpdateRequest request, String updatedBy) {
        log.info("Updating document type ID: {} by user: {}", id, updatedBy);

        DocumentType documentType = getDocumentTypeById(id);

        // Validate update request
        validateDocumentTypeUpdateRequest(request);

        try {
            // Update fields if provided
            if (request.getTypeName() != null) {
                documentType.setTypeName(request.getTypeName());
            }
            if (request.getDescription() != null) {
                documentType.setDescription(request.getDescription());
            }
            if (request.getIsMandatory() != null) {
                documentType.setIsMandatory(request.getIsMandatory());
            }
            if (request.getMinDocuments() != null) {
                documentType.setMinDocuments(request.getMinDocuments());
            }
            if (request.getMaxDocuments() != null) {
                documentType.setMaxDocuments(request.getMaxDocuments());
            }
            if (request.getAllowedFormats() != null) {
                documentType.setAllowedFormats(request.getAllowedFormats());
            }
            if (request.getMaxFileSizeMb() != null) {
                documentType.setMaxFileSizeMb(request.getMaxFileSizeMb());
            }
            if (request.getRequiresValidation() != null) {
                documentType.setRequiresValidation(request.getRequiresValidation());
            }
            if (request.getValidatorRoles() != null) {
                documentType.setValidatorRoles(request.getValidatorRoles());
            }
            if (request.getAutoValidateConditions() != null) {
                documentType.setAutoValidateConditions(request.getAutoValidateConditions());
            }
            if (request.getHasExpiration() != null) {
                documentType.setHasExpiration(request.getHasExpiration());
            }
            if (request.getExpirationWarningDays() != null) {
                documentType.setExpirationWarningDays(request.getExpirationWarningDays());
            }
            if (request.getDefaultValidityDays() != null) {
                documentType.setDefaultValidityDays(request.getDefaultValidityDays());
            }
            if (request.getCustomValidationRules() != null) {
                documentType.setCustomValidationRules(request.getCustomValidationRules());
            }
            if (request.getMetadataSchema() != null) {
                documentType.setMetadataSchema(request.getMetadataSchema());
            }
            if (request.getIsActive() != null) {
                documentType.setIsActive(request.getIsActive());
            }
            if (request.getDisplayOrder() != null) {
                documentType.setDisplayOrder(request.getDisplayOrder());
            }

            documentType.setUpdatedBy(updatedBy);
            documentType.setUpdatedAt(LocalDateTime.now());

            DocumentType updatedType = documentTypeRepository.save(documentType);
            log.info("Successfully updated document type ID: {}", id);

            return updatedType;
        } catch (Exception e) {
            log.error("Error updating document type ID: {}", id, e);
            throw new DocumentStorageException("Failed to update document type", e);
        }
    }

    /**
     * Deletes a document type.
     * Prevents deletion if documents exist for this type.
     *
     * @param id the document type identifier
     * @throws DocumentStorageException if deletion fails or documents exist
     */
    @Transactional
    public void deleteDocumentType(Long id) {
        log.info("Attempting to delete document type ID: {}", id);

        DocumentType documentType = getDocumentTypeById(id);

        // Check if any documents use this type
        long documentCount = documentRepository.countByOrderIdAndDocumentTypeId(null, id);
        if (documentCount > 0) {
            log.warn("Cannot delete document type ID: {} - {} documents exist", id, documentCount);
            throw new DocumentStorageException("Cannot delete document type - " + documentCount + " documents exist using this type");
        }

        try {
            documentTypeRepository.delete(documentType);
            log.info("Successfully deleted document type ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting document type ID: {}", id, e);
            throw new DocumentStorageException("Failed to delete document type", e);
        }
    }

    /**
     * Duplicates a document type with a new code.
     *
     * @param id the source document type identifier
     * @param newCode the new type code
     * @param createdBy the user creating the duplicate
     * @return the duplicated document type
     * @throws DocumentStorageException if duplication fails
     */
    @Transactional
    public DocumentType duplicateDocumentType(Long id, String newCode, String createdBy) {
        log.info("Duplicating document type ID: {} with new code: {}", id, newCode);

        DocumentType source = getDocumentTypeById(id);

        // Check if new code already exists
        if (documentTypeRepository.existsByTypeCode(newCode)) {
            throw new DocumentStorageException("Document type with code '" + newCode + "' already exists");
        }

        try {
            DocumentType duplicate = DocumentType.builder()
                    .typeCode(newCode)
                    .typeName(source.getTypeName() + " (Copy)")
                    .description(source.getDescription())
                    .category(source.getCategory())
                    .isMandatory(source.getIsMandatory())
                    .minDocuments(source.getMinDocuments())
                    .maxDocuments(source.getMaxDocuments())
                    .allowedFormats(source.getAllowedFormats())
                    .maxFileSizeMb(source.getMaxFileSizeMb())
                    .requiresValidation(source.getRequiresValidation())
                    .validatorRoles(source.getValidatorRoles())
                    .autoValidateConditions(new HashMap<>(source.getAutoValidateConditions()))
                    .hasExpiration(source.getHasExpiration())
                    .expirationWarningDays(source.getExpirationWarningDays())
                    .defaultValidityDays(source.getDefaultValidityDays())
                    .customValidationRules(new HashMap<>(source.getCustomValidationRules()))
                    .metadataSchema(new HashMap<>(source.getMetadataSchema()))
                    .isActive(true)
                    .displayOrder(source.getDisplayOrder())
                    .businessUnitId(source.getBusinessUnitId())
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now())
                    .build();

            DocumentType savedDuplicate = documentTypeRepository.save(duplicate);
            log.info("Successfully duplicated document type ID: {} to new ID: {}", id, savedDuplicate.getId());

            return savedDuplicate;
        } catch (Exception e) {
            log.error("Error duplicating document type ID: {}", id, e);
            throw new DocumentStorageException("Failed to duplicate document type", e);
        }
    }

    /**
     * Activates a document type.
     *
     * @param id the document type identifier
     * @return the activated document type
     */
    @Transactional
    public DocumentType activateDocumentType(Long id) {
        log.info("Activating document type ID: {}", id);

        DocumentType documentType = getDocumentTypeById(id);
        documentType.setIsActive(true);
        documentType.setUpdatedAt(LocalDateTime.now());

        DocumentType saved = documentTypeRepository.save(documentType);
        log.info("Successfully activated document type ID: {}", id);

        return saved;
    }

    /**
     * Deactivates a document type.
     *
     * @param id the document type identifier
     * @return the deactivated document type
     */
    @Transactional
    public DocumentType deactivateDocumentType(Long id) {
        log.info("Deactivating document type ID: {}", id);

        DocumentType documentType = getDocumentTypeById(id);
        documentType.setIsActive(false);
        documentType.setUpdatedAt(LocalDateTime.now());

        DocumentType saved = documentTypeRepository.save(documentType);
        log.info("Successfully deactivated document type ID: {}", id);

        return saved;
    }

    /**
     * Retrieves statistics for a document type.
     *
     * @param id the document type identifier
     * @return map containing statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDocumentTypeStatistics(Long id) {
        log.debug("Retrieving statistics for document type ID: {}", id);

        DocumentType documentType = getDocumentTypeById(id);

        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("documentTypeId", id);
            stats.put("typeCode", documentType.getTypeCode());
            stats.put("typeName", documentType.getTypeName());
            stats.put("category", documentType.getCategory());
            stats.put("isActive", documentType.getIsActive());
            stats.put("isMandatory", documentType.getIsMandatory());

            // Count total documents for this type (query across all orders)
            // Note: This is a simplified version - actual implementation would need proper query
            stats.put("totalDocuments", 0L);
            stats.put("pendingDocuments", 0L);
            stats.put("validatedDocuments", 0L);
            stats.put("rejectedDocuments", 0L);

            stats.put("createdAt", documentType.getCreatedAt());
            stats.put("updatedAt", documentType.getUpdatedAt());
            stats.put("createdBy", documentType.getCreatedBy());

            return stats;
        } catch (Exception e) {
            log.error("Error retrieving statistics for document type ID: {}", id, e);
            throw new DocumentStorageException("Failed to retrieve document type statistics", e);
        }
    }

    // Private helper methods

    /**
     * Validates document type creation request.
     *
     * @param request the creation request
     * @throws DocumentStorageException if validation fails
     */
    private void validateDocumentTypeCreateRequest(DocumentTypeCreateRequest request) {
        if (request == null) {
            throw new DocumentStorageException("Document type creation request cannot be null");
        }

        if (request.getTypeCode() == null || request.getTypeCode().trim().isEmpty()) {
            throw new DocumentStorageException("Type code is required");
        }

        if (request.getTypeName() == null || request.getTypeName().trim().isEmpty()) {
            throw new DocumentStorageException("Type name is required");
        }

        if (request.getCategory() == null) {
            throw new DocumentStorageException("Category is required");
        }

        if (request.getMinDocuments() != null && request.getMaxDocuments() != null) {
            if (request.getMinDocuments() > request.getMaxDocuments()) {
                throw new DocumentStorageException("Minimum documents cannot exceed maximum documents");
            }
        }

        if (request.getAllowedFormats() == null || request.getAllowedFormats().isEmpty()) {
            throw new DocumentStorageException("At least one allowed format is required");
        }
    }

    /**
     * Validates document type update request.
     *
     * @param request the update request
     * @throws DocumentStorageException if validation fails
     */
    private void validateDocumentTypeUpdateRequest(DocumentTypeUpdateRequest request) {
        if (request == null) {
            throw new DocumentStorageException("Document type update request cannot be null");
        }

        if (request.getMinDocuments() != null && request.getMaxDocuments() != null) {
            if (request.getMinDocuments() > request.getMaxDocuments()) {
                throw new DocumentStorageException("Minimum documents cannot exceed maximum documents");
            }
        }
    }
}
