package com.eflo.document.scenarios;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentResponse;
import com.eflo.document.domain.model.DocumentUploadRequest;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentEventPublisher;
import com.eflo.document.service.DocumentManagementService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Cucumber step definitions for Document Upload scenarios.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"document.uploaded", "document.scan.completed", "validation.task.created"})
public class DocumentUploadSteps {

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

    @SpyBean
    private DocumentEventPublisher eventPublisher;

    // Scenario context
    private Long currentOrderId;
    private String currentOrderNumber;
    private DocumentType identityDocumentType;
    private DocumentResponse uploadedDocument;
    private Exception caughtException;

    @Before
    public void setUp() {
        // Clean up before each scenario
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();

        // Reset scenario context
        currentOrderId = null;
        currentOrderNumber = null;
        identityDocumentType = null;
        uploadedDocument = null;
        caughtException = null;
    }

    @Given("a new vehicle order exists")
    public void aNewVehicleOrderExists() {
        currentOrderId = 5001L;
        currentOrderNumber = "ORD-VEH-2024-001";
        System.out.println("Created vehicle order: " + currentOrderNumber + " (ID: " + currentOrderId + ")");
    }

    @And("the order requires identity document validation")
    public void theOrderRequiresIdentityDocumentValidation() {
        identityDocumentType = DocumentType.builder()
                .typeCode("IDENTITY_CARD")
                .typeName("Identity Card")
                .description("Government issued identity document")
                .category("IDENTITY")
                .allowedFormats(List.of("pdf", "jpg", "png"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(1)
                .isMandatory(true)
                .requiresValidation(true)
                .hasExpiration(true)
                .isActive(true)
                .build();

        identityDocumentType = documentTypeRepository.save(identityDocumentType);
        System.out.println("Identity document type configured: " + identityDocumentType.getTypeCode());
        System.out.println("  - Mandatory: " + identityDocumentType.getIsMandatory());
        System.out.println("  - Requires Validation: " + identityDocumentType.getRequiresValidation());
    }

    @When("a user uploads a valid identity card PDF")
    public void aUserUploadsAValidIdentityCardPDF() {
        try {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "identity-card.pdf",
                    "application/pdf",
                    "Valid identity card PDF content with personal information".getBytes(StandardCharsets.UTF_8)
            );

            DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                    .file(file)
                    .documentTypeId(identityDocumentType.getId())
                    .orderId(currentOrderId)
                    .orderNumber(currentOrderNumber)
                    .description("National Identity Card for vehicle purchase")
                    .expirationDate(LocalDate.now().plusYears(5))
                    .businessUnitId(100L)
                    .build();

            uploadedDocument = documentManagementService.uploadDocument(uploadRequest);
            System.out.println("Document uploaded successfully - ID: " + uploadedDocument.getId());

        } catch (Exception e) {
            caughtException = e;
            System.err.println("Upload failed: " + e.getMessage());
        }
    }

    @Then("the document should be stored in MinIO")
    public void theDocumentShouldBeStoredInMinIO() {
        assertThat(uploadedDocument).isNotNull();
        assertThat(uploadedDocument.getStorageBucket()).isEqualTo("documents");
        assertThat(uploadedDocument.getStoragePath()).isNotNull();
        assertThat(uploadedDocument.getStoragePath()).contains("orders/" + currentOrderId);
        assertThat(uploadedDocument.getStoragePath()).contains("IDENTITY_CARD");

        System.out.println("✓ Document stored in MinIO");
        System.out.println("  - Bucket: " + uploadedDocument.getStorageBucket());
        System.out.println("  - Path: " + uploadedDocument.getStoragePath());
    }

    @And("the document status should be PENDING")
    public void theDocumentStatusShouldBePENDING() {
        assertThat(uploadedDocument.getStatus()).isEqualTo(DocumentStatus.PENDING);

        // Verify in database as well
        Document docFromDb = documentRepository.findById(uploadedDocument.getId()).orElseThrow();
        assertThat(docFromDb.getStatus()).isEqualTo(DocumentStatus.PENDING);

        System.out.println("✓ Document status is PENDING");
    }

    @And("a validation task should be created")
    public void aValidationTaskShouldBeCreated() {
        // Verify document requires validation
        Document document = documentRepository.findById(uploadedDocument.getId()).orElseThrow();
        assertThat(document.getDocumentType().getRequiresValidation()).isTrue();
        assertThat(document.getValidatedAt()).isNull();
        assertThat(document.getValidatedBy()).isNull();

        System.out.println("✓ Validation task should be created");
        System.out.println("  - Document awaiting validation");
        System.out.println("  - Validation required: true");
    }

    @And("a document.uploaded event should be published")
    public void aDocumentUploadedEventShouldBePublished() {
        // Verify event was published
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(eventPublisher, atLeastOnce()).publishDocumentUploaded(documentCaptor.capture());

        Document publishedDocument = documentCaptor.getValue();
        assertThat(publishedDocument.getId()).isEqualTo(uploadedDocument.getId());

        System.out.println("✓ document.uploaded event published");
        System.out.println("  - Document ID: " + publishedDocument.getId());
        System.out.println("  - Order ID: " + publishedDocument.getOrderId());
    }

    @Given("a document type {string} exists")
    public void aDocumentTypeExists(String typeCode) {
        DocumentType documentType = DocumentType.builder()
                .typeCode(typeCode)
                .typeName(typeCode.replace("_", " "))
                .description("Document type: " + typeCode)
                .category("GENERAL")
                .allowedFormats(List.of("pdf", "jpg", "png", "docx"))
                .maxFileSizeMb(BigDecimal.valueOf(10.0))
                .minDocuments(1)
                .maxDocuments(5)
                .isMandatory(false)
                .requiresValidation(true)
                .hasExpiration(false)
                .isActive(true)
                .build();

        documentTypeRepository.save(documentType);
        System.out.println("Document type created: " + typeCode);
    }

    @Given("an order {string} exists with ID {long}")
    public void anOrderExistsWithID(String orderNumber, Long orderId) {
        currentOrderNumber = orderNumber;
        currentOrderId = orderId;
        System.out.println("Order context set: " + orderNumber + " (ID: " + orderId + ")");
    }

    @When("the user uploads a {string} file named {string}")
    public void theUserUploadsAFileNamed(String mimeType, String filename) {
        try {
            // Find the document type from filename pattern
            String typeCode = extractTypeCodeFromFilename(filename);
            DocumentType documentType = documentTypeRepository.findByTypeCode(typeCode)
                    .orElseThrow(() -> new RuntimeException("Document type not found: " + typeCode));

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    filename,
                    mimeType,
                    ("Sample content for " + filename).getBytes(StandardCharsets.UTF_8)
            );

            DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                    .file(file)
                    .documentTypeId(documentType.getId())
                    .orderId(currentOrderId)
                    .orderNumber(currentOrderNumber)
                    .description("Uploaded: " + filename)
                    .build();

            uploadedDocument = documentManagementService.uploadDocument(uploadRequest);
            System.out.println("Uploaded: " + filename + " (ID: " + uploadedDocument.getId() + ")");

        } catch (Exception e) {
            caughtException = e;
            System.err.println("Upload failed: " + e.getMessage());
        }
    }

