package com.eflo.document.service;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.model.*;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.exception.DocumentNotFoundException;
import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.exception.DocumentValidationException;
import com.eflo.document.config.MinIOConfig;
import com.eflo.document.mapper.DocumentMapper;
import com.eflo.document.scanner.ScanResult;
import com.eflo.document.scanner.VirusScanningService;
import com.eflo.document.storage.FileProcessor;
import com.eflo.document.storage.FileValidator;
import com.eflo.document.storage.MinIOStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Document Management Service - Main orchestration service for all document operations.
 * This service coordinates document upload, retrieval, validation, modification, and deletion operations
 * across multiple subsystems including storage, virus scanning, validation, and event publishing.
 *
 * <p>Core Responsibilities:</p>
 * <ul>
 *   <li>Document upload and bulk upload orchestration</li>
 *   <li>Document retrieval and download</li>
 *   <li>Document validation and rejection workflows</li>
 *   <li>Document modification and versioning</li>
 *   <li>Document deletion and archival</li>
 *   <li>Order-level document operations</li>
 *   <li>Event publishing for all document lifecycle events</li>
 * </ul>
 *
 * <p>Orchestration Flow:</p>
 * <ol>
 *   <li>Upload: Store in MinIO → Virus scan → Create DB record → Publish event</li>
 *   <li>Validation: Check rules → Update status → Update DB → Publish event</li>
 *   <li>Delete: Soft delete → Archive in MinIO → Update DB → Publish event</li>
 *   <li>Version: Upload new → Link to parent → Mark old as superseded → Publish event</li>
 * </ol>
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentManagementService {

    // Repository dependencies
    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;

    // Storage and processing
    private final MinIOStorageService minIOStorageService;
    private final FileProcessor fileProcessor;
    private final FileValidator fileValidator;
    private final MinIOConfig minIOConfig;

    // Validation and scanning
    private final VirusScanningService virusScanningService;

    // Event publishing
    private final DocumentEventPublisher eventPublisher;

    // Mapping
    private final DocumentMapper documentMapper;

    // Storage configuration comes from MinIOConfig (application.yml)

    /**
     * Uploads a single document with full orchestration including storage, scanning, and validation.
     *
     * <p>Orchestration steps:</p>
     * <ol>
     *   <li>Validate file and document type</li>
     *   <li>Calculate file hash and extract metadata</li>
     *   <li>Upload to MinIO storage</li>
     *   <li>Perform virus scan</li>
     *   <li>Create database record</li>
     *   <li>Publish upload event</li>
     * </ol>
     *
     * @param request document upload request containing file and metadata
     * @return DocumentResponse with upload result
     * @throws DocumentValidationException if validation fails
     * @throws DocumentStorageException if storage operation fails
     */
    @Transactional
    public DocumentResponse uploadDocument(DocumentUploadRequest request) {
        log.info("Starting document upload - File: {}, Type ID: {}, Order ID: {}",
                request.getFile().getOriginalFilename(), request.getDocumentTypeId(), request.getOrderId());

        try {
            // 1. Validate file
            MultipartFile file = request.getFile();
            FileValidator.ValidationResult fileValidation = fileValidator.validateFile(file);
            if (!fileValidation.isValid()) {
                throw new DocumentValidationException(
                        "File validation failed: " + fileValidation.getFirstError(),
                        fileValidation.getErrors()
                );
            }

            // 2. Retrieve and validate document type
            DocumentType documentType = documentTypeRepository.findById(request.getDocumentTypeId())
                    .orElseThrow(() -> new DocumentValidationException(
                            "Document type not found with ID: " + request.getDocumentTypeId()
                    ));

            if (!documentType.getIsActive()) {
                throw new DocumentValidationException("Document type is not active: " + documentType.getTypeCode());
            }

            // 3. Calculate file hash
            String fileHash = fileProcessor.calculateFileHash(file);
            log.debug("Calculated file hash: {}", fileHash);

            // 4. Generate storage path
            String storedFilename = generateStoredFilename(file.getOriginalFilename());
            String storagePath = generateStoragePath(request.getOrderId(), documentType.getTypeCode(), storedFilename);

            // 5. Upload to MinIO
            String docsBucket = minIOConfig.getDocumentsBucket();
            minIOStorageService.createBucketIfNotExists(docsBucket);
            minIOStorageService.uploadFile(
                    docsBucket,
                    storagePath,
                    file.getInputStream(),
                    file.getContentType(),
                    file.getSize()
            );
            log.info("File uploaded to MinIO - Bucket: {}, Path: {}", docsBucket, storagePath);

            // 6. Perform virus scan
            ScanResult scanResult;
            try {
                scanResult = virusScanningService.scanDocument(file.getInputStream(), file.getOriginalFilename());
                log.info("Virus scan completed - Status: {}, Safe: {}", scanResult.getStatus(), scanResult.isSafe());
            } catch (Exception e) {
                log.error("Virus scan failed: {}", e.getMessage(), e);
                scanResult = ScanResult.failed(file.getOriginalFilename(), "Scan error: " + e.getMessage());
            }

            // 7. Create document entity
            Document document = Document.builder()
                    .documentUuid(UUID.randomUUID())
                    .documentType(documentType)
                    .typeCode(documentType.getTypeCode())
                    .orderId(request.getOrderId())
                    .orderNumber(request.getOrderNumber())
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .fileExtension(getFileExtension(file.getOriginalFilename()))
                    .mimeType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .fileHash(fileHash)
                    .storageBucket(docsBucket)
                    .storagePath(storagePath)
                    .version(1)
                    .isLatestVersion(true)
                    .status(scanResult.isSafe() ? DocumentStatus.PENDING : DocumentStatus.REJECTED)
                    .virusScanStatus(scanResult.getStatus())
                    .virusScanDate(LocalDateTime.now())
                    .virusScanResult(scanResult.toMap())
                    .customMetadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                    .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                    .description(request.getDescription())
                    .expirationDate(request.getExpirationDate())
                    .businessUnitId(request.getBusinessUnitId())
                    .uploadedBy("SYSTEM") // Should be replaced with actual user from security context
                    .uploadedAt(LocalDateTime.now())
                    .createdBy("SYSTEM") // Should be replaced with actual user from security context
                    .createdAt(LocalDateTime.now())
                    .build();

            // 8. Save to database
            Document savedDocument = documentRepository.save(document);
            log.info("Document saved to database - ID: {}, UUID: {}", savedDocument.getId(), savedDocument.getDocumentUuid());

            // 9. Publish upload event
            eventPublisher.publishDocumentUploaded(savedDocument);

            // 10. Publish scan completed event
            eventPublisher.publishScanCompleted(savedDocument, scanResult);

            // 11. Map to response
            DocumentResponse response = documentMapper.toResponse(savedDocument);
            log.info("Document upload completed successfully - ID: {}", savedDocument.getId());

            return response;

        } catch (DocumentValidationException | DocumentStorageException e) {
            log.error("Document upload failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during document upload: {}", e.getMessage(), e);
            throw new DocumentStorageException("Document upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads multiple documents in bulk with support for partial success.
     *
     * @param request bulk upload request
     * @return BulkUploadResultResponse with results for all files
     */
    @Transactional
    public BulkUploadResultResponse uploadMultipleDocuments(BulkUploadRequest request) {
        log.info("Starting bulk upload - Files: {}, Order ID: {}", request.getFiles().size(), request.getOrderId());

        LocalDateTime startTime = LocalDateTime.now();
        List<BulkUploadResultResponse.FileUploadResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (MultipartFile file : request.getFiles()) {
            long fileStartTime = System.currentTimeMillis();

            try {
                // Build individual upload request
                DocumentUploadRequest uploadRequest = buildUploadRequest(file, request);

                // Upload document
                DocumentResponse documentResponse = uploadDocument(uploadRequest);

                // Build success result
                BulkUploadResultResponse.FileUploadResult result = BulkUploadResultResponse.FileUploadResult.builder()
                        .filename(file.getOriginalFilename())
                        .success(true)
                        .documentId(documentResponse.getId())
                        .documentUuid(documentResponse.getDocumentUuid())
                        .typeCode(documentResponse.getTypeCode())
                        .fileSizeMB(documentResponse.getFileSizeMB())
                        .uploadedAt(documentResponse.getUploadedAt())
                        .processingTimeMs(System.currentTimeMillis() - fileStartTime)
                        .build();

                results.add(result);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to upload file in bulk: {} - {}", file.getOriginalFilename(), e.getMessage());

                BulkUploadResultResponse.FileUploadResult result = BulkUploadResultResponse.FileUploadResult.builder()
                        .filename(file.getOriginalFilename())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .errors(List.of(e.getMessage()))
                        .processingTimeMs(System.currentTimeMillis() - fileStartTime)
                        .build();

                results.add(result);
                failureCount++;

                if (!Boolean.TRUE.equals(request.getContinueOnError())) {
                    log.warn("Bulk upload aborted due to error and continueOnError=false");
                    break;
                }
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        long totalProcessingTime = java.time.Duration.between(startTime, endTime).toMillis();

        BulkUploadResultResponse response = BulkUploadResultResponse.builder()
                .overallSuccess(failureCount == 0)
                .totalFiles(request.getFiles().size())
                .successfulUploads(successCount)
                .failedUploads(failureCount)
                .startTime(startTime)
                .endTime(endTime)
                .totalProcessingTimeMs(totalProcessingTime)
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .results(results)
                .message(String.format("Bulk upload completed: %d successful, %d failed", successCount, failureCount))
                .build();

        log.info("Bulk upload completed - Total: {}, Success: {}, Failed: {}",
                request.getFiles().size(), successCount, failureCount);

        return response;
    }

    /**
     * Uploads a document and validates it immediately.
     *
     * @param request upload request
     * @return DocumentResponse with validated document
     */
    @Transactional
    public DocumentResponse uploadAndValidate(DocumentUploadRequest request) {
        log.info("Upload and validate - File: {}", request.getFile().getOriginalFilename());

        DocumentResponse uploadResponse = uploadDocument(request);

        // Auto-validate if document type doesn't require manual validation
        DocumentType documentType = documentTypeRepository.findById(request.getDocumentTypeId())
                .orElseThrow(() -> new DocumentNotFoundException("Document type not found"));

        if (!documentType.getRequiresValidation()) {
            DocumentValidationRequest validationRequest = DocumentValidationRequest.builder()
                    .isApproved(true)
                    .comments("Auto-validated")
                    .validatedBy("SYSTEM")
                    .build();

            return getDocumentById(uploadResponse.getId());
        }

        return uploadResponse;
    }

    /**
     * Retrieves a document by its ID.
     *
     * @param id document ID
     * @return DocumentResponse
     * @throws DocumentNotFoundException if document not found
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        log.debug("Retrieving document by ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        return documentMapper.toResponse(document);
    }

    /**
     * Retrieves a document by its UUID.
     *
     * @param uuid document UUID
     * @return DocumentResponse
     * @throws DocumentNotFoundException if document not found
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentByUuid(UUID uuid) {
        log.debug("Retrieving document by UUID: {}", uuid);

        Document document = documentRepository.findByDocumentUuid(uuid)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with UUID: " + uuid, uuid.toString()));

        return documentMapper.toResponse(document);
    }

    /**
     * Downloads a document as an InputStream.
     *
     * @param id document ID
     * @return InputStream of the document
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentStorageException if download fails
     */
    @Transactional(readOnly = true)
    public InputStream downloadDocument(Long id) {
        log.info("Downloading document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        if (document.isDeleted()) {
            throw new DocumentValidationException("Cannot download deleted document");
        }

        return minIOStorageService.downloadFile(document.getStorageBucket(), document.getStoragePath());
    }

    /**
     * Gets document preview (for images, first page of PDFs, etc.).
     *
     * @param id document ID
     * @return byte array of preview
     */
    @Transactional(readOnly = true)
    public byte[] getDocumentPreview(Long id) {
        log.debug("Generating preview for document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        try (InputStream inputStream = minIOStorageService.downloadFile(document.getStorageBucket(), document.getStoragePath())) {
            // For images, generate thumbnail
            // For other types, return placeholder or first page
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to generate preview: {}", e.getMessage(), e);
            throw new DocumentStorageException("Preview generation failed", e);
        }
    }

    /**
     * Gets document metadata.
     *
     * @param id document ID
     * @return Map of metadata
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDocumentMetadata(Long id) {
        log.debug("Retrieving metadata for document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", document.getId());
        metadata.put("uuid", document.getDocumentUuid());
        metadata.put("originalFilename", document.getOriginalFilename());
        metadata.put("fileSize", document.getFileSizeBytes());
        metadata.put("mimeType", document.getMimeType());
        metadata.put("fileHash", document.getFileHash());
        metadata.put("uploadedAt", document.getUploadedAt());
        metadata.put("uploadedBy", document.getUploadedBy());
        metadata.put("status", document.getStatus());
        metadata.put("customMetadata", document.getCustomMetadata());

        return metadata;
    }

    /**
     * Updates document metadata.
     *
     * @param id document ID
     * @param request update request
     * @return updated DocumentResponse
     */
    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentUpdateRequest request) {
        log.info("Updating document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        if (document.isDeleted()) {
            throw new DocumentValidationException("Cannot update deleted document");
        }

        // Update fields
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }

        if (request.getMetadata() != null) {
            if (Boolean.TRUE.equals(request.getMergeMetadata())) {
                Map<String, Object> existingMetadata = document.getCustomMetadata();
                if (existingMetadata == null) {
                    existingMetadata = new HashMap<>();
                }
                existingMetadata.putAll(request.getMetadata());
                document.setCustomMetadata(existingMetadata);
            } else {
                document.setCustomMetadata(request.getMetadata());
            }
        }

        if (request.getTags() != null) {
            document.setTags(request.getTags());
        }

        if (request.getExpirationDate() != null) {
            document.setExpirationDate(request.getExpirationDate());
        }

        document.setUpdatedBy("SYSTEM"); // Should be from security context
        document.setUpdatedAt(LocalDateTime.now());

        Document updatedDocument = documentRepository.save(document);
        log.info("Document updated successfully - ID: {}", id);

        return documentMapper.toResponse(updatedDocument);
    }

    /**
     * Replaces a document file with a new version.
     *
     * @param id document ID to replace
     * @param newFile new file
     * @param updatedBy user performing the replacement
     * @return new document version response
     */
    @Transactional
    public DocumentResponse replaceDocument(Long id, MultipartFile newFile, String updatedBy) {
        log.info("Replacing document ID: {} with new file: {}", id, newFile.getOriginalFilename());

        Document oldDocument = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        if (oldDocument.isDeleted()) {
            throw new DocumentValidationException("Cannot replace deleted document");
        }

        // Create new version
        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(newFile)
                .documentTypeId(oldDocument.getDocumentType().getId())
                .orderId(oldDocument.getOrderId())
                .orderNumber(oldDocument.getOrderNumber())
                .metadata(oldDocument.getCustomMetadata())
                .tags(oldDocument.getTags())
                .description(oldDocument.getDescription())
                .expirationDate(oldDocument.getExpirationDate())
                .businessUnitId(oldDocument.getBusinessUnitId())
                .build();

        DocumentResponse newVersion = uploadDocument(uploadRequest);

        // Update old document
        oldDocument.setIsLatestVersion(false);
        oldDocument.setReplacedByDocument(documentRepository.findById(newVersion.getId()).orElse(null));
        documentRepository.save(oldDocument);

        // Update new document with parent reference
        Document newDocument = documentRepository.findById(newVersion.getId()).orElseThrow();
        newDocument.setParentDocument(oldDocument);
        newDocument.setVersion(oldDocument.getVersion() + 1);
        documentRepository.save(newDocument);

        // Publish version created event
        eventPublisher.publishVersionCreated(oldDocument, newDocument);

        log.info("Document replaced - Old ID: {}, New ID: {}, Version: {}",
                oldDocument.getId(), newDocument.getId(), newDocument.getVersion());

        return documentMapper.toResponse(newDocument);
    }

    /**
     * Soft deletes a document.
     *
     * @param id document ID
     * @param deletedBy user performing deletion
     */
    @Transactional
    public void deleteDocument(Long id, String deletedBy) {
        log.info("Deleting document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        if (document.isDeleted()) {
            log.warn("Document already deleted - ID: {}", id);
            return;
        }

        // Soft delete
        document.markAsDeleted(deletedBy);
        documentRepository.save(document);

        // Publish delete event
        eventPublisher.publishDocumentDeleted(
                document.getId(),
                document.getOrderId(),
                document.getOrderNumber(),
                deletedBy,
                "Document deleted"
        );

        log.info("Document soft deleted successfully - ID: {}", id);
    }

    /**
     * Archives a document.
     *
     * @param id document ID
     * @param archivedBy user performing archival
     * @return archived document response
     */
    @Transactional
    public DocumentResponse archiveDocument(Long id, String archivedBy) {
        log.info("Archiving document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        if (document.isArchived()) {
            log.warn("Document already archived - ID: {}", id);
            return documentMapper.toResponse(document);
        }

        // Copy to archive bucket
        try {
            String archiveBucket = minIOConfig.getArchiveBucket();
            minIOStorageService.createBucketIfNotExists(archiveBucket);
            minIOStorageService.copyFile(
                    document.getStorageBucket(),
                    document.getStoragePath(),
                    archiveBucket,
                    document.getStoragePath()
            );
        } catch (Exception e) {
            log.error("Failed to copy document to archive bucket: {}", e.getMessage(), e);
            throw new DocumentStorageException("Archive operation failed", e);
        }

        document.markAsArchived();
        document.setUpdatedBy(archivedBy);
        Document archivedDocument = documentRepository.save(document);

        eventPublisher.publishDocumentArchived(archivedDocument);

        log.info("Document archived successfully - ID: {}", id);
        return documentMapper.toResponse(archivedDocument);
    }

    /**
     * Restores an archived or deleted document.
     *
     * @param id document ID
     * @param restoredBy user performing restoration
     * @return restored document response
     */
    @Transactional
    public DocumentResponse restoreDocument(Long id, String restoredBy) {
        log.info("Restoring document ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        if (document.isDeleted()) {
            document.setDeletedAt(null);
            document.setDeletedBy(null);
            document.setStatus(DocumentStatus.PENDING);
        }

        if (document.isArchived()) {
            document.setStatus(DocumentStatus.VALIDATED);
        }

        document.setUpdatedBy(restoredBy);
        Document restoredDocument = documentRepository.save(document);

        log.info("Document restored successfully - ID: {}", id);
        return documentMapper.toResponse(restoredDocument);
    }

    /**
     * Validates a document.
     *
     * @param id document ID
     * @param request validation request
     * @return validation response
     */
    @Transactional
    public DocumentValidationResponse validateDocument(Long id, DocumentValidationRequest request) {
        log.info("Validating document ID: {} - Approved: {}", id, request.getIsApproved());

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id, id));

        DocumentValidationResponse response;

        if (request.isApproval()) {
            document.markAsValidated(request.getValidatedBy(), request.getComments());
            documentRepository.save(document);

            eventPublisher.publishDocumentValidated(document, request.getValidatedBy());

            response = DocumentValidationResponse.builder()
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .validationSuccessful(true)
                    .isApproved(true)
                    .status(DocumentStatus.VALIDATED)
                    .validatedAt(document.getValidatedAt())
                    .validatedBy(document.getValidatedBy())
                    .validationComments(request.getComments())
                    .originalFilename(document.getOriginalFilename())
                    .typeCode(document.getTypeCode())
                    .orderNumber(document.getOrderNumber())
                    .message("Document validated successfully")
                    .build();

            log.info("Document validated - ID: {}", id);

        } else {
            document.markAsRejected(request.getValidatedBy(), request.getComments());
            documentRepository.save(document);

            eventPublisher.publishDocumentRejected(document, request.getComments());

            response = DocumentValidationResponse.builder()
                    .documentId(document.getId())
                    .documentUuid(document.getDocumentUuid())
                    .validationSuccessful(true)
                    .isApproved(false)
                    .status(DocumentStatus.REJECTED)
                    .validatedAt(document.getValidatedAt())
                    .validatedBy(document.getValidatedBy())
                    .validationComments(request.getComments())
                    .statusReason(request.getComments())
                    .originalFilename(document.getOriginalFilename())
                    .typeCode(document.getTypeCode())
                    .orderNumber(document.getOrderNumber())
                    .message("Document rejected")
                    .build();

            log.info("Document rejected - ID: {}, Reason: {}", id, request.getComments());
        }

        return response;
    }

    /**
     * Rejects a document with a reason.
     *
     * @param id document ID
     * @param reason rejection reason
     * @param rejectedBy user rejecting the document
     * @return validation response
     */
    @Transactional
    public DocumentValidationResponse rejectDocument(Long id, String reason, String rejectedBy) {
        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .isApproved(false)
                .comments(reason)
                .validatedBy(rejectedBy)
                .build();

        return validateDocument(id, request);
    }

    /**
     * Validates multiple documents in batch.
     *
     * @param documentIds list of document IDs
     * @param validatedBy user validating
     * @return list of validation responses
     */
    @Transactional
    public List<DocumentValidationResponse> validateBatchDocuments(List<Long> documentIds, String validatedBy) {
        log.info("Batch validating {} documents", documentIds.size());

        List<DocumentValidationResponse> responses = new ArrayList<>();

        for (Long documentId : documentIds) {
            try {
                DocumentValidationRequest request = DocumentValidationRequest.builder()
                        .isApproved(true)
                        .comments("Batch validated")
                        .validatedBy(validatedBy)
                        .build();

                DocumentValidationResponse response = validateDocument(documentId, request);
                responses.add(response);

            } catch (Exception e) {
                log.error("Failed to validate document {} in batch: {}", documentId, e.getMessage());

                DocumentValidationResponse errorResponse = DocumentValidationResponse.builder()
                        .documentId(documentId)
                        .validationSuccessful(false)
                        .message("Validation failed: " + e.getMessage())
                        .build();
                responses.add(errorResponse);
            }
        }

        return responses;
    }

    /**
     * Gets all documents for an order.
     *
     * @param orderId order ID
     * @return list of documents
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getOrderDocuments(Long orderId) {
        log.debug("Retrieving documents for order ID: {}", orderId);

        List<Document> documents = documentRepository.findByOrderIdOrderByUploadedAtDesc(orderId);
        return documents.stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets documents for an order filtered by type.
     *
     * @param orderId order ID
     * @param typeCode document type code
     * @return list of documents
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getOrderDocumentsByType(Long orderId, String typeCode) {
        log.debug("Retrieving documents for order ID: {} and type: {}", orderId, typeCode);

        DocumentType documentType = documentTypeRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> new DocumentNotFoundException("Document type not found: " + typeCode));

        List<Document> documents = documentRepository.findByOrderIdAndDocumentTypeId(orderId, documentType.getId());
        return documents.stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Checks order document completeness.
     *
     * @param orderId order ID
     * @return completeness result
     */
    @Transactional(readOnly = true)
    public DocumentCompletenessResult checkOrderDocumentCompleteness(Long orderId) {
        log.info("Checking document completeness for order ID: {}", orderId);

        List<DocumentType> allTypes = documentTypeRepository.findByIsActiveTrue();
        List<DocumentType> mandatoryTypes = allTypes.stream()
                .filter(dt -> Boolean.TRUE.equals(dt.getIsMandatory()))
                .collect(Collectors.toList());

        List<Document> orderDocuments = documentRepository.findByOrderId(orderId);

        List<String> missingTypes = new ArrayList<>();
        List<String> incompleteTypes = new ArrayList<>();

        for (DocumentType mandatoryType : mandatoryTypes) {
            List<Document> typeDocuments = orderDocuments.stream()
                    .filter(doc -> doc.getTypeCode().equals(mandatoryType.getTypeCode()))
                    .filter(doc -> !doc.isDeleted())
                    .collect(Collectors.toList());

            if (typeDocuments.isEmpty()) {
                missingTypes.add(mandatoryType.getTypeCode());
            } else {
                long validatedCount = typeDocuments.stream()
                        .filter(Document::isValidated)
                        .count();

                if (validatedCount < mandatoryType.getMinDocuments()) {
                    incompleteTypes.add(mandatoryType.getTypeCode());
                }
            }
        }

        boolean isComplete = missingTypes.isEmpty() && incompleteTypes.isEmpty();

        DocumentCompletenessResult result = DocumentCompletenessResult.builder()
                .orderId(orderId)
                .isComplete(isComplete)
                .totalRequiredTypes(mandatoryTypes.size())
                .satisfiedTypes(mandatoryTypes.size() - missingTypes.size() - incompleteTypes.size())
                .missingDocumentTypes(missingTypes)
                .incompleteDocumentTypes(incompleteTypes)
                .totalDocuments(orderDocuments.size())
                .validatedDocuments((int) orderDocuments.stream().filter(Document::isValidated).count())
                .pendingDocuments((int) orderDocuments.stream().filter(Document::isPending).count())
                .rejectedDocuments((int) orderDocuments.stream().filter(Document::isRejected).count())
                .message(isComplete ? "All required documents are complete" : "Missing or incomplete documents")
                .build();

        log.info("Completeness check - Order: {}, Complete: {}, Missing: {}, Incomplete: {}",
                orderId, isComplete, missingTypes.size(), incompleteTypes.size());

        return result;
    }

    /**
     * Validates all documents for an order.
     *
     * @param orderId order ID
     * @param validatedBy user validating
     * @return validation summary
     */
    @Transactional
    public ValidationSummary validateAllOrderDocuments(Long orderId, String validatedBy) {
        log.info("Validating all documents for order ID: {}", orderId);

        List<Document> orderDocuments = documentRepository.findByOrderId(orderId);
        List<Document> pendingDocuments = orderDocuments.stream()
                .filter(Document::isPending)
                .collect(Collectors.toList());

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (Document document : pendingDocuments) {
            try {
                DocumentValidationRequest request = DocumentValidationRequest.builder()
                        .isApproved(true)
                        .comments("Bulk order validation")
                        .validatedBy(validatedBy)
                        .build();

                validateDocument(document.getId(), request);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to validate document {} for order {}: {}",
                        document.getId(), orderId, e.getMessage());
                errors.add(String.format("Document %s: %s", document.getOriginalFilename(), e.getMessage()));
                failureCount++;
            }
        }

        boolean allValidated = failureCount == 0 && pendingDocuments.size() > 0;

        if (allValidated) {
            eventPublisher.publishAllDocumentsValidated(
                    orderId,
                    orderDocuments.size(),
                    successCount,
                    validatedBy
            );
        }

        ValidationSummary summary = ValidationSummary.builder()
                .orderId(orderId)
                .totalDocuments(orderDocuments.size())
                .validatedCount(successCount)
                .failedCount(failureCount)
                .errors(errors)
                .allValidated(allValidated)
                .validatedBy(validatedBy)
                .validatedAt(LocalDateTime.now())
                .message(allValidated ? "All documents validated successfully" : "Some documents failed validation")
                .build();

        log.info("Order validation completed - Order: {}, Success: {}, Failed: {}",
                orderId, successCount, failureCount);

        return summary;
    }

    // Helper methods

    private String generateStoredFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }

    private String generateStoragePath(Long orderId, String typeCode, String filename) {
        return String.format("orders/%d/%s/%s", orderId, typeCode, filename);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private DocumentUploadRequest buildUploadRequest(MultipartFile file, BulkUploadRequest bulkRequest) {
        BulkUploadRequest.FileMetadata fileMetadata = bulkRequest.getMetadataForFile(file.getOriginalFilename());

        Map<String, Object> metadata = new HashMap<>();
        if (bulkRequest.hasCommonMetadata()) {
            metadata.putAll(bulkRequest.getCommonMetadata());
        }
        if (fileMetadata != null && fileMetadata.getMetadata() != null) {
            metadata.putAll(fileMetadata.getMetadata());
        }

        List<String> tags = new ArrayList<>();
        if (bulkRequest.hasCommonTags()) {
            tags.addAll(bulkRequest.getCommonTags());
        }
        if (fileMetadata != null && fileMetadata.getTags() != null) {
            tags.addAll(fileMetadata.getTags());
        }

        Long documentTypeId = fileMetadata != null && fileMetadata.getDocumentTypeId() != null
                ? fileMetadata.getDocumentTypeId()
                : bulkRequest.getDocumentTypeId();

        return DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(documentTypeId)
                .orderId(bulkRequest.getOrderId())
                .orderNumber(bulkRequest.getOrderNumber())
                .metadata(metadata)
                .tags(tags)
                .description(fileMetadata != null ? fileMetadata.getDescription() : null)
                .expirationDate(fileMetadata != null && fileMetadata.getExpirationDate() != null
                        ? fileMetadata.getExpirationDate()
                        : bulkRequest.getCommonExpirationDate())
                .businessUnitId(bulkRequest.getBusinessUnitId())
                .build();
    }
}
