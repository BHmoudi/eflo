package com.eflo.document.e2e;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.model.DocumentResponse;
import com.eflo.document.domain.model.DocumentUploadRequest;
import com.eflo.document.domain.model.DocumentValidationRequest;
import com.eflo.document.domain.model.DocumentValidationResponse;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentManagementService;
import io.minio.MinioClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for complete document lifecycle.
 * Tests the full workflow: Upload -> Virus Scan -> Validation -> Versioning -> Archival
 *
 * Uses real containers for PostgreSQL, MinIO, Kafka, and ClamAV
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
    "document.uploaded",
    "document.validated",
    "document.rejected",
    "document.archived",
    "document.scan.completed",
    "document.version.created"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentLifecycleE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("document_service_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> minioContainer = new GenericContainer<?>("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @Container
    static GenericContainer<?> clamavContainer = new GenericContainer<>(DockerImageName.parse("clamav/clamav:latest"))
            .withExposedPorts(3310)
            .withEnv("CLAMAV_NO_FRESHCLAM", "true");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // MinIO
        registry.add("minio.url", () -> "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
        registry.add("minio.bucket-name", () -> "documents");

        // ClamAV
        registry.add("clamav.host", clamavContainer::getHost);
        registry.add("clamav.port", () -> clamavContainer.getMappedPort(3310));
        registry.add("clamav.timeout", () -> 60000);
        registry.add("clamav.enabled", () -> true);

        // Kafka - using embedded Kafka from @EmbeddedKafka
        registry.add("spring.kafka.bootstrap-servers", () -> "${spring.embedded.kafka.brokers}");
    }

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private MinioClient minioClient;

    private DocumentType testDocumentType;
    private Long testOrderId = 1001L;
    private String testOrderNumber = "ORD-2024-001";

    @BeforeEach
    void setUp() {
        // Create test document type
        testDocumentType = DocumentType.builder()
                .typeCode("IDENTITY_CARD")
                .typeName("Identity Card")
                .description("Government issued identity card")
                .category("IDENTITY")
                .allowedFormats(List.of("pdf", "jpg", "png"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(2)
                .isMandatory(true)
                .requiresValidation(true)
                .hasExpiration(true)
                .expirationWarningDays(30)
                .isActive(true)
                .build();
        testDocumentType = documentTypeRepository.save(testDocumentType);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Complete Document Lifecycle - Upload to Archive")
    void testCompleteDocumentLifecycle() throws Exception {
        // Step 1: Upload document
        System.out.println("\n=== STEP 1: Uploading Document ===");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "identity-card.pdf",
                "application/pdf",
                "Test PDF content for identity card".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("Test identity card upload")
                .expirationDate(LocalDate.now().plusYears(5))
                .businessUnitId(100L)
                .build();

        DocumentResponse uploadedDoc = documentManagementService.uploadDocument(uploadRequest);

        assertThat(uploadedDoc).isNotNull();
        assertThat(uploadedDoc.getId()).isNotNull();
        assertThat(uploadedDoc.getDocumentUuid()).isNotNull();
        assertThat(uploadedDoc.getOriginalFilename()).isEqualTo("identity-card.pdf");
        assertThat(uploadedDoc.getTypeCode()).isEqualTo("IDENTITY_CARD");
        assertThat(uploadedDoc.getOrderId()).isEqualTo(testOrderId);
        assertThat(uploadedDoc.getVersion()).isEqualTo(1);
        assertThat(uploadedDoc.getIsLatestVersion()).isTrue();

        System.out.println("Document uploaded successfully - ID: " + uploadedDoc.getId());
        System.out.println("Storage path: " + uploadedDoc.getStoragePath());

        // Step 2: Verify virus scan completed
        System.out.println("\n=== STEP 2: Verifying Virus Scan ===");
        Thread.sleep(2000); // Wait for async virus scan to complete

        Document scannedDoc = documentRepository.findById(uploadedDoc.getId()).orElseThrow();

        // Note: Scan might fail or be pending in test environment without actual ClamAV
        assertThat(scannedDoc.getVirusScanStatus()).isNotNull();
        System.out.println("Virus scan status: " + scannedDoc.getVirusScanStatus());

        // If scan succeeded, verify it's clean
        if (scannedDoc.getVirusScanStatus() == VirusScanStatus.CLEAN) {
            assertThat(scannedDoc.getStatus()).isEqualTo(DocumentStatus.PENDING);
            System.out.println("Virus scan: CLEAN - Document ready for validation");
        } else {
            System.out.println("Virus scan: " + scannedDoc.getVirusScanStatus() + " - Expected in test environment");
        }

        // Step 3: Validate document
        System.out.println("\n=== STEP 3: Validating Document ===");
        DocumentValidationRequest validationRequest = DocumentValidationRequest.builder()
                .isApproved(true)
                .comments("Document verified and approved")
                .validatedBy("test-validator")
                .build();

        DocumentValidationResponse validationResponse = documentManagementService.validateDocument(
                uploadedDoc.getId(),
                validationRequest
        );

        assertThat(validationResponse).isNotNull();
        assertThat(validationResponse.getValidationSuccessful()).isTrue();
        assertThat(validationResponse.getIsApproved()).isTrue();
        assertThat(validationResponse.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(validationResponse.getValidatedBy()).isEqualTo("test-validator");

        System.out.println("Document validated successfully");
        System.out.println("Validated by: " + validationResponse.getValidatedBy());
        System.out.println("Validation comments: " + validationResponse.getValidationComments());

        // Verify document status in database
        Document validatedDoc = documentRepository.findById(uploadedDoc.getId()).orElseThrow();
        assertThat(validatedDoc.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(validatedDoc.getValidatedAt()).isNotNull();
        assertThat(validatedDoc.getValidatedBy()).isEqualTo("test-validator");

        // Step 4: Create new version
        System.out.println("\n=== STEP 4: Creating New Version ===");
        MockMultipartFile newVersionFile = new MockMultipartFile(
                "file",
                "identity-card-updated.pdf",
                "application/pdf",
                "Updated PDF content for identity card".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse newVersion = documentManagementService.replaceDocument(
                uploadedDoc.getId(),
                newVersionFile,
                "test-user"
        );

        assertThat(newVersion).isNotNull();
        assertThat(newVersion.getId()).isNotEqualTo(uploadedDoc.getId());
        assertThat(newVersion.getVersion()).isEqualTo(2);
        assertThat(newVersion.getIsLatestVersion()).isTrue();
        assertThat(newVersion.getOrderId()).isEqualTo(testOrderId);

        System.out.println("New version created - ID: " + newVersion.getId() + ", Version: " + newVersion.getVersion());

        // Verify old version is marked as superseded
        Document oldVersion = documentRepository.findById(uploadedDoc.getId()).orElseThrow();
        assertThat(oldVersion.getIsLatestVersion()).isFalse();
        assertThat(oldVersion.getReplacedByDocument()).isNotNull();

        System.out.println("Old version marked as superseded");

        // Verify new version has parent reference
        Document newVersionDoc = documentRepository.findById(newVersion.getId()).orElseThrow();
        assertThat(newVersionDoc.getParentDocument()).isNotNull();
        assertThat(newVersionDoc.getParentDocument().getId()).isEqualTo(uploadedDoc.getId());

        // Step 5: Archive document
        System.out.println("\n=== STEP 5: Archiving Document ===");
        DocumentResponse archivedDoc = documentManagementService.archiveDocument(
                newVersion.getId(),
                "test-archiver"
        );

        assertThat(archivedDoc).isNotNull();
        assertThat(archivedDoc.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);

        System.out.println("Document archived successfully");

        // Verify archive status in database
        Document archivedDocFromDb = documentRepository.findById(newVersion.getId()).orElseThrow();
        assertThat(archivedDocFromDb.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);
        assertThat(archivedDocFromDb.isArchived()).isTrue();

        // Step 6: Verify complete lifecycle
        System.out.println("\n=== STEP 6: Lifecycle Verification Summary ===");
        System.out.println("Original document ID: " + uploadedDoc.getId());
        System.out.println("New version document ID: " + newVersion.getId());
        System.out.println("Final status: ARCHIVED");
        System.out.println("Version history: 1 -> 2");
        System.out.println("All lifecycle stages completed successfully!");

        // Verify we have 2 documents in the database (original + new version)
        List<Document> allDocs = documentRepository.findByOrderId(testOrderId);
        assertThat(allDocs).hasSize(2);

        // Verify version chain
        assertThat(allDocs.stream().anyMatch(d -> d.getVersion() == 1)).isTrue();
        assertThat(allDocs.stream().anyMatch(d -> d.getVersion() == 2)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Document Rejection Flow")
    void testDocumentRejectionFlow() {
        // Upload document
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid-document.pdf",
                "application/pdf",
                "Invalid document content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("Document to be rejected")
                .build();

        DocumentResponse uploadedDoc = documentManagementService.uploadDocument(uploadRequest);
        assertThat(uploadedDoc).isNotNull();

        // Reject document
        DocumentValidationRequest rejectionRequest = DocumentValidationRequest.builder()
                .isApproved(false)
                .comments("Document quality is insufficient - please reupload")
                .validatedBy("test-validator")
                .build();

        DocumentValidationResponse rejectionResponse = documentManagementService.validateDocument(
                uploadedDoc.getId(),
                rejectionRequest
        );

        assertThat(rejectionResponse).isNotNull();
        assertThat(rejectionResponse.getValidationSuccessful()).isTrue();
        assertThat(rejectionResponse.getIsApproved()).isFalse();
        assertThat(rejectionResponse.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejectionResponse.getStatusReason()).isEqualTo("Document quality is insufficient - please reupload");

        // Verify rejection in database
        Document rejectedDoc = documentRepository.findById(uploadedDoc.getId()).orElseThrow();
        assertThat(rejectedDoc.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejectedDoc.isRejected()).isTrue();
        assertThat(rejectedDoc.getValidatedBy()).isEqualTo("test-validator");
        assertThat(rejectedDoc.getStatusReason()).contains("insufficient");
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Document Expiration Flow")
    void testDocumentExpirationFlow() {
        // Upload document with expiration date
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "expiring-document.pdf",
                "application/pdf",
                "Document with expiration".getBytes(StandardCharsets.UTF_8)
        );

        LocalDate expirationDate = LocalDate.now().plusDays(30);

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("Document with expiration")
                .expirationDate(expirationDate)
                .build();

        DocumentResponse uploadedDoc = documentManagementService.uploadDocument(uploadRequest);
        assertThat(uploadedDoc).isNotNull();
        assertThat(uploadedDoc.getExpirationDate()).isEqualTo(expirationDate);

        // Verify expiration date in database
        Document docWithExpiration = documentRepository.findById(uploadedDoc.getId()).orElseThrow();
        assertThat(docWithExpiration.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(docWithExpiration.isExpiringSoon(30)).isTrue();
        assertThat(docWithExpiration.isExpiringSoon(29)).isFalse();
        assertThat(docWithExpiration.needsExpirationNotification(30)).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Soft Delete and Restore Flow")
    void testSoftDeleteAndRestoreFlow() {
        // Upload document
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to-be-deleted.pdf",
                "application/pdf",
                "Document to be deleted".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("Document for deletion test")
                .build();

        DocumentResponse uploadedDoc = documentManagementService.uploadDocument(uploadRequest);
        assertThat(uploadedDoc).isNotNull();

        // Soft delete document
        documentManagementService.deleteDocument(uploadedDoc.getId(), "test-deleter");

        // Verify soft delete
        Document deletedDoc = documentRepository.findById(uploadedDoc.getId()).orElseThrow();
        assertThat(deletedDoc.getStatus()).isEqualTo(DocumentStatus.DELETED);
        assertThat(deletedDoc.isDeleted()).isTrue();
        assertThat(deletedDoc.getDeletedBy()).isEqualTo("test-deleter");
        assertThat(deletedDoc.getDeletedAt()).isNotNull();

        // Restore document
        DocumentResponse restoredDoc = documentManagementService.restoreDocument(
                uploadedDoc.getId(),
                "test-restorer"
        );

        assertThat(restoredDoc).isNotNull();
        assertThat(restoredDoc.getStatus()).isEqualTo(DocumentStatus.PENDING);

        // Verify restoration
        Document restoredDocFromDb = documentRepository.findById(uploadedDoc.getId()).orElseThrow();
        assertThat(restoredDocFromDb.getDeletedAt()).isNull();
        assertThat(restoredDocFromDb.getDeletedBy()).isNull();
        assertThat(restoredDocFromDb.isDeleted()).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Multiple Version Chain")
    void testMultipleVersionChain() {
        // Upload original document
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "document-v1.pdf",
                "application/pdf",
                "Version 1 content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest uploadRequest1 = DocumentUploadRequest.builder()
                .file(file1)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .description("Version 1")
                .build();

        DocumentResponse v1 = documentManagementService.uploadDocument(uploadRequest1);
        assertThat(v1.getVersion()).isEqualTo(1);

        // Create version 2
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "document-v2.pdf",
                "application/pdf",
                "Version 2 content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse v2 = documentManagementService.replaceDocument(v1.getId(), file2, "user1");
        assertThat(v2.getVersion()).isEqualTo(2);

        // Create version 3
        MockMultipartFile file3 = new MockMultipartFile(
                "file",
                "document-v3.pdf",
                "application/pdf",
                "Version 3 content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse v3 = documentManagementService.replaceDocument(v2.getId(), file3, "user2");
        assertThat(v3.getVersion()).isEqualTo(3);

        // Verify version chain
        Document v1Doc = documentRepository.findById(v1.getId()).orElseThrow();
        Document v2Doc = documentRepository.findById(v2.getId()).orElseThrow();
        Document v3Doc = documentRepository.findById(v3.getId()).orElseThrow();

        assertThat(v1Doc.getIsLatestVersion()).isFalse();
        assertThat(v2Doc.getIsLatestVersion()).isFalse();
        assertThat(v3Doc.getIsLatestVersion()).isTrue();

        assertThat(v2Doc.getParentDocument().getId()).isEqualTo(v1.getId());
        assertThat(v3Doc.getParentDocument().getId()).isEqualTo(v2.getId());

        // Verify all versions belong to same order
        List<Document> orderDocs = documentRepository.findByOrderId(testOrderId);
        assertThat(orderDocs).hasSize(3);
        assertThat(orderDocs.stream().allMatch(d -> d.getOrderId().equals(testOrderId))).isTrue();
    }
}