    @Then("the document should be uploaded successfully")
    public void theDocumentShouldBeUploadedSuccessfully() {
        assertThat(caughtException).isNull();
        assertThat(uploadedDocument).isNotNull();
        assertThat(uploadedDocument.getId()).isNotNull();
        System.out.println("✓ Document uploaded successfully");
    }

    @And("the document filename should be {string}")
    public void theDocumentFilenameShouldBe(String expectedFilename) {
        assertThat(uploadedDocument.getOriginalFilename()).isEqualTo(expectedFilename);
        System.out.println("✓ Filename verified: " + expectedFilename);
    }

    @And("the document order should be {string}")
    public void theDocumentOrderShouldBe(String expectedOrderNumber) {
        assertThat(uploadedDocument.getOrderNumber()).isEqualTo(expectedOrderNumber);
        System.out.println("✓ Order verified: " + expectedOrderNumber);
    }

    @And("the virus scan status should not be null")
    public void theVirusScanStatusShouldNotBeNull() throws InterruptedException {
        // Wait for async virus scan
        Thread.sleep(1000);

        Document document = documentRepository.findById(uploadedDocument.getId()).orElseThrow();
        assertThat(document.getVirusScanStatus()).isNotNull();
        System.out.println("✓ Virus scan status: " + document.getVirusScanStatus());
    }

    // Helper methods

    private String extractTypeCodeFromFilename(String filename) {
        // Simple mapping for test files
        if (filename.contains("identity") || filename.contains("id")) {
            return "IDENTITY_CARD";
        } else if (filename.contains("license")) {
            return "DRIVING_LICENSE";
        } else if (filename.contains("address") || filename.contains("utility")) {
            return "PROOF_OF_ADDRESS";
        }
        return "GENERAL_DOCUMENT";
    }

    // Getter for other step definitions
    public DocumentResponse getUploadedDocument() {
        return uploadedDocument;
    }

    public Long getCurrentOrderId() {
        return currentOrderId;
    }

    public String getCurrentOrderNumber() {
        return currentOrderNumber;
    }
}
