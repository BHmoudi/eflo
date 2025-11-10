package com.eflo.document.service;

import com.eflo.document.domain.entity.DocumentPlaceholder;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentPlaceholderRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Document Initialization Service
 *
 * Handles automatic creation of document placeholders when workflow instances are created.
 * Consumes events from workflow-service to initialize required documents.
 *
 * @author Document Service
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentInitializationService {

    private final DocumentPlaceholderRepository placeholderRepository;
    private final DocumentTypeRepository documentTypeRepository;

    /**
     * Initialize document placeholders for a workflow instance
     * This is called when a workflow instance is created
     *
     * @param orderId Order ID
     * @param instanceId Workflow instance ID
     * @param requiredDocuments List of required document specifications
     * @param createdBy User who created the workflow
     */
    @Transactional
    public void initializeDocumentsForWorkflowInstance(
            Long orderId,
            Long instanceId,
            List<Map<String, Object>> requiredDocuments,
            String createdBy) {

        log.info("Initializing {} document placeholders for order {} (instance: {})",
                requiredDocuments.size(), orderId, instanceId);

        int created = 0;
        int skipped = 0;

        for (Map<String, Object> docReq : requiredDocuments) {
            try {
                String typeCode = (String) docReq.get("documentTypeCode");
                Boolean isMandatory = (Boolean) docReq.get("isMandatory");
                Integer deadlineHours = extractInteger(docReq.get("uploadDeadlineHours"));
                Boolean validationRequired = (Boolean) docReq.get("validationRequired");
                String description = (String) docReq.get("description");
                String taskCode = (String) docReq.get("taskCode");
                String taskName = (String) docReq.get("taskName");

                if (typeCode == null || typeCode.isBlank()) {
                    log.warn("Skipping document requirement with missing type code: {}", docReq);
                    skipped++;
                    continue;
                }

                // Check if placeholder already exists (idempotency)
                Optional<DocumentPlaceholder> existing = placeholderRepository
                        .findByOrderIdAndWorkflowInstanceIdAndDocumentTypeCode(orderId, instanceId, typeCode);

                if (existing.isPresent()) {
                    log.debug("Placeholder already exists for order={}, instance={}, typeCode={}. Skipping.",
                            orderId, instanceId, typeCode);
                    skipped++;
                    continue;
                }

                // Find document type
                DocumentType documentType = documentTypeRepository.findByTypeCode(typeCode)
                        .orElseThrow(() -> new RuntimeException("Document type not found: " + typeCode));

                // Calculate deadline
                LocalDateTime deadline = null;
                if (deadlineHours != null && deadlineHours > 0) {
                    deadline = LocalDateTime.now().plusHours(deadlineHours);
                }

                // Create placeholder
                DocumentPlaceholder placeholder = DocumentPlaceholder.builder()
                        .orderId(orderId)
                        .workflowInstanceId(instanceId)
                        .documentTypeId(documentType.getId())
                        .documentTypeCode(typeCode)
                        .documentTypeName(documentType.getTypeName())
                        .isMandatory(isMandatory != null ? isMandatory : true)
                        .status(DocumentStatus.PENDING_UPLOAD)
                        .deadline(deadline)
                        .validationRequired(validationRequired != null ? validationRequired : true)
                        .description(description)
                        .taskCode(taskCode)
                        .taskName(taskName)
                        .createdBy(createdBy != null ? createdBy : "SYSTEM")
                        .updatedBy(createdBy != null ? createdBy : "SYSTEM")
                        .build();

                placeholderRepository.save(placeholder);
                created++;

                log.debug("Created document placeholder: orderId={}, typeCode={}, mandatory={}, deadline={}",
                        orderId, typeCode, placeholder.getIsMandatory(), deadline);

            } catch (Exception e) {
                log.error("Failed to create document placeholder: {}", docReq, e);
                skipped++;
            }
        }

        log.info("Document placeholder initialization complete: created={}, skipped={}, total={}",
                created, skipped, requiredDocuments.size());
    }

    /**
     * Get document placeholders for an order
     */
    @Transactional(readOnly = true)
    public List<DocumentPlaceholder> getPlaceholdersForOrder(Long orderId) {
        return placeholderRepository.findByOrderId(orderId);
    }

    /**
     * Get mandatory pending placeholders for an order
     */
    @Transactional(readOnly = true)
    public List<DocumentPlaceholder> getMandatoryPendingForOrder(Long orderId) {
        return placeholderRepository.findMandatoryPendingByOrderId(orderId);
    }

    /**
     * Check if all mandatory documents are uploaded
     */
    @Transactional(readOnly = true)
    public boolean areAllMandatoryDocumentsUploaded(Long orderId) {
        return placeholderRepository.areAllMandatoryDocumentsUploaded(orderId);
    }

    /**
     * Link uploaded document to placeholder
     */
    @Transactional
    public void linkUploadedDocument(Long orderId, String documentTypeCode, Long documentId) {
        // Find the most recent pending placeholder for this type
        List<DocumentPlaceholder> placeholders = placeholderRepository.findByOrderId(orderId);

        placeholders.stream()
                .filter(p -> p.getDocumentTypeCode().equals(documentTypeCode))
                .filter(p -> p.getStatus() == DocumentStatus.PENDING_UPLOAD)
                .findFirst()
                .ifPresent(placeholder -> {
                    placeholder.markAsUploaded(documentId);
                    placeholderRepository.save(placeholder);
                    log.info("Linked document {} to placeholder {} for order {}",
                            documentId, placeholder.getId(), orderId);
                });
    }

    /**
     * Helper method to extract Integer from Object
     */
    private Integer extractInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
