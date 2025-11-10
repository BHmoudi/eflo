package com.eflo.user.integration;

import com.eflo.user.integration.dto.DocumentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with the Document Service.
 */
@FeignClient(
        name = "document-service",
        fallback = DocumentServiceClientFallback.class
)
public interface DocumentServiceClient {

    /**
     * Get all documents for a specific user.
     *
     * @param userId the user ID
     * @return list of documents
     */
    @GetMapping("/api/documents/user/{userId}")
    List<DocumentDTO> getDocumentsByUserId(@PathVariable("userId") String userId);

    /**
     * Get documents for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return list of documents
     */
    @GetMapping("/api/documents/business-unit/{businessUnitId}")
    List<DocumentDTO> getDocumentsByBusinessUnitId(@PathVariable("businessUnitId") String businessUnitId);

    /**
     * Get document by ID.
     *
     * @param documentId the document ID
     * @return document details
     */
    @GetMapping("/api/documents/{documentId}")
    DocumentDTO getDocumentById(@PathVariable("documentId") String documentId);

    /**
     * Check if user has access to a specific document.
     *
     * @param userId     the user ID
     * @param documentId the document ID
     * @return true if user has access
     */
    @GetMapping("/api/documents/{documentId}/access")
    Boolean checkUserAccess(@PathVariable("documentId") String documentId, @RequestParam("userId") String userId);

    /**
     * Get documents by order ID.
     *
     * @param orderId the order ID
     * @return list of documents
     */
    @GetMapping("/api/documents/order/{orderId}")
    List<DocumentDTO> getDocumentsByOrderId(@PathVariable("orderId") String orderId);
}
