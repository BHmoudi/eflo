package com.eflo.document.integration;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.storage.MinIOStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DocumentController.
 * Tests complete upload flow: file → storage → DB → event
 *
 * @author Document Service
 * @version 1.0
 */
@EmbeddedKafka(partitions = 1, topics = {
    "document-uploaded",
    "document-validated",
    "document-rejected",
    "document-deleted",
    "document-scan-completed"
})
class DocumentControllerIntegrationTest extends BaseIntegrationTest {

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
    }

    @Test
    @DisplayName("Should upload document successfully and store in DB and MinIO")
    @WithMockUser(roles = "USER")
    void testUploadDocument() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-invoice.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Test PDF content".getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        MvcResult result = mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(file)
                .param("documentTypeId", testDocumentType.getId().toString())
                .param("orderId", "1001")
                .param("orderNumber", "ORD-2024-001")
                .param("description", "Test invoice upload")
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.documentUuid").exists())
            .andExpect(jsonPath("$.originalFilename").value("test-invoice.pdf"))
            .andExpect(jsonPath("$.typeCode").value("INVOICE"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        // Verify database state
        Document savedDocument = documentRepository.findAll().get(0);
        assertThat(savedDocument).isNotNull();
        assertThat(savedDocument.getOriginalFilename()).isEqualTo("test-invoice.pdf");
        assertThat(savedDocument.getOrderId()).isEqualTo(1001L);
        assertThat(savedDocument.getOrderNumber()).isEqualTo("ORD-2024-001");
        assertThat(savedDocument.getTypeCode()).isEqualTo("INVOICE");
        assertThat(savedDocument.getStatus()).isEqualTo(DocumentStatus.PENDING);

        // Verify MinIO storage
        boolean fileExists = minIOStorageService.fileExists(
            savedDocument.getStorageBucket(),
            savedDocument.getStoragePath()
        );
        assertThat(fileExists).isTrue();

        // Verify file content
        InputStream downloadedStream = minIOStorageService.downloadFile(
            savedDocument.getStorageBucket(),
            savedDocument.getStoragePath()
        );
        String content = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(content).contains("Test PDF content");
    }

    @Test
    @DisplayName("Should fail to upload document with invalid type")
    @WithMockUser(roles = "USER")
    void testUploadDocumentWithInvalidType() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Test content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(file)
                .param("documentTypeId", "99999")
                .param("orderId", "1001")
                .param("orderNumber", "ORD-2024-001")
                .with(csrf()))
            .andExpect(status().isBadRequest());

        // Verify no document was created
        assertThat(documentRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should download document successfully")
    @WithMockUser(roles = "USER")
    void testDownloadDocument() throws Exception {
        // Given - Create a document first
        Document document = createTestDocument();

        // When & Then
        mockMvc.perform(get("/api/v1/documents/{id}/download", document.getId()))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @DisplayName("Should get document by ID")
    @WithMockUser(roles = "USER")
    void testGetDocumentById() throws Exception {
        // Given
        Document document = createTestDocument();

        // When & Then
        mockMvc.perform(get("/api/v1/documents/{id}", document.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(document.getId()))
            .andExpect(jsonPath("$.originalFilename").value(document.getOriginalFilename()))
            .andExpect(jsonPath("$.typeCode").value("INVOICE"))
            .andExpect(jsonPath("$.orderNumber").value(document.getOrderNumber()));
    }

    @Test
    @DisplayName("Should delete document successfully (soft delete)")
    @WithMockUser(roles = "ADMIN")
    void testDeleteDocument() throws Exception {
        // Given
        Document document = createTestDocument();

        // When & Then
        mockMvc.perform(delete("/api/v1/documents/{id}", document.getId())
                .with(csrf()))
            .andExpect(status().isNoContent());

        // Verify soft delete
        Document deletedDocument = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(deletedDocument.isDeleted()).isTrue();
        assertThat(deletedDocument.getDeletedAt()).isNotNull();
        assertThat(deletedDocument.getDeletedBy()).isNotNull();
    }

    @Test
    @DisplayName("Should search documents with filters")
    @WithMockUser(roles = "USER")
    void testSearchDocuments() throws Exception {
        // Given - Create multiple documents
        createTestDocument("invoice1.pdf", "ORD-001");
        createTestDocument("invoice2.pdf", "ORD-001");
        createTestDocument("invoice3.pdf", "ORD-002");

        String searchRequest = """
            {
                "orderId": 1001,
                "typeCode": "INVOICE",
                "status": "PENDING"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(searchRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should get documents by order ID")
    @WithMockUser(roles = "USER")
    void testGetOrderDocuments() throws Exception {
        // Given
        Long orderId = 1001L;
        createTestDocument("doc1.pdf", "ORD-001", orderId);
        createTestDocument("doc2.pdf", "ORD-001", orderId);
        createTestDocument("doc3.pdf", "ORD-002", 1002L); // Different order

        // When & Then
        mockMvc.perform(get("/api/v1/documents/order/{orderId}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[*].orderId", everyItem(is(orderId.intValue()))));
    }

    @Test
    @DisplayName("Should get documents by order and type")
    @WithMockUser(roles = "USER")
    void testGetDocumentsByOrderAndType() throws Exception {
        // Given
        Long orderId = 1001L;
        createTestDocument("invoice.pdf", "ORD-001", orderId);

        // Create another document type
        DocumentType otherType = DocumentType.builder()
            .typeCode("CONTRACT")
            .typeName("Contract")
            .category(DocumentCategory.LEGAL)
            .isActive(true)
            .build();
        otherType = documentTypeRepository.save(otherType);

        createTestDocument("contract.pdf", "ORD-001", orderId, otherType);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/order/{orderId}/type/{typeCode}",
                orderId, "INVOICE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].typeCode").value("INVOICE"));
    }

    @Test
    @DisplayName("Should upload multiple documents in bulk")
    @WithMockUser(roles = "USER")
    void testBulkUpload() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "files",
            "invoice1.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
            "files",
            "invoice2.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Content 2".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/documents/upload-multiple")
                .file(file1)
                .file(file2)
                .param("documentTypeId", testDocumentType.getId().toString())
                .param("orderId", "1001")
                .param("orderNumber", "ORD-2024-001")
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.totalFiles").value(2))
            .andExpect(jsonPath("$.successfulUploads").value(2))
            .andExpect(jsonPath("$.failedUploads").value(0))
            .andExpect(jsonPath("$.results", hasSize(2)));

        // Verify database
        assertThat(documentRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should replace document with new version")
    @WithMockUser(roles = "USER")
    void testReplaceDocument() throws Exception {
        // Given
        Document oldDocument = createTestDocument();

        MockMultipartFile newFile = new MockMultipartFile(
            "file",
            "invoice-v2.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Updated content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/documents/{id}/replace", oldDocument.getId())
                .file(newFile)
                .param("reason", "Updated invoice")
                .with(csrf())
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(2));

        // Verify old document is not latest version
        Document updatedOldDoc = documentRepository.findById(oldDocument.getId()).orElseThrow();
        assertThat(updatedOldDoc.getIsLatestVersion()).isFalse();
        assertThat(updatedOldDoc.getReplacedByDocument()).isNotNull();
    }

    @Test
    @DisplayName("Should archive document")
    @WithMockUser(roles = "ADMIN")
    void testArchiveDocument() throws Exception {
        // Given
        Document document = createTestDocument();

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/archive", document.getId())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ARCHIVED"));

        // Verify archival
        Document archivedDoc = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(archivedDoc.isArchived()).isTrue();
    }

    @Test
    @DisplayName("Should get document versions")
    @WithMockUser(roles = "USER")
    void testGetDocumentVersions() throws Exception {
        // Given - Create versions
        Document v1 = createTestDocument();
        Document v2 = createVersion(v1);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/{id}/versions", v1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should get pending validation documents")
    @WithMockUser(roles = "ADMIN")
    void testGetPendingValidationDocuments() throws Exception {
        // Given
        createTestDocument("pending1.pdf", "ORD-001");
        createTestDocument("pending2.pdf", "ORD-002");

        // When & Then
        mockMvc.perform(get("/api/v1/documents/pending-validation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should get expiring documents")
    @WithMockUser(roles = "USER")
    void testGetExpiringDocuments() throws Exception {
        // Given - Create document with expiration date
        Document document = createTestDocument();
        document.setExpirationDate(LocalDate.now().plusDays(15));
        documentRepository.save(document);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/expiring")
                .param("days", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get document statistics")
    @WithMockUser(roles = "ADMIN")
    void testGetDocumentStatistics() throws Exception {
        // Given
        createTestDocument("doc1.pdf", "ORD-001");
        createTestDocument("doc2.pdf", "ORD-002");

        // When & Then
        mockMvc.perform(get("/api/v1/documents/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDocuments").value(greaterThanOrEqualTo(2)));
    }

    // Helper methods

    private Document createTestDocument() {
        return createTestDocument("test-invoice.pdf", "ORD-2024-001");
    }

    private Document createTestDocument(String filename, String orderNumber) {
        return createTestDocument(filename, orderNumber, 1001L);
    }

    private Document createTestDocument(String filename, String orderNumber, Long orderId) {
        return createTestDocument(filename, orderNumber, orderId, testDocumentType);
    }

    private Document createTestDocument(String filename, String orderNumber, Long orderId, DocumentType docType) {
        // Upload file to MinIO
        String storagePath = "orders/" + orderId + "/INVOICE/" + filename;
        minIOStorageService.createBucketIfNotExists("documents");
        minIOStorageService.uploadFile(
            "documents",
            storagePath,
            new java.io.ByteArrayInputStream("Test content".getBytes()),
            MediaType.APPLICATION_PDF_VALUE,
            12L
        );

        Document document = Document.builder()
            .documentUuid(UUID.randomUUID())
            .documentType(docType)
            .typeCode(docType.getTypeCode())
            .orderId(orderId)
            .orderNumber(orderNumber)
            .originalFilename(filename)
            .storedFilename(filename)
            .fileExtension("pdf")
            .mimeType(MediaType.APPLICATION_PDF_VALUE)
            .fileSizeBytes(12L)
            .fileHash("test-hash-" + UUID.randomUUID())
            .storageBucket("documents")
            .storagePath(storagePath)
            .version(1)
            .isLatestVersion(true)
            .status(DocumentStatus.PENDING)
            .uploadedBy("test-user")
            .build();

        return documentRepository.save(document);
    }

    private Document createVersion(Document parent) {
        Document newVersion = Document.builder()
            .documentUuid(UUID.randomUUID())
            .documentType(parent.getDocumentType())
            .typeCode(parent.getTypeCode())
            .orderId(parent.getOrderId())
            .orderNumber(parent.getOrderNumber())
            .originalFilename("updated-" + parent.getOriginalFilename())
            .storedFilename("updated-" + parent.getStoredFilename())
            .fileExtension("pdf")
            .mimeType(MediaType.APPLICATION_PDF_VALUE)
            .fileSizeBytes(20L)
            .fileHash("new-hash-" + UUID.randomUUID())
            .storageBucket("documents")
            .storagePath(parent.getStoragePath() + "-v2")
            .version(parent.getVersion() + 1)
            .isLatestVersion(true)
            .status(DocumentStatus.PENDING)
            .parentDocument(parent)
            .uploadedBy("test-user")
            .build();

        parent.setIsLatestVersion(false);
        parent.setReplacedByDocument(newVersion);
        documentRepository.save(parent);

        return documentRepository.save(newVersion);
    }
}
