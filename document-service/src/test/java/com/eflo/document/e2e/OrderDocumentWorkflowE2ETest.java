package com.eflo.document.e2e;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.*;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentManagementService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for complete order document workflow.
 * Tests order creation, uploading all mandatory documents, validation, and completeness checking.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
    "document.uploaded",
    "document.validated",
    "all.documents.validated"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderDocumentWorkflowE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("document_service_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> minioContainer = new GenericContainer<?>("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("minio.url", () -> "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
        registry.add("minio.bucket-name", () -> "documents");

        registry.add("spring.kafka.bootstrap-servers", () -> "${spring.embedded.kafka.brokers}");
    }

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private Long testOrderId = 2001L;
    private String testOrderNumber = "ORD-2024-WORKFLOW-001";

    private DocumentType identityCardType;
    private DocumentType proofOfAddressType;
    private DocumentType drivingLicenseType;

    @BeforeEach
    void setUp() {
        // Create mandatory document types for order
        identityCardType = createDocumentType(
                "IDENTITY_CARD",
                "Identity Card",
                "Government issued identity card",
                true,
                1,
                1
        );

        proofOfAddressType = createDocumentType(
                "PROOF_OF_ADDRESS",
                "Proof of Address",
                "Recent utility bill or bank statement",
                true,
                1,
                2
        );

        drivingLicenseType = createDocumentType(
                "DRIVING_LICENSE",
                "Driving License",
                "Valid driving license",
                false,
                0,
                1
        );
    }

    @AfterEach
    void tearDown() {
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Complete Order Document Workflow - All Mandatory Documents")
    void testCompleteOrderDocumentWorkflow() {
        System.out.println("\n=== ORDER DOCUMENT WORKFLOW TEST ===");
        System.out.println("Order ID: " + testOrderId);
        System.out.println("Order Number: " + testOrderNumber);

        // Step 1: Simulate order creation (in actual system, this would come from Order Service)
        System.out.println("\n--- Step 1: Order Created ---");
        System.out.println("Required mandatory documents:");
        System.out.println("  1. Identity Card (1 required)");
        System.out.println("  2. Proof of Address (1-2 required)");
        System.out.println("Optional documents:");
        System.out.println("  3. Driving License (optional)");

        // Step 2: Upload Identity Card
        System.out.println("\n--- Step 2: Uploading Identity Card ---");
        MockMultipartFile identityCard = new MockMultipartFile(
                "file",
                "identity-card.pdf",
                "application/pdf",
                "Identity card PDF content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest identityUploadRequest = DocumentUploadRequest.builder()
                .file(identityCard)
                .documentTypeId(identityCardType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("National Identity Card")
                .expirationDate(LocalDate.now().plusYears(10))
                .build();

        DocumentResponse identityDoc = documentManagementService.uploadDocument(identityUploadRequest);
        assertThat(identityDoc).isNotNull();
        assertThat(identityDoc.getTypeCode()).isEqualTo("IDENTITY_CARD");
        assertThat(identityDoc.getStatus()).isEqualTo(DocumentStatus.PENDING);

        System.out.println("Identity Card uploaded - ID: " + identityDoc.getId());

        // Step 3: Upload Proof of Address
        System.out.println("\n--- Step 3: Uploading Proof of Address ---");
        MockMultipartFile proofOfAddress = new MockMultipartFile(
                "file",
                "utility-bill.pdf",
                "application/pdf",
                "Utility bill PDF content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest addressUploadRequest = DocumentUploadRequest.builder()
                .file(proofOfAddress)
                .documentTypeId(proofOfAddressType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("Electricity bill dated last month")
                .build();

        DocumentResponse addressDoc = documentManagementService.uploadDocument(addressUploadRequest);
        assertThat(addressDoc).isNotNull();
        assertThat(addressDoc.getTypeCode()).isEqualTo("PROOF_OF_ADDRESS");

        System.out.println("Proof of Address uploaded - ID: " + addressDoc.getId());

        // Step 4: Check order completeness before validation
        System.out.println("\n--- Step 4: Checking Order Completeness (Before Validation) ---");
        DocumentCompletenessResult completenessBeforeValidation =
                documentManagementService.checkOrderDocumentCompleteness(testOrderId);

        System.out.println("Completeness Status: " + completenessBeforeValidation.getIsComplete());
        System.out.println("Total Required Types: " + completenessBeforeValidation.getTotalRequiredTypes());
        System.out.println("Satisfied Types: " + completenessBeforeValidation.getSatisfiedTypes());
        System.out.println("Total Documents: " + completenessBeforeValidation.getTotalDocuments());
        System.out.println("Pending Documents: " + completenessBeforeValidation.getPendingDocuments());

        // Should not be complete because documents are not validated yet
        assertThat(completenessBeforeValidation.getIsComplete()).isFalse();
        assertThat(completenessBeforeValidation.getTotalDocuments()).isEqualTo(2);
        assertThat(completenessBeforeValidation.getPendingDocuments()).isEqualTo(2);
        assertThat(completenessBeforeValidation.getValidatedDocuments()).isEqualTo(0);

        // Step 5: Validate Identity Card
        System.out.println("\n--- Step 5: Validating Identity Card ---");
        DocumentValidationRequest identityValidation = DocumentValidationRequest.builder()
                .isApproved(true)
                .comments("Identity card verified and approved")
                .validatedBy("validator-user-1")
                .build();

        DocumentValidationResponse identityValidationResult =
                documentManagementService.validateDocument(identityDoc.getId(), identityValidation);

        assertThat(identityValidationResult.getValidationSuccessful()).isTrue();
        assertThat(identityValidationResult.getIsApproved()).isTrue();
        assertThat(identityValidationResult.getStatus()).isEqualTo(DocumentStatus.VALIDATED);

        System.out.println("Identity Card validated successfully");

        // Step 6: Validate Proof of Address
        System.out.println("\n--- Step 6: Validating Proof of Address ---");
        DocumentValidationRequest addressValidation = DocumentValidationRequest.builder()
                .isApproved(true)
                .comments("Proof of address verified and approved")
                .validatedBy("validator-user-1")
                .build();

        DocumentValidationResponse addressValidationResult =
                documentManagementService.validateDocument(addressDoc.getId(), addressValidation);

        assertThat(addressValidationResult.getValidationSuccessful()).isTrue();
        assertThat(addressValidationResult.getIsApproved()).isTrue();

        System.out.println("Proof of Address validated successfully");

        // Step 7: Check order completeness after validation
        System.out.println("\n--- Step 7: Checking Order Completeness (After Validation) ---");
        DocumentCompletenessResult completenessAfterValidation =
                documentManagementService.checkOrderDocumentCompleteness(testOrderId);

        System.out.println("Completeness Status: " + completenessAfterValidation.getIsComplete());
        System.out.println("Total Required Types: " + completenessAfterValidation.getTotalRequiredTypes());
        System.out.println("Satisfied Types: " + completenessAfterValidation.getSatisfiedTypes());
        System.out.println("Validated Documents: " + completenessAfterValidation.getValidatedDocuments());
        System.out.println("Missing Document Types: " + completenessAfterValidation.getMissingDocumentTypes());
        System.out.println("Incomplete Document Types: " + completenessAfterValidation.getIncompleteDocumentTypes());

        // Should be complete now
        assertThat(completenessAfterValidation.getIsComplete()).isTrue();
        assertThat(completenessAfterValidation.getValidatedDocuments()).isEqualTo(2);
        assertThat(completenessAfterValidation.getMissingDocumentTypes()).isEmpty();
        assertThat(completenessAfterValidation.getIncompleteDocumentTypes()).isEmpty();

        // Step 8: Verify workflow events would trigger next steps
        System.out.println("\n--- Step 8: Workflow Events Summary ---");
        System.out.println("Events that should have been published:");
        System.out.println("  1. document.uploaded (Identity Card)");
        System.out.println("  2. document.uploaded (Proof of Address)");
        System.out.println("  3. document.validated (Identity Card)");
        System.out.println("  4. document.validated (Proof of Address)");
        System.out.println("  5. all.documents.validated (Order level)");
        System.out.println("\nOrder is now ready for next workflow step!");

        // Verify all documents in final state
        List<DocumentResponse> orderDocuments = documentManagementService.getOrderDocuments(testOrderId);
        assertThat(orderDocuments).hasSize(2);
        assertThat(orderDocuments.stream().allMatch(d -> d.getStatus() == DocumentStatus.VALIDATED)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Order Workflow with Rejected Document")
    void testOrderWorkflowWithRejection() {
        System.out.println("\n=== ORDER WORKFLOW WITH REJECTION TEST ===");

        // Upload Identity Card
        MockMultipartFile identityCard = new MockMultipartFile(
                "file",
                "identity-card.pdf",
                "application/pdf",
                "Identity card content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest identityUpload = DocumentUploadRequest.builder()
                .file(identityCard)
                .documentTypeId(identityCardType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .build();

        DocumentResponse identityDoc = documentManagementService.uploadDocument(identityUpload);

        // Upload Proof of Address (will be rejected)
        MockMultipartFile proofOfAddress = new MockMultipartFile(
                "file",
                "poor-quality-bill.pdf",
                "application/pdf",
                "Poor quality bill".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest addressUpload = DocumentUploadRequest.builder()
                .file(proofOfAddress)
                .documentTypeId(proofOfAddressType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .build();

        DocumentResponse addressDoc = documentManagementService.uploadDocument(addressUpload);

        // Validate Identity Card
        documentManagementService.validateDocument(
                identityDoc.getId(),
                DocumentValidationRequest.builder()
                        .isApproved(true)
                        .comments("Approved")
                        .validatedBy("validator")
                        .build()
        );

        // Reject Proof of Address
        System.out.println("\n--- Rejecting Proof of Address ---");
        DocumentValidationResponse rejectionResponse = documentManagementService.validateDocument(
                addressDoc.getId(),
                DocumentValidationRequest.builder()
                        .isApproved(false)
                        .comments("Document is not readable - please upload a clearer copy")
                        .validatedBy("validator")
                        .build()
        );

        assertThat(rejectionResponse.getIsApproved()).isFalse();
        assertThat(rejectionResponse.getStatus()).isEqualTo(DocumentStatus.REJECTED);

        System.out.println("Document rejected: " + rejectionResponse.getStatusReason());

        // Check completeness - should not be complete due to rejection
        DocumentCompletenessResult completeness =
                documentManagementService.checkOrderDocumentCompleteness(testOrderId);

        System.out.println("\n--- Completeness Check ---");
        System.out.println("Is Complete: " + completeness.getIsComplete());
        System.out.println("Rejected Documents: " + completeness.getRejectedDocuments());

        assertThat(completeness.getIsComplete()).isFalse();
        assertThat(completeness.getRejectedDocuments()).isEqualTo(1);
        assertThat(completeness.getValidatedDocuments()).isEqualTo(1);

        System.out.println("\nOrder cannot proceed - requires document reupload");
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Order Workflow with Multiple Proof of Address Documents")
    void testOrderWorkflowWithMultipleProofOfAddress() {
        System.out.println("\n=== ORDER WORKFLOW WITH MULTIPLE PROOF OF ADDRESS ===");

        // Upload Identity Card
        MockMultipartFile identityCard = new MockMultipartFile(
                "file",
                "identity-card.pdf",
                "application/pdf",
                "Identity card content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse identityDoc = documentManagementService.uploadDocument(
                DocumentUploadRequest.builder()
                        .file(identityCard)
                        .documentTypeId(identityCardType.getId())
                        .orderId(testOrderId)
                        .orderNumber(testOrderNumber)
                        .build()
        );

        // Upload first Proof of Address (Utility Bill)
        MockMultipartFile utilityBill = new MockMultipartFile(
                "file",
                "utility-bill.pdf",
                "application/pdf",
                "Utility bill content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse utilityDoc = documentManagementService.uploadDocument(
                DocumentUploadRequest.builder()
                        .file(utilityBill)
                        .documentTypeId(proofOfAddressType.getId())
                        .orderId(testOrderId)
                        .orderNumber(testOrderNumber)
                        .description("Electricity bill")
                        .build()
        );

        // Upload second Proof of Address (Bank Statement)
        MockMultipartFile bankStatement = new MockMultipartFile(
                "file",
                "bank-statement.pdf",
                "application/pdf",
                "Bank statement content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse bankDoc = documentManagementService.uploadDocument(
                DocumentUploadRequest.builder()
                        .file(bankStatement)
                        .documentTypeId(proofOfAddressType.getId())
                        .orderId(testOrderId)
                        .orderNumber(testOrderNumber)
                        .description("Bank statement")
                        .build()
        );

        System.out.println("\nUploaded 3 documents:");
        System.out.println("  1. Identity Card");
        System.out.println("  2. Utility Bill (Proof of Address)");
        System.out.println("  3. Bank Statement (Proof of Address)");

        // Validate all documents
        documentManagementService.validateDocument(identityDoc.getId(),
                DocumentValidationRequest.builder().isApproved(true).validatedBy("validator").build());
        documentManagementService.validateDocument(utilityDoc.getId(),
                DocumentValidationRequest.builder().isApproved(true).validatedBy("validator").build());
        documentManagementService.validateDocument(bankDoc.getId(),
                DocumentValidationRequest.builder().isApproved(true).validatedBy("validator").build());

        // Check completeness
        DocumentCompletenessResult completeness =
                documentManagementService.checkOrderDocumentCompleteness(testOrderId);

        System.out.println("\n--- Completeness Check ---");
        System.out.println("Is Complete: " + completeness.getIsComplete());
        System.out.println("Total Documents: " + completeness.getTotalDocuments());
        System.out.println("Validated Documents: " + completeness.getValidatedDocuments());

        assertThat(completeness.getIsComplete()).isTrue();
        assertThat(completeness.getTotalDocuments()).isEqualTo(3);
        assertThat(completeness.getValidatedDocuments()).isEqualTo(3);

        // Verify Proof of Address count
        List<DocumentResponse> proofOfAddressDocs =
                documentManagementService.getOrderDocumentsByType(testOrderId, "PROOF_OF_ADDRESS");
        assertThat(proofOfAddressDocs).hasSize(2);
        assertThat(proofOfAddressDocs.stream()
                .allMatch(d -> d.getStatus() == DocumentStatus.VALIDATED)).isTrue();

        System.out.println("\nOrder complete with 2 Proof of Address documents!");
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Batch Validate All Order Documents")
    void testBatchValidateAllOrderDocuments() {
        System.out.println("\n=== BATCH VALIDATION TEST ===");

        // Upload multiple documents
        List<Long> documentIds = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document-" + i + ".pdf",
                    "application/pdf",
                    ("Document " + i + " content").getBytes(StandardCharsets.UTF_8)
            );

            DocumentType type = i == 1 ? identityCardType : proofOfAddressType;

            DocumentResponse doc = documentManagementService.uploadDocument(
                    DocumentUploadRequest.builder()
                            .file(file)
                            .documentTypeId(type.getId())
                            .orderId(testOrderId)
                            .orderNumber(testOrderNumber)
                            .build()
            );

            documentIds.add(doc.getId());
        }

        System.out.println("Uploaded " + documentIds.size() + " documents");

        // Batch validate all documents for the order
        System.out.println("\n--- Batch Validating All Documents ---");
        ValidationSummary validationSummary =
                documentManagementService.validateAllOrderDocuments(testOrderId, "batch-validator");

        System.out.println("Validation Summary:");
        System.out.println("  Total Documents: " + validationSummary.getTotalDocuments());
        System.out.println("  Validated Count: " + validationSummary.getValidatedCount());
        System.out.println("  Failed Count: " + validationSummary.getFailedCount());
        System.out.println("  All Validated: " + validationSummary.getAllValidated());

        assertThat(validationSummary.getAllValidated()).isTrue();
        assertThat(validationSummary.getValidatedCount()).isEqualTo(3);
        assertThat(validationSummary.getFailedCount()).isEqualTo(0);

        // Verify all documents are validated
        List<DocumentResponse> orderDocs = documentManagementService.getOrderDocuments(testOrderId);
        assertThat(orderDocs.stream()
                .allMatch(d -> d.getStatus() == DocumentStatus.VALIDATED)).isTrue();

        System.out.println("\nAll documents validated in batch successfully!");
    }

    // Helper method to create document types
    private DocumentType createDocumentType(String code, String name, String description,
                                           boolean mandatory, int minDocs, int maxDocs) {
        DocumentType type = DocumentType.builder()
                .typeCode(code)
                .typeName(name)
                .description(description)
                .category("IDENTITY")
                .allowedFormats(List.of("pdf", "jpg", "png"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(minDocs)
                .maxDocuments(maxDocs)
                .isMandatory(mandatory)
                .requiresValidation(true)
                .hasExpiration(false)
                .isActive(true)
                .build();
        return documentTypeRepository.save(type);
    }
}
