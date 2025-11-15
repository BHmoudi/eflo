package com.eflo.document.integration;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.*;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentManagementService;
import com.eflo.document.storage.MinIOStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for DocumentManagementService.
 * Tests with real database and MinIO storage.
 *
 * @author Document Service
 * @version 1.0
 */
class DocumentManagementServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private MinIOStorageService minIOStorageService;

    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        // Create test document type
        testDocumentType = DocumentType.builder()
            .typeCode("INVOICE")
            .typeName("Invoice")
            .description("Invoice document")
            .category(DocumentCategory.FINANCIAL)
            .isActive(true)
            .isMandatory(true)
            .requiresValidation(true)
            .minDocuments(1)
            .maxDocuments(10)
            .allowedExtensions(new String[]{"pdf", "jpg", "png"})
            .maxFileSizeMB(10)
            .build();
        testDocumentType = documentTypeRepository.save(testDocumentType);

        // Ensure MinIO bucket exists
        minIOStorageService.createBucketIfNotExists("documents");
    }

    @Test
    @DisplayName("Should upload document end-to-end with real storage and database")
    void testUploadDocumentEndToEnd() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "invoice-2024.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Test invoice content for integration test".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest request = DocumentUploadRequest.builder()
            .file(file)
            .documentTypeId(testDocumentType.getId())
            .orderId(1001L)
            .orderNumber("ORD-2024-001")
            .description("Q1 2024 Invoice")
            .tags(List.of("invoice", "q1", "2024"))
            .businessUnitId(5L)
            .build();

        // When
        DocumentResponse response = documentManagementService.uploadDocument(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getDocumentUuid()).isNotNull();
        assertThat(response.getOriginalFilename()).isEqualTo("invoice-2024.pdf");
        assertThat(response.getTypeCode()).isEqualTo("INVOICE");
        assertThat(response.getOrderId()).isEqualTo(1001L);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-2024-001");
        assertThat(response.getStatus()).isIn(DocumentStatus.PENDING, DocumentStatus.QUARANTINED);
        assertThat(response.getTags()).containsExactlyInAnyOrder("invoice", "q1", "2024");

        // Verify database persistence
        Document savedDocument = documentRepository.findById(response.getId()).orElseThrow();
        assertThat(savedDocument.getOriginalFilename()).isEqualTo("invoice-2024.pdf");
        assertThat(savedDocument.getFileHash()).isNotEmpty();
        assertThat(savedDocument.getStorageBucket()).isEqualTo("documents");
        assertThat(savedDocument.getStoragePath()).isNotEmpty();

        // Verify MinIO storage
        boolean fileExists = minIOStorageService.fileExists(
            savedDocument.getStorageBucket(),
            savedDocument.getStoragePath()
        );
        assertThat(fileExists).isTrue();

        // Verify file content in storage
        InputStream downloadedStream = minIOStorageService.downloadFile(
            savedDocument.getStorageBucket(),
            savedDocument.getStoragePath()
        );
        String storedContent = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(storedContent).isEqualTo("Test invoice content for integration test");
    }

    @Test
    @DisplayName("Should create document version successfully")
    void testCreateDocumentVersion() throws Exception {
        // Given - Upload original document
        Document originalDoc = uploadTestDocument("original.pdf", "ORD-001");

        // When - Replace with new version
        MockMultipartFile newFile = new MockMultipartFile(
            "file",
            "updated.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Updated content v2".getBytes()
        );

        DocumentResponse newVersion = documentManagementService.replaceDocument(
            originalDoc.getId(),
            newFile,
            "test-user"
        );

        // Then
        assertThat(newVersion).isNotNull();
        assertThat(newVersion.getVersion()).isEqualTo(2);
        assertThat(newVersion.getIsLatestVersion()).isTrue();

        // Verify original is marked as not latest
        Document updatedOriginal = documentRepository.findById(originalDoc.getId()).orElseThrow();
        assertThat(updatedOriginal.getIsLatestVersion()).isFalse();
        assertThat(updatedOriginal.getReplacedByDocument()).isNotNull();
        assertThat(updatedOriginal.getReplacedByDocument().getId()).isEqualTo(newVersion.getId());

        // Verify new version links to parent
        Document newVersionDoc = documentRepository.findById(newVersion.getId()).orElseThrow();
        assertThat(newVersionDoc.getParentDocument()).isNotNull();
        assertThat(newVersionDoc.getParentDocument().getId()).isEqualTo(originalDoc.getId());
    }

    @Test
    @DisplayName("Should check order document completeness")
    void testCheckOrderDocumentCompleteness() throws Exception {
        // Given - Create mandatory document types
        DocumentType invoiceType = testDocumentType; // Already mandatory

        DocumentType contractType = DocumentType.builder()
            .typeCode("CONTRACT")
            .typeName("Contract")
            .category(DocumentCategory.LEGAL)
            .isActive(true)
            .isMandatory(true)
            .minDocuments(1)
            .build();
        contractType = documentTypeRepository.save(contractType);

        Long orderId = 1001L;

        // Upload invoice (complete)
        uploadTestDocumentWithType("invoice.pdf", "ORD-001", orderId, invoiceType);

        // Contract is missing

        // When
        DocumentCompletenessResult result = documentManagementService
            .checkOrderDocumentCompleteness(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getIsComplete()).isFalse();
        assertThat(result.getMissingDocumentTypes()).contains("CONTRACT");
        assertThat(result.getTotalRequiredTypes()).isEqualTo(2);
        assertThat(result.getSatisfiedTypes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should validate all order documents")
    void testValidateAllOrderDocuments() throws Exception {
        // Given
        Long orderId = 1001L;
        Document doc1 = uploadTestDocument("doc1.pdf", "ORD-001", orderId);
        Document doc2 = uploadTestDocument("doc2.pdf", "ORD-001", orderId);
        Document doc3 = uploadTestDocument("doc3.pdf", "ORD-001", orderId);

        // When
        ValidationSummary summary = documentManagementService
            .validateAllOrderDocuments(orderId, "validator@test.com");

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getOrderId()).isEqualTo(orderId);
        assertThat(summary.getTotalDocuments()).isEqualTo(3);
        assertThat(summary.getValidatedCount()).isEqualTo(3);
        assertThat(summary.getFailedCount()).isEqualTo(0);
        assertThat(summary.getAllValidated()).isTrue();

        // Verify all documents are validated in database
        List<Document> documents = documentRepository.findByOrderId(orderId);
        assertThat(documents).hasSize(3);
        assertThat(documents).allMatch(doc -> doc.getStatus() == DocumentStatus.VALIDATED);
    }

    @Test
    @DisplayName("Should download document from storage")
    void testDownloadDocument() throws Exception {
        // Given
        Document document = uploadTestDocument("download-test.pdf", "ORD-001");

        // When
        InputStream downloadStream = documentManagementService.downloadDocument(document.getId());

        // Then
        assertThat(downloadStream).isNotNull();
        String content = new String(downloadStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(content).contains("Test document content");
    }

    @Test
    @DisplayName("Should soft delete document")
    void testSoftDeleteDocument() throws Exception {
        // Given
        Document document = uploadTestDocument("to-delete.pdf", "ORD-001");

        // When
        documentManagementService.deleteDocument(document.getId(), "admin@test.com");

        // Then
        Document deleted = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getDeletedBy()).isEqualTo("admin@test.com");

        // Verify file still exists in storage (soft delete)
        boolean fileExists = minIOStorageService.fileExists(
            deleted.getStorageBucket(),
            deleted.getStoragePath()
        );
        assertThat(fileExists).isTrue();
    }

    @Test
    @DisplayName("Should archive document to archive bucket")
    void testArchiveDocument() throws Exception {
        // Given
        Document document = uploadTestDocument("to-archive.pdf", "ORD-001");

        // When
        DocumentResponse archived = documentManagementService.archiveDocument(
            document.getId(),
            "admin@test.com"
        );

        // Then
        assertThat(archived).isNotNull();
        assertThat(archived.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);

        // Verify database
        Document archivedDoc = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(archivedDoc.isArchived()).isTrue();

        // Verify file copied to archive bucket
        boolean fileExistsInArchive = minIOStorageService.fileExists(
            "documents-archive",
            archivedDoc.getStoragePath()
        );
        assertThat(fileExistsInArchive).isTrue();
    }

    @Test
    @DisplayName("Should update document metadata")
    void testUpdateDocumentMetadata() throws Exception {
        // Given
        Document document = uploadTestDocument("update-test.pdf", "ORD-001");

        DocumentUpdateRequest updateRequest = DocumentUpdateRequest.builder()
            .description("Updated description")
            .tags(List.of("updated", "new-tag"))
            .expirationDate(LocalDate.now().plusYears(1))
            .build();

        // When
        DocumentResponse updated = documentManagementService.updateDocument(
            document.getId(),
            updateRequest
        );

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getTags()).containsExactlyInAnyOrder("updated", "new-tag");
        assertThat(updated.getExpirationDate()).isEqualTo(LocalDate.now().plusYears(1));

        // Verify database
        Document updatedDoc = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(updatedDoc.getDescription()).isEqualTo("Updated description");
        assertThat(updatedDoc.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should get order documents")
    void testGetOrderDocuments() throws Exception {
        // Given
        Long orderId = 1001L;
        uploadTestDocument("doc1.pdf", "ORD-001", orderId);
        uploadTestDocument("doc2.pdf", "ORD-001", orderId);
        uploadTestDocument("doc3.pdf", "ORD-002", 1002L); // Different order

        // When
        List<DocumentResponse> documents = documentManagementService.getOrderDocuments(orderId);

        // Then
        assertThat(documents).hasSize(2);
        assertThat(documents).allMatch(doc -> doc.getOrderId().equals(orderId));
    }

    @Test
    @DisplayName("Should get order documents by type")
    void testGetOrderDocumentsByType() throws Exception {
        // Given
        Long orderId = 1001L;
        uploadTestDocumentWithType("invoice.pdf", "ORD-001", orderId, testDocumentType);

        // Create another type and upload
        DocumentType contractType = DocumentType.builder()
            .typeCode("CONTRACT")
            .typeName("Contract")
            .category(DocumentCategory.LEGAL)
            .isActive(true)
            .build();
        contractType = documentTypeRepository.save(contractType);

        uploadTestDocumentWithType("contract.pdf", "ORD-001", orderId, contractType);

        // When
        List<DocumentResponse> invoices = documentManagementService
            .getOrderDocumentsByType(orderId, "INVOICE");

        // Then
        assertThat(invoices).hasSize(1);
        assertThat(invoices.get(0).getTypeCode()).isEqualTo("INVOICE");
    }

    @Test
    @DisplayName("Should bulk upload multiple documents")
    void testBulkUploadDocuments() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "file1",
            "invoice1.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Invoice 1 content".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
            "file2",
            "invoice2.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Invoice 2 content".getBytes()
        );

        BulkUploadRequest request = BulkUploadRequest.builder()
            .files(List.of(file1, file2))
            .documentTypeId(testDocumentType.getId())
            .orderId(1001L)
            .orderNumber("ORD-2024-001")
            .continueOnError(true)
            .build();

        // When
        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalFiles()).isEqualTo(2);
        assertThat(result.getSuccessfulUploads()).isEqualTo(2);
        assertThat(result.getFailedUploads()).isEqualTo(0);
        assertThat(result.getOverallSuccess()).isTrue();
        assertThat(result.getResults()).hasSize(2);

        // Verify all files in database
        assertThat(documentRepository.count()).isEqualTo(2);

        // Verify all files in storage
        List<Document> documents = documentRepository.findAll();
        for (Document doc : documents) {
            boolean exists = minIOStorageService.fileExists(
                doc.getStorageBucket(),
                doc.getStoragePath()
            );
            assertThat(exists).isTrue();
        }
    }

    @Test
    @DisplayName("Should restore deleted document")
    void testRestoreDocument() throws Exception {
        // Given
        Document document = uploadTestDocument("restore-test.pdf", "ORD-001");
        documentManagementService.deleteDocument(document.getId(), "admin");

        // When
        DocumentResponse restored = documentManagementService.restoreDocument(
            document.getId(),
            "admin"
        );

        // Then
        assertThat(restored).isNotNull();
        assertThat(restored.getStatus()).isEqualTo(DocumentStatus.PENDING);

        Document restoredDoc = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(restoredDoc.isDeleted()).isFalse();
        assertThat(restoredDoc.getDeletedAt()).isNull();
        assertThat(restoredDoc.getDeletedBy()).isNull();
    }

    // Helper methods

    private Document uploadTestDocument(String filename, String orderNumber) throws Exception {
        return uploadTestDocument(filename, orderNumber, 1001L);
    }

    private Document uploadTestDocument(String filename, String orderNumber, Long orderId)
        throws Exception {
        return uploadTestDocumentWithType(filename, orderNumber, orderId, testDocumentType);
    }

    private Document uploadTestDocumentWithType(
        String filename,
        String orderNumber,
        Long orderId,
        DocumentType documentType
    ) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            filename,
            MediaType.APPLICATION_PDF_VALUE,
            "Test document content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest request = DocumentUploadRequest.builder()
            .file(file)
            .documentTypeId(documentType.getId())
            .orderId(orderId)
            .orderNumber(orderNumber)
            .build();

        DocumentResponse response = documentManagementService.uploadDocument(request);
        return documentRepository.findById(response.getId()).orElseThrow();
    }
}
