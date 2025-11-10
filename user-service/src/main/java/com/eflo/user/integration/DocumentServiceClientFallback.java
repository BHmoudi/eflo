package com.eflo.user.integration;

import com.eflo.user.integration.dto.DocumentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for DocumentServiceClient.
 * Provides graceful degradation when the Document Service is unavailable.
 */
@Slf4j
@Component
public class DocumentServiceClientFallback implements DocumentServiceClient {

    @Override
    public List<DocumentDTO> getDocumentsByUserId(String userId) {
        log.warn("Document Service unavailable. Returning empty list for user documents. UserId: {}", userId);
        return Collections.emptyList();
    }

    @Override
    public List<DocumentDTO> getDocumentsByBusinessUnitId(String businessUnitId) {
        log.warn("Document Service unavailable. Returning empty list for business unit documents. BusinessUnitId: {}", businessUnitId);
        return Collections.emptyList();
    }

    @Override
    public DocumentDTO getDocumentById(String documentId) {
        log.warn("Document Service unavailable. Returning null for document details. DocumentId: {}", documentId);
        return null;
    }

    @Override
    public Boolean checkUserAccess(String documentId, String userId) {
        log.warn("Document Service unavailable. Defaulting to no access. DocumentId: {}, UserId: {}", documentId, userId);
        return false;
    }

    @Override
    public List<DocumentDTO> getDocumentsByOrderId(String orderId) {
        log.warn("Document Service unavailable. Returning empty list for order documents. OrderId: {}", orderId);
        return Collections.emptyList();
    }
}
