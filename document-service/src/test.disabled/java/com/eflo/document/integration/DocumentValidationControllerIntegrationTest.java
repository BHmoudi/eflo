package com.eflo.document.integration;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DocumentValidationController.
 * Tests document validation and rejection workflows with real database and Kafka.
 *
 * @author Document Service
 * @version 1.0
 */
@EmbeddedKafka(partitions = 1, topics = {
    "document-validated",
    "document-rejected",
    "all-documents-validated"
})
class DocumentValidationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
            .typeCode("INVOICE")
            .typeName("Invoice")
            .description("Invoice document")
            .category(DocumentCategory.FINANCIAL)
            .isActive(true)
            .isMandatory(true)
            .requiresValidation(true)
            .build();
        testDocumentType = documentTypeRepository.save(testDocumentType);
    }

    @Test
    @DisplayName("Should validate document successfully")
    @WithMockUser(roles = "VALIDATOR")
    void testValidateDocument() throws Exception {
        // Given
        Document document = createPendingDocument();

        String validationRequest = """
            {
                "isApproved": true,
                "comments": "Document looks good",
                "validatedBy": "validator@test.com"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/validate", document.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validationRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentId").value(document.getId()))
            .andExpect(jsonPath("$.isApproved").value(true))
            .andExpect(jsonPath("$.status").value("VALIDATED"))
            .andExpect(jsonPath("$.validationSuccessful").value(true))
            .andExpect(jsonPath("$.validatedBy").value("validator@test.com"))
            .andExpect(jsonPath("$.validationComments").value("Document looks good"));

        // Verify database state
        Document validated = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(validated.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(validated.getValidatedBy()).isEqualTo("validator@test.com");
        assertThat(validated.getValidatedAt()).isNotNull();
        assertThat(validated.getValidationComments()).isEqualTo("Document looks good");
    }

    @Test
    @DisplayName("Should reject document successfully")
    @WithMockUser(roles = "VALIDATOR")
    void testRejectDocument() throws Exception {
        // Given
        Document document = createPendingDocument();

        String rejectionRequest = """
            {
                "isApproved": false,
                "comments": "Document is incomplete",
                "validatedBy": "validator@test.com"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/validate", document.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(rejectionRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentId").value(document.getId()))
            .andExpect(jsonPath("$.isApproved").value(false))
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.validationSuccessful").value(true))
            .andExpect(jsonPath("$.statusReason").value("Document is incomplete"));

        // Verify database state
        Document rejected = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(rejected.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejected.getValidatedBy()).isEqualTo("validator@test.com");
        assertThat(rejected.getValidatedAt()).isNotNull();
        assertThat(rejected.getValidationComments()).isEqualTo("Document is incomplete");
    }

    @Test
    @DisplayName("Should validate batch documents")
    @WithMockUser(roles = "VALIDATOR")
    void testBatchValidateDocuments() throws Exception {
        // Given
        Document doc1 = createPendingDocument();
        Document doc2 = createPendingDocument();
        Document doc3 = createPendingDocument();

        String batchRequest = String.format("""
            {
                "documentIds": [%d, %d, %d],
                "validatedBy": "validator@test.com",
                "comments": "Batch validation"
            }
            """, doc1.getId(), doc2.getId(), doc3.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/documents/validate-batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].validationSuccessful", everyItem(is(true))))
            .andExpect(jsonPath("$[*].isApproved", everyItem(is(true))));

        // Verify all documents are validated
        assertThat(documentRepository.findById(doc1.getId()).orElseThrow().getStatus())
            .isEqualTo(DocumentStatus.VALIDATED);
        assertThat(documentRepository.findById(doc2.getId()).orElseThrow().getStatus())
            .isEqualTo(DocumentStatus.VALIDATED);
        assertThat(documentRepository.findById(doc3.getId()).orElseThrow().getStatus())
            .isEqualTo(DocumentStatus.VALIDATED);
    }

    @Test
    @DisplayName("Should reject specific document with reason")
    @WithMockUser(roles = "VALIDATOR")
    void testRejectDocumentWithReason() throws Exception {
        // Given
        Document document = createPendingDocument();

        String rejectionRequest = """
            {
                "reason": "Missing signature",
                "rejectedBy": "validator@test.com"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/reject", document.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(rejectionRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isApproved").value(false))
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.statusReason").value("Missing signature"));

        // Verify database
        Document rejected = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(rejected.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejected.getValidationComments()).contains("Missing signature");
    }

    @Test
    @DisplayName("Should validate all documents for an order")
    @WithMockUser(roles = "VALIDATOR")
    void testValidateAllOrderDocuments() throws Exception {
        // Given
        Long orderId = 1001L;
        createPendingDocument(orderId, "ORD-001");
        createPendingDocument(orderId, "ORD-001");
        createPendingDocument(orderId, "ORD-001");

        String request = """
            {
                "validatedBy": "validator@test.com"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/order/{orderId}/validate-all", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.totalDocuments").value(3))
            .andExpect(jsonPath("$.validatedCount").value(3))
            .andExpect(jsonPath("$.failedCount").value(0))
            .andExpect(jsonPath("$.allValidated").value(true));

        // Verify all documents for order are validated
        long validatedCount = documentRepository.findByOrderId(orderId).stream()
            .filter(doc -> doc.getStatus() == DocumentStatus.VALIDATED)
            .count();
        assertThat(validatedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Should get validation status for document")
    @WithMockUser(roles = "USER")
    void testGetValidationStatus() throws Exception {
        // Given
        Document document = createPendingDocument();
        document.setStatus(DocumentStatus.VALIDATED);
        document.setValidatedBy("validator@test.com");
        documentRepository.save(document);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/{id}/validation-status", document.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentId").value(document.getId()))
            .andExpect(jsonPath("$.status").value("VALIDATED"))
            .andExpect(jsonPath("$.validatedBy").value("validator@test.com"));
    }

    @Test
    @DisplayName("Should get validation history for document")
    @WithMockUser(roles = "USER")
    void testGetValidationHistory() throws Exception {
        // Given
        Document document = createPendingDocument();

        // When & Then
        mockMvc.perform(get("/api/v1/documents/{id}/validation-history", document.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should require validator role for validation")
    @WithMockUser(roles = "USER")
    void testRequireValidatorRole() throws Exception {
        // Given
        Document document = createPendingDocument();

        String request = """
            {
                "isApproved": true,
                "validatedBy": "user@test.com"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/validate", document.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should not validate already validated document")
    @WithMockUser(roles = "VALIDATOR")
    void testCannotRevalidateDocument() throws Exception {
        // Given
        Document document = createPendingDocument();
        document.setStatus(DocumentStatus.VALIDATED);
        document.setValidatedBy("previous-validator");
        documentRepository.save(document);

        String request = """
            {
                "isApproved": true,
                "validatedBy": "new-validator@test.com"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/validate", document.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get pending validation documents")
    @WithMockUser(roles = "VALIDATOR")
    void testGetPendingValidationDocuments() throws Exception {
        // Given
        createPendingDocument();
        createPendingDocument();

        Document validated = createPendingDocument();
        validated.setStatus(DocumentStatus.VALIDATED);
        documentRepository.save(validated);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/validation/pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Should get validation statistics")
    @WithMockUser(roles = "ADMIN")
    void testGetValidationStatistics() throws Exception {
        // Given
        createPendingDocument();

        Document validated = createPendingDocument();
        validated.setStatus(DocumentStatus.VALIDATED);
        documentRepository.save(validated);

        Document rejected = createPendingDocument();
        rejected.setStatus(DocumentStatus.REJECTED);
        documentRepository.save(rejected);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/validation/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDocuments").value(3))
            .andExpect(jsonPath("$.pendingCount").value(1))
            .andExpect(jsonPath("$.validatedCount").value(1))
            .andExpect(jsonPath("$.rejectedCount").value(1));
    }

    @Test
    @DisplayName("Should check order validation completeness")
    @WithMockUser(roles = "USER")
    void testCheckOrderValidationCompleteness() throws Exception {
        // Given
        Long orderId = 1001L;

        Document doc1 = createPendingDocument(orderId, "ORD-001");
        doc1.setStatus(DocumentStatus.VALIDATED);
        documentRepository.save(doc1);

        Document doc2 = createPendingDocument(orderId, "ORD-001");
        doc2.setStatus(DocumentStatus.VALIDATED);
        documentRepository.save(doc2);

        createPendingDocument(orderId, "ORD-001"); // Still pending

        // When & Then
        mockMvc.perform(get("/api/v1/documents/order/{orderId}/validation-completeness", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.totalDocuments").value(3))
            .andExpect(jsonPath("$.validatedDocuments").value(2))
            .andExpect(jsonPath("$.pendingDocuments").value(1))
            .andExpect(jsonPath("$.isComplete").value(false));
    }

    @Test
    @DisplayName("Should re-validate rejected document")
    @WithMockUser(roles = "VALIDATOR")
    void testRevalidateRejectedDocument() throws Exception {
        // Given
        Document document = createPendingDocument();
        document.setStatus(DocumentStatus.REJECTED);
        document.setValidatedBy("previous-validator");
        documentRepository.save(document);

        String request = """
            {
                "isApproved": true,
                "validatedBy": "new-validator@test.com",
                "comments": "Fixed and re-validated"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/documents/{id}/revalidate", document.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("VALIDATED"))
            .andExpect(jsonPath("$.validationComments").value("Fixed and re-validated"));

        // Verify status changed
        Document revalidated = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(revalidated.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
    }

    // Helper methods

    private Document createPendingDocument() {
        return createPendingDocument(1001L, "ORD-2024-001");
    }

    private Document createPendingDocument(Long orderId, String orderNumber) {
        Document document = Document.builder()
            .documentUuid(UUID.randomUUID())
            .documentType(testDocumentType)
            .typeCode(testDocumentType.getTypeCode())
            .orderId(orderId)
            .orderNumber(orderNumber)
            .originalFilename("test-document.pdf")
            .storedFilename("stored-" + UUID.randomUUID() + ".pdf")
            .fileExtension("pdf")
            .mimeType("application/pdf")
            .fileSizeBytes(1000L)
            .fileHash("hash-" + UUID.randomUUID())
            .storageBucket("documents")
            .storagePath("orders/" + orderId + "/INVOICE/test.pdf")
            .version(1)
            .isLatestVersion(true)
            .status(DocumentStatus.PENDING)
            .uploadedBy("test-user")
            .build();

        return documentRepository.save(document);
    }
}
