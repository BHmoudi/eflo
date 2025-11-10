package com.eflo.workflow.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for Document Service
 *
 * Provides integration with the Document Service for document verification.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@FeignClient(name = "document-service", url = "${eflo.workflow.integration.document-service.url}")
public interface DocumentServiceClient {

    /**
     * Get documents for an order
     *
     * @param orderId Order ID
     * @return List of documents
     */
    @GetMapping("/api/v1/documents/order/{orderId}")
    List<Map<String, Object>> getDocumentsByOrderId(@PathVariable Long orderId);

    /**
     * Check if required documents are uploaded
     *
     * @param orderId Order ID
     * @param documentTypes Required document types
     * @return Verification result
     */
    @GetMapping("/api/v1/documents/order/{orderId}/verify")
    Map<String, Object> verifyRequiredDocuments(
            @PathVariable Long orderId,
            @RequestParam List<String> documentTypes
    );

    /**
     * Upload a document
     *
     * @param file Document file
     * @param orderId Order ID
     * @param taskId Task ID
     * @param documentType Document type
     * @return Document details
     */
    @PostMapping(value = "/api/v1/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long orderId,
            @RequestParam Long taskId,
            @RequestParam String documentType
    );

    /**
     * Get document by ID
     *
     * @param documentId Document ID
     * @return Document details
     */
    @GetMapping("/api/v1/documents/{documentId}")
    Map<String, Object> getDocument(@PathVariable Long documentId);

    /**
     * Delete document by ID
     *
     * @param documentId Document ID
     */
    @DeleteMapping("/api/v1/documents/{documentId}")
    void deleteDocument(@PathVariable Long documentId);

    /**
     * Get documents by task ID
     *
     * @param taskId Task ID
     * @return List of documents
     */
    @GetMapping("/api/v1/documents/task/{taskId}")
    List<Map<String, Object>> getDocumentsByTask(@PathVariable Long taskId);
}
