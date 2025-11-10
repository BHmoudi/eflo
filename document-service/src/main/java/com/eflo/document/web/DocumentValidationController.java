package com.eflo.document.web;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.model.DocumentValidationRequest;
import com.eflo.document.domain.model.DocumentValidationResponse;
import com.eflo.document.domain.model.ScanResult;
import com.eflo.document.domain.model.ValidationResult;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.scanner.VirusScanningService;
import com.eflo.document.service.DocumentEventPublisher;
import com.eflo.document.service.DocumentManagementService;
import com.eflo.document.service.DocumentSearchService;
import com.eflo.document.storage.MinIOStorageService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.IOException;

/**
 * REST Controller for document validation operations.
 * Provides endpoints for validating, rejecting, and managing document validation status.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Validation", description = "APIs for document validation and verification")
@SecurityRequirement(name = "bearer-auth")
public class DocumentValidationController {

    private final DocumentManagementService documentService;
    private final DocumentSearchService documentSearchService;
    private final DocumentRepository documentRepository;
    private final MinIOStorageService storageService;
    private final VirusScanningService virusScanningService;
    private final DocumentEventPublisher eventPublisher;

    /**
     * Validate a document.
     *
     * @param id document ID
     * @param request validation request with comments and result
     * @return validation result
     */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'DOCUMENT_VALIDATOR')")
    @Operation(summary = "Validate document", description = "Validates a document and marks it as approved")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document validated successfully",
            content = @Content(schema = @Schema(implementation = DocumentValidationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "400", description = "Invalid validation request"),
        @ApiResponse(responseCode = "409", description = "Document already validated or in invalid state"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<DocumentValidationResponse> validateDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DocumentValidationRequest request) {

        log.info("Validating document: id={}, validator={}", id, request.getValidatedBy());
        DocumentValidationResponse response = documentService.validateDocument(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a document.
     *
     * @param id document ID
     * @param request rejection request with reason
     * @return validation result
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'DOCUMENT_VALIDATOR')")
    @Operation(summary = "Reject document", description = "Rejects a document with a reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document rejected successfully",
            content = @Content(schema = @Schema(implementation = DocumentValidationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "400", description = "Invalid rejection request - reason required"),
        @ApiResponse(responseCode = "409", description = "Document in invalid state for rejection"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<DocumentValidationResponse> rejectDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DocumentValidationRequest request) {

        log.info("Rejecting document: id={}, reason={}", id, request.getComments());
        // Ensure isApproved = false for rejection
        request.setIsApproved(false);
        DocumentValidationResponse response = documentService.validateDocument(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate multiple documents in batch.
     *
     * @param documentIds list of document IDs to validate
     * @param comments optional validation comments
     * @return batch validation results
     */
    @PostMapping("/validate-batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'DOCUMENT_VALIDATOR')")
    @Operation(summary = "Batch validate documents", description = "Validates multiple documents at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Batch validation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request - document IDs required"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<Map<Long, ValidationResult>> validateBatch(
            @Parameter(description = "List of document IDs", required = true)
            @RequestParam("documentIds") List<Long> documentIds,
            @Parameter(description = "Validation comments")
            @RequestParam(value = "comments", required = false) String comments) {

        log.info("Batch validating documents: count={}", documentIds.size());
        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";

        Map<Long, ValidationResult> results = new HashMap<>();
        for (Long docId : documentIds) {
            try {
                DocumentValidationRequest req = DocumentValidationRequest.builder()
                        .isApproved(true)
                        .comments(comments)
                        .validatedBy(user)
                        .build();
                documentService.validateDocument(docId, req);
                results.put(docId, ValidationResult.valid());
            } catch (Exception e) {
                results.put(docId, ValidationResult.invalid(e.getMessage()));
            }
        }
        return ResponseEntity.ok(results);
    }

    /**
     * Get validation status for all documents in an order.
     *
     * @param orderId order ID
     * @return validation status summary
     */
    @GetMapping("/validation-status/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER', 'VALIDATOR')")
    @Operation(summary = "Get order validation status",
               description = "Retrieves validation status for all documents in an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> getOrderValidationStatus(
            @Parameter(description = "Order ID", required = true)
            @PathVariable Long orderId) {

        log.info("Retrieving validation status for order: orderId={}", orderId);
        Map<String, Object> status = documentSearchService.getOrderDocumentStatus(orderId);
        // Add progress percentage if possible
        try {
            long total = ((Number) status.getOrDefault("totalDocuments", 0)).longValue();
            long validated = ((Number) status.getOrDefault("validatedCount", status.getOrDefault("validatedDocuments", 0))).longValue();
            double progress = total > 0 ? (validated * 100.0 / total) : 0.0;
            status.put("validationProgress", progress);
        } catch (Exception ignored) { }
        return ResponseEntity.ok(status);
    }

    /**
     * Rescan a document for viruses.
     *
     * @param id document ID
     * @return scan result
     */
    @PostMapping("/{id}/rescan")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ADMIN')")
    @Operation(summary = "Rescan document for viruses",
               description = "Initiates a new virus scan for a document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scan initiated successfully",
            content = @Content(schema = @Schema(implementation = ScanResult.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "503", description = "Virus scanner unavailable"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
    })
    public ResponseEntity<ScanResult> rescanForVirus(
            @Parameter(description = "Document ID", required = true)
            @PathVariable Long id) {

        log.info("Rescanning document for viruses: id={}", id);
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Document", id));

        try (InputStream input = storageService.downloadFile(document.getStorageBucket(), document.getStoragePath())) {
            com.eflo.document.scanner.ScanResult scan = virusScanningService.scanDocument(input, document.getOriginalFilename());

            // Update entity
            document.recordVirusScan(scan.getStatus(), scan.toMap());
            documentRepository.save(document);

            // Publish event
            eventPublisher.publishScanCompleted(document, scan);

            ScanResult response = ScanResult.builder()
                    .scanStatus(document.getVirusScanStatus())
                    .scanDate(document.getVirusScanDate())
                    .details(document.getVirusScanResult())
                    .message(scan.getMessage())
                    .build();
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            throw new DocumentStorageException("Virus rescan failed", e);
        }
    }

    /**
     * Get virus scan status for documents.
     *
     * @param documentIds optional list of document IDs to check
     * @return scan status map
     */
    @GetMapping("/scan-status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SECURITY_ADMIN')")
    @Operation(summary = "Get virus scan status",
               description = "Retrieves virus scan status for specified documents or all pending scans")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scan status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<Long, ScanResult>> getScanStatus(
            @Parameter(description = "List of document IDs to check")
            @RequestParam(value = "documentIds", required = false) List<Long> documentIds) {

        if (documentIds != null && !documentIds.isEmpty()) {
            log.info("Retrieving scan status for documents: count={}", documentIds.size());
        } else {
            log.info("Retrieving scan status for all pending scans");
        }
        Map<Long, ScanResult> result = new HashMap<>();
        List<Document> docs;
        if (documentIds != null && !documentIds.isEmpty()) {
            docs = documentRepository.findAllById(documentIds);
        } else {
            // default: all pending
            docs = documentRepository.findByVirusScanStatus(VirusScanStatus.PENDING);
        }
        for (Document d : docs) {
            ScanResult sr = ScanResult.builder()
                    .scanStatus(d.getVirusScanStatus())
                    .scanDate(d.getVirusScanDate())
                    .details(d.getVirusScanResult())
                    .build();
            result.put(d.getId(), sr);
        }
        return ResponseEntity.ok(result);
    }
}
