package com.eflo.document.web;

import com.eflo.document.domain.model.*;
import com.eflo.document.mapper.DocumentMapper;
import com.eflo.document.service.DocumentManagementService;
import com.eflo.document.service.DocumentSearchService;
import com.eflo.document.service.DocumentVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for document operations.
 * Provides endpoints for document upload, download, management, and retrieval.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for managing documents")
@SecurityRequirement(name = "bearer-auth")
public class DocumentController {

    private final DocumentManagementService documentService;
    private final DocumentSearchService documentSearchService;
    private final DocumentVersionService documentVersionService;
    private final DocumentMapper documentMapper;

    /**
     * Upload a single document.
     *
     * @param file the file to upload
     * @param documentTypeId document type ID
     * @param orderId order ID
     * @param orderNumber order number
     * @param description optional description
     * @param tags optional tags
     * @param businessUnitId optional business unit ID
     * @return upload result with document details
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCUMENT_UPLOADER')")
    @Operation(summary = "Upload a document", description = "Uploads a single document with metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document uploaded successfully",
            content = @Content(schema = @Schema(implementation = UploadResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported file type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UploadResultResponse> uploadDocument(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document type ID", required = true)
            @RequestParam("documentTypeId") Long documentTypeId,
            @Parameter(description = "Order ID", required = true)
            @RequestParam("orderId") Long orderId,
            @Parameter(description = "Order number", required = true)
            @RequestParam("orderNumber") String orderNumber,
            @Parameter(description = "Document description")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Document tags (comma-separated)")
            @RequestParam(value = "tags", required = false) List<String> tags,
            @Parameter(description = "Business unit ID")
            @RequestParam(value = "businessUnitId", required = false) Long businessUnitId) {

        log.info("Uploading document: filename={}, typeId={}, orderId={}",
            file.getOriginalFilename(), documentTypeId, orderId);

        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(documentTypeId)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .description(description)
                .tags(tags)
                .businessUnitId(businessUnitId)
                .build();

        DocumentResponse saved = documentService.uploadDocument(request);
        UploadResultResponse result = UploadResultResponse.builder()
                .success(true)
                .documentId(saved.getId())
                .documentUuid(saved.getDocumentUuid())
                .typeCode(saved.getTypeCode())
                .fileSizeMB(saved.getFileSizeMB())
                .uploadedAt(saved.getUploadedAt())
                .message("Document uploaded successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Upload multiple documents at once.
     *
     * @param files list of files to upload
     * @param documentTypeId document type ID
     * @param orderId order ID
     * @param orderNumber order number
     * @return bulk upload result
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCUMENT_UPLOADER')")
    @Operation(summary = "Upload multiple documents", description = "Uploads multiple documents in a single request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Documents uploaded successfully",
            content = @Content(schema = @Schema(implementation = BulkUploadResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<BulkUploadResultResponse> uploadMultiple(
            @Parameter(description = "Files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Document type ID", required = true)
            @RequestParam("documentTypeId") Long documentTypeId,
            @Parameter(description = "Order ID", required = true)
            @RequestParam("orderId") Long orderId,
            @Parameter(description = "Order number", required = true)
            @RequestParam("orderNumber") String orderNumber) {

        log.info("Uploading multiple documents: count={}, typeId={}, orderId={}",
            files.size(), documentTypeId, orderId);

        BulkUploadRequest request = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(documentTypeId)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .continueOnError(true)
                .build();

        BulkUploadResultResponse result = documentService.uploadMultipleDocuments(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Get document details by ID.
     *
     * @param id document ID
     * @return document details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get document by ID", description = "Retrieves detailed document information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document found",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentResponse> getDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Retrieving document: id={}", id);

        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Download document file.
     *
     * @param id document ID
     * @return document file as stream
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Download document", description = "Downloads the document file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<InputStreamResource> downloadDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Downloading document: id={}", id);

        DocumentResponse document = documentService.getDocumentById(id);
        InputStreamResource resource = new InputStreamResource(documentService.downloadDocument(id));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getMimeType()));
        headers.setContentDispositionFormData("attachment", document.getOriginalFilename());
        if (document.getFileSizeBytes() != null) {
            headers.setContentLength(document.getFileSizeBytes());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * Get document preview (for supported formats).
     *
     * @param id document ID
     * @return preview data
     */
    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get document preview", description = "Generates or retrieves a preview of the document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preview generated successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "415", description = "Preview not supported for this file type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<InputStreamResource> getPreview(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Generating preview for document: id={}", id);

        byte[] bytes = documentService.getDocumentPreview(id);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));

        // Best effort content type resolution from document metadata
        DocumentResponse document = documentService.getDocumentById(id);
        MediaType contentType;
        try {
            contentType = MediaType.parseMediaType(document.getMimeType());
        } catch (Exception e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

    /**
     * Delete a document (soft delete).
     *
     * @param id document ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER')")
    @Operation(summary = "Delete document", description = "Soft deletes a document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Deleting document: id={}", id);

        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";
        documentService.deleteDocument(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Replace an existing document with a new version.
     *
     * @param id document ID to replace
     * @param file new file
     * @return upload result
     */
    @PutMapping(value = "/{id}/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCUMENT_UPLOADER')")
    @Operation(summary = "Replace document", description = "Replaces an existing document with a new version")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document replaced successfully",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentResponse> replaceDocument(
            @Parameter(description = "Document ID to replace", required = true)
            @PathVariable Long id,
            @Parameter(description = "New file", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Replacement reason")
            @RequestParam(value = "reason", required = false) String reason) {

        log.info("Replacing document: id={}, newFilename={}", id, file.getOriginalFilename());

        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";
        DocumentResponse response = documentService.replaceDocument(id, file, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Archive a document.
     *
     * @param id document ID
     * @return success response
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER')")
    @Operation(summary = "Archive document", description = "Archives a document for long-term storage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document archived successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentResponse> archiveDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Archiving document: id={}", id);

        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";
        DocumentResponse response = documentService.archiveDocument(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all versions of a document.
     *
     * @param id document ID
     * @return list of document versions
     */
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get document versions", description = "Retrieves all versions of a document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Versions retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<DocumentResponse>> getVersions(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Retrieving versions for document: id={}", id);

        List<com.eflo.document.domain.entity.Document> versions = documentVersionService.getAllVersions(id);
        List<DocumentResponse> response = versions.stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all documents for a specific order.
     *
     * @param orderId order ID
     * @param pageable pagination parameters
     * @return page of documents
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get documents by order", description = "Retrieves all documents associated with an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentSummaryResponse>> getOrderDocuments(
            @Parameter(description = "Order ID", required = true)
            @PathVariable Long orderId,
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {

        log.info("Retrieving documents for order: orderId={}, page={}", orderId, pageable.getPageNumber());

        List<com.eflo.document.domain.entity.Document> docs = documentSearchService.getOrderDocuments(orderId);
        List<DocumentSummaryResponse> summaries = docs.stream()
                .map(documentMapper::toSummaryResponse)
                .collect(Collectors.toList());
        Page<DocumentSummaryResponse> page = new org.springframework.data.domain.PageImpl<>(
                summaries,
                pageable,
                summaries.size()
        );
        return ResponseEntity.ok(page);
    }

    /**
     * Get documents by order and type.
     *
     * @param orderId order ID
     * @param typeCode document type code
     * @return list of documents
     */
    @GetMapping("/order/{orderId}/type/{typeCode}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get documents by order and type",
               description = "Retrieves documents for a specific order and document type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<DocumentResponse>> getByType(
            @Parameter(description = "Order ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "Document type code", required = true)
            @PathVariable String typeCode) {

        log.info("Retrieving documents: orderId={}, typeCode={}", orderId, typeCode);

        List<DocumentResponse> response = documentService.getOrderDocumentsByType(orderId, typeCode);
        return ResponseEntity.ok(response);
    }

    // REMOVED: Duplicate endpoint - search functionality is handled by DocumentSearchController
    // The /api/v1/documents/search endpoint is already defined in DocumentSearchController
    /**
     * Search documents with advanced filters.
     * NOTE: This endpoint has been moved to DocumentSearchController to avoid duplicate mappings.
     * Use POST /api/v1/documents/search via DocumentSearchController instead.
     *
     * @param searchRequest search criteria
     * @return page of matching documents
     */
    // @PostMapping("/search")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    // @Operation(summary = "Search documents", description = "Searches documents with advanced filtering")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Search completed successfully"),
    //     @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
    //     @ApiResponse(responseCode = "401", description = "Unauthorized"),
    //     @ApiResponse(responseCode = "403", description = "Forbidden")
    // })
    // public ResponseEntity<Page<DocumentSummaryResponse>> searchDocuments(
    //         @Valid @RequestBody DocumentSearchRequest searchRequest) {
    //
    //     log.info("Searching documents with criteria: {}", searchRequest);
    //
    //     // TODO: Implement document search
    //     throw new UnsupportedOperationException("Search not yet implemented");
    // }

    /**
     * Get documents pending validation.
     *
     * @param pageable pagination parameters
     * @return page of pending documents
     */
    @GetMapping("/pending-validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'DOCUMENT_VALIDATOR')")
    @Operation(summary = "Get pending documents", description = "Retrieves documents awaiting validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending documents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentSummaryResponse>> getPendingValidation(
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {

        log.info("Retrieving pending validation documents: page={}", pageable.getPageNumber());

        List<com.eflo.document.domain.entity.Document> docs = documentSearchService.getPendingValidation();
        List<DocumentSummaryResponse> summaries = docs.stream()
                .map(documentMapper::toSummaryResponse)
                .collect(Collectors.toList());
        Page<DocumentSummaryResponse> page = new org.springframework.data.domain.PageImpl<>(
                summaries,
                pageable,
                summaries.size()
        );
        return ResponseEntity.ok(page);
    }

    /**
     * Get documents expiring soon.
     *
     * @param days number of days to look ahead
     * @param pageable pagination parameters
     * @return page of expiring documents
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCUMENT_MANAGER')")
    @Operation(summary = "Get expiring documents", description = "Retrieves documents expiring within specified days")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expiring documents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentSummaryResponse>> getExpiring(
            @Parameter(description = "Days to look ahead", required = false)
            @RequestParam(value = "days", defaultValue = "30") Integer days,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {

        log.info("Retrieving expiring documents: days={}, page={}", days, pageable.getPageNumber());

        List<com.eflo.document.domain.entity.Document> docs = documentSearchService.getExpiringDocuments(days);
        List<DocumentSummaryResponse> summaries = docs.stream()
                .map(documentMapper::toSummaryResponse)
                .collect(Collectors.toList());
        Page<DocumentSummaryResponse> page = new org.springframework.data.domain.PageImpl<>(
                summaries,
                pageable,
                summaries.size()
        );
        return ResponseEntity.ok(page);
    }

    /**
     * Get document statistics.
     *
     * @return document statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER', 'VIEWER')")
    @Operation(summary = "Get document statistics", description = "Retrieves overall document statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocumentStatisticsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentStatisticsResponse> getStatistics() {

        log.info("Retrieving document statistics");

        DocumentStatisticsResponse response = DocumentStatisticsResponse.builder()
                .totalDocuments(0L)
                .activeDocuments(0L)
                .pendingDocuments(0L)
                .validatedDocuments(0L)
                .rejectedDocuments(0L)
                .expiredDocuments(0L)
                .archivedDocuments(0L)
                .deletedDocuments(0L)
                .expiringDocuments(0L)
                .virusScanPending(0L)
                .infectedDocuments(0L)
                .confidentialDocuments(0L)
                .totalStorageSizeBytes(0L)
                .totalStorageSizeGB(0.0)
                .averageFileSizeMB(0.0)
                .largestFileSizeMB(0.0)
                .smallestFileSizeMB(0.0)
                .build();
        // NOTE: Detailed stats available at /api/v1/documents/stats in DocumentReportController
        return ResponseEntity.ok(response);
    }
}
