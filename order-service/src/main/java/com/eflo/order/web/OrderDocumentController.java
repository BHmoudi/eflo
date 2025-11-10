package com.eflo.order.web;

import com.eflo.order.domain.entity.DocumentType;
import com.eflo.order.domain.model.dto.OrderDocumentDTO;
import com.eflo.order.service.OrderDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.eflo.order.security.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/documents")
@RequiredArgsConstructor
@Tag(name = "Order Document Management", description = "APIs for managing order documents and attachments")
public class OrderDocumentController {

    private final OrderDocumentService documentService;

    /**
     * Upload a document for an order
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDocumentDTO> uploadDocument(
            @PathVariable Long orderId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = SecurityUtils.getUserId(jwt);
        OrderDocumentDTO document = documentService.uploadDocument(orderId, file, documentType, description, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    /**
     * Get all documents for an order
     */
    @GetMapping
    @Operation(summary = "Get all documents for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDocumentDTO>> getOrderDocuments(@PathVariable Long orderId) {
        List<OrderDocumentDTO> documents = documentService.getOrderDocuments(orderId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get a specific document by ID
     */
    @GetMapping("/{documentId}")
    @Operation(summary = "Get document by ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<OrderDocumentDTO> getDocument(
            @PathVariable Long orderId,
            @PathVariable Long documentId) {
        OrderDocumentDTO document = documentService.getDocument(orderId, documentId);
        return ResponseEntity.ok(document);
    }

    /**
     * Download a document
     */
    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download document file")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long orderId,
            @PathVariable Long documentId) {

        // Get document metadata first
        OrderDocumentDTO documentDTO = documentService.getDocument(orderId, documentId);

        // Load file as resource
        Resource resource = documentService.downloadDocument(orderId, documentId);

        // Determine content type
        String contentType = documentDTO.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentDTO.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Delete a document
     */
    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a document")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long orderId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = SecurityUtils.getUserId(jwt);
        documentService.deleteDocument(orderId, documentId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get documents by type
     */
    @GetMapping("/type/{documentType}")
    @Operation(summary = "Get documents by type")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDocumentDTO>> getDocumentsByType(
            @PathVariable Long orderId,
            @PathVariable DocumentType documentType) {
        List<OrderDocumentDTO> documents = documentService.getDocumentsByType(orderId, documentType);
        return ResponseEntity.ok(documents);
    }

    /**
     * Extract user ID from JWT token
     */
    // User extraction centralized in SecurityUtils
}
