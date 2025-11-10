package com.eflo.document.integration;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentSearchRequest;
import com.eflo.document.domain.model.DocumentSummaryResponse;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DocumentSearchService.
 * Tests complex searches with real data and pagination.
 *
 * @author Document Service
 * @version 1.0
 */
class DocumentSearchServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DocumentSearchService documentSearchService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private DocumentType invoiceType;
    private DocumentType contractType;
    private DocumentType receiptType;

    @BeforeEach
    void setUp() {
        // Create document types
        invoiceType = createDocumentType("INVOICE", "Invoice", DocumentCategory.FINANCIAL);
        contractType = createDocumentType("CONTRACT", "Contract", DocumentCategory.LEGAL);
        receiptType = createDocumentType("RECEIPT", "Receipt", DocumentCategory.FINANCIAL);
    }

    @Test
    @DisplayName("Should search documents by order ID")
    void testSearchByOrderId() {
        // Given
        Long orderId = 1001L;
        createTestDocument("doc1.pdf", "ORD-001", orderId, invoiceType);
        createTestDocument("doc2.pdf", "ORD-001", orderId, invoiceType);
        createTestDocument("doc3.pdf", "ORD-002", 1002L, invoiceType); // Different order

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .orderId(orderId)
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results).isNotNull();
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(doc -> doc.getOrderId().equals(orderId));
    }

    @Test
    @DisplayName("Should search documents by type code")
    void testSearchByTypeCode() {
        // Given
        createTestDocument("invoice.pdf", "ORD-001", 1001L, invoiceType);
        createTestDocument("contract.pdf", "ORD-002", 1002L, contractType);
        createTestDocument("receipt.pdf", "ORD-003", 1003L, receiptType);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .typeCode("INVOICE")
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTypeCode()).isEqualTo("INVOICE");
    }

    @Test
    @DisplayName("Should search documents by status")
    void testSearchByStatus() {
        // Given
        Document pending1 = createTestDocument("pending1.pdf", "ORD-001", 1001L, invoiceType);
        Document pending2 = createTestDocument("pending2.pdf", "ORD-002", 1002L, invoiceType);

        Document validated = createTestDocument("validated.pdf", "ORD-003", 1003L, invoiceType);
        validated.setStatus(DocumentStatus.VALIDATED);
        documentRepository.save(validated);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .status(DocumentStatus.PENDING)
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(doc ->
            doc.getStatus().equals(DocumentStatus.PENDING));
    }

    @Test
    @DisplayName("Should search documents by date range")
    void testSearchByDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        Document oldDoc = createTestDocument("old.pdf", "ORD-001", 1001L, invoiceType);
        oldDoc.setUploadedAt(LocalDateTime.now().minusDays(10));
        documentRepository.save(oldDoc);

        Document recentDoc = createTestDocument("recent.pdf", "ORD-002", 1002L, invoiceType);
        recentDoc.setUploadedAt(LocalDateTime.now().minusDays(3));
        documentRepository.save(recentDoc);

        Document todayDoc = createTestDocument("today.pdf", "ORD-003", 1003L, invoiceType);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .uploadedFrom(startDate.toLocalDate())
            .uploadedTo(endDate.toLocalDate())
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getOriginalFilename()).isEqualTo("recent.pdf");
    }

    @Test
    @DisplayName("Should search documents with multiple criteria")
    void testSearchWithMultipleCriteria() {
        // Given
        Long orderId = 1001L;

        Document match = createTestDocument("invoice1.pdf", "ORD-001", orderId, invoiceType);
        match.setStatus(DocumentStatus.VALIDATED);
        documentRepository.save(match);

        createTestDocument("invoice2.pdf", "ORD-002", 1002L, invoiceType); // Wrong order
        createTestDocument("contract.pdf", "ORD-001", orderId, contractType); // Wrong type

        Document wrongStatus = createTestDocument("invoice3.pdf", "ORD-001", orderId, invoiceType);
        wrongStatus.setStatus(DocumentStatus.REJECTED);
        documentRepository.save(wrongStatus);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .orderId(orderId)
            .typeCode("INVOICE")
            .status(DocumentStatus.VALIDATED)
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getOriginalFilename()).isEqualTo("invoice1.pdf");
    }

    @Test
    @DisplayName("Should search documents by filename")
    void testSearchByFilename() {
        // Given
        createTestDocument("invoice-2024-q1.pdf", "ORD-001", 1001L, invoiceType);
        createTestDocument("invoice-2024-q2.pdf", "ORD-002", 1002L, invoiceType);
        createTestDocument("contract-2024.pdf", "ORD-003", 1003L, contractType);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .filename("invoice-2024")
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(doc ->
            doc.getOriginalFilename().contains("invoice-2024"));
    }

    @Test
    @DisplayName("Should search documents by category")
    void testSearchByCategory() {
        // Given
        createTestDocument("invoice.pdf", "ORD-001", 1001L, invoiceType);
        createTestDocument("receipt.pdf", "ORD-002", 1002L, receiptType);
        createTestDocument("contract.pdf", "ORD-003", 1003L, contractType);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .category(DocumentCategory.FINANCIAL)
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(doc ->
            doc.getCategory().equals(DocumentCategory.FINANCIAL));
    }

    @Test
    @DisplayName("Should search documents by tags")
    void testSearchByTags() {
        // Given
        Document doc1 = createTestDocument("doc1.pdf", "ORD-001", 1001L, invoiceType);
        doc1.setTags(List.of("urgent", "q1", "2024"));
        documentRepository.save(doc1);

        Document doc2 = createTestDocument("doc2.pdf", "ORD-002", 1002L, invoiceType);
        doc2.setTags(List.of("urgent", "q2", "2024"));
        documentRepository.save(doc2);

        Document doc3 = createTestDocument("doc3.pdf", "ORD-003", 1003L, invoiceType);
        doc3.setTags(List.of("normal", "q1", "2024"));
        documentRepository.save(doc3);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .tags(List.of("urgent"))
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(doc ->
            doc.getTags().contains("urgent"));
    }

    @Test
    @DisplayName("Should support pagination")
    void testPagination() {
        // Given - Create 25 documents
        for (int i = 1; i <= 25; i++) {
            createTestDocument("doc" + i + ".pdf", "ORD-" + i, 1000L + i, invoiceType);
        }

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .typeCode("INVOICE")
            .build();

        // When - Get first page
        Page<DocumentSummaryResponse> page1 = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.hasNext()).isTrue();

        // When - Get second page
        Page<DocumentSummaryResponse> page2 = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(1, 10)
        );

        // Then
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page2.hasNext()).isTrue();

        // When - Get last page
        Page<DocumentSummaryResponse> page3 = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(2, 10)
        );

        // Then
        assertThat(page3.getContent()).hasSize(5);
        assertThat(page3.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should support sorting")
    void testSorting() {
        // Given
        Document doc1 = createTestDocument("a-doc.pdf", "ORD-001", 1001L, invoiceType);
        doc1.setUploadedAt(LocalDateTime.now().minusDays(3));
        documentRepository.save(doc1);

        Document doc2 = createTestDocument("b-doc.pdf", "ORD-002", 1002L, invoiceType);
        doc2.setUploadedAt(LocalDateTime.now().minusDays(1));
        documentRepository.save(doc2);

        Document doc3 = createTestDocument("c-doc.pdf", "ORD-003", 1003L, invoiceType);
        doc3.setUploadedAt(LocalDateTime.now().minusDays(2));
        documentRepository.save(doc3);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .typeCode("INVOICE")
            .build();

        // When - Sort by upload date ascending
        Page<DocumentSummaryResponse> ascResults = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10, Sort.by("uploadedAt").ascending())
        );

        // Then
        assertThat(ascResults.getContent().get(0).getOriginalFilename()).isEqualTo("a-doc.pdf");
        assertThat(ascResults.getContent().get(2).getOriginalFilename()).isEqualTo("b-doc.pdf");

        // When - Sort by upload date descending
        Page<DocumentSummaryResponse> descResults = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10, Sort.by("uploadedAt").descending())
        );

        // Then
        assertThat(descResults.getContent().get(0).getOriginalFilename()).isEqualTo("b-doc.pdf");
        assertThat(descResults.getContent().get(2).getOriginalFilename()).isEqualTo("a-doc.pdf");
    }

    @Test
    @DisplayName("Should search expiring documents")
    void testSearchExpiringDocuments() {
        // Given
        Document expiringSoon = createTestDocument("expiring.pdf", "ORD-001", 1001L, invoiceType);
        expiringSoon.setExpirationDate(LocalDate.now().plusDays(15));
        documentRepository.save(expiringSoon);

        Document notExpiring = createTestDocument("valid.pdf", "ORD-002", 1002L, invoiceType);
        notExpiring.setExpirationDate(LocalDate.now().plusYears(1));
        documentRepository.save(notExpiring);

        Document expired = createTestDocument("expired.pdf", "ORD-003", 1003L, invoiceType);
        expired.setExpirationDate(LocalDate.now().minusDays(1));
        documentRepository.save(expired);

        // When - Search documents expiring in next 30 days
        Page<DocumentSummaryResponse> results = documentSearchService.searchExpiringDocuments(
            30,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getOriginalFilename()).isEqualTo("expiring.pdf");
    }

    @Test
    @DisplayName("Should exclude deleted documents from search")
    void testExcludeDeletedDocuments() {
        // Given
        createTestDocument("active.pdf", "ORD-001", 1001L, invoiceType);

        Document deleted = createTestDocument("deleted.pdf", "ORD-002", 1002L, invoiceType);
        deleted.markAsDeleted("admin");
        documentRepository.save(deleted);

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .typeCode("INVOICE")
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getOriginalFilename()).isEqualTo("active.pdf");
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testEmptySearchResults() {
        // Given
        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .orderId(9999L) // Non-existent order
            .build();

        // When
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).isEmpty();
        assertThat(results.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should search with performance on large dataset")
    void testSearchPerformanceOnLargeDataset() {
        // Given - Create 100 documents
        for (int i = 1; i <= 100; i++) {
            Document doc = createTestDocument("doc" + i + ".pdf", "ORD-" + i, 1000L + i, invoiceType);
            if (i % 2 == 0) {
                doc.setStatus(DocumentStatus.VALIDATED);
            }
            if (i % 3 == 0) {
                doc.setTags(List.of("important"));
            }
            documentRepository.save(doc);
        }

        DocumentSearchRequest searchRequest = DocumentSearchRequest.builder()
            .typeCode("INVOICE")
            .status(DocumentStatus.VALIDATED)
            .tags(List.of("important"))
            .build();

        // When
        long startTime = System.currentTimeMillis();
        Page<DocumentSummaryResponse> results = documentSearchService.searchDocuments(
            searchRequest,
            PageRequest.of(0, 20)
        );
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(results).isNotNull();
        assertThat(endTime - startTime).isLessThan(1000); // Should complete in less than 1 second
        assertThat(results.getContent()).allMatch(doc ->
            doc.getStatus().equals(DocumentStatus.VALIDATED) &&
            doc.getTags().contains("important")
        );
    }

    // Helper methods

    private DocumentType createDocumentType(String code, String name, DocumentCategory category) {
        DocumentType type = DocumentType.builder()
            .typeCode(code)
            .typeName(name)
            .category(category)
            .isActive(true)
            .build();
        return documentTypeRepository.save(type);
    }

    private Document createTestDocument(
        String filename,
        String orderNumber,
        Long orderId,
        DocumentType documentType
    ) {
        Document document = Document.builder()
            .documentUuid(UUID.randomUUID())
            .documentType(documentType)
            .typeCode(documentType.getTypeCode())
            .orderId(orderId)
            .orderNumber(orderNumber)
            .originalFilename(filename)
            .storedFilename("stored-" + filename)
            .fileExtension("pdf")
            .mimeType("application/pdf")
            .fileSizeBytes(1000L)
            .fileHash("hash-" + UUID.randomUUID())
            .storageBucket("documents")
            .storagePath("orders/" + orderId + "/" + filename)
            .version(1)
            .isLatestVersion(true)
            .status(DocumentStatus.PENDING)
            .uploadedBy("test-user")
            .uploadedAt(LocalDateTime.now())
            .build();

        return documentRepository.save(document);
    }
}
