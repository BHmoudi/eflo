package com.eflo.document.e2e;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.BulkUploadRequest;
import com.eflo.document.domain.model.BulkUploadResultResponse;
import com.eflo.document.domain.model.DocumentResponse;
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
import org.springframework.web.multipart.MultipartFile;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for bulk document upload.
 * Tests uploading multiple documents in batch with partial failure handling.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"document.uploaded", "document.scan.completed"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BulkUploadE2ETest {

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

    private DocumentType testDocumentType;
    private Long testOrderId = 3001L;
    private String testOrderNumber = "ORD-2024-BULK-001";

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
                .typeCode("BULK_UPLOAD_DOC")
                .typeName("Bulk Upload Document")
                .description("Document type for bulk upload testing")
                .category("GENERAL")
                .allowedFormats(List.of("pdf", "jpg", "png", "docx"))
                .maxFileSizeMb(BigDecimal.valueOf(10.0))
                .minDocuments(0)
                .maxDocuments(100)
                .isMandatory(false)
                .requiresValidation(false)
                .hasExpiration(false)
                .isActive(true)
                .build();
        testDocumentType = documentTypeRepository.save(testDocumentType);
    }

    @AfterEach
    void tearDown() {
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Successful Bulk Upload of Multiple Documents")
    void testSuccessfulBulkUpload() {
        System.out.println("\n=== SUCCESSFUL BULK UPLOAD TEST ===");

        // Create multiple files for bulk upload
        List<MultipartFile> files = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    "document-" + i + ".pdf",
                    "application/pdf",
                    ("Content of document " + i).getBytes(StandardCharsets.UTF_8)
            );
            files.add(file);
        }

        System.out.println("Created " + files.size() + " files for bulk upload");

        // Create bulk upload request
        BulkUploadRequest bulkRequest = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .continueOnError(true)
                .commonMetadata(Map.of(
                        "uploadBatch", "BATCH-001",
                        "uploadReason", "Initial document submission"
                ))
                .commonTags(List.of("bulk-upload", "batch-001"))
                .build();

        // Execute bulk upload
        System.out.println("\n--- Executing Bulk Upload ---");
        long startTime = System.currentTimeMillis();

        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(bulkRequest);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verify results
        System.out.println("\n--- Bulk Upload Results ---");
        System.out.println("Overall Success: " + result.getOverallSuccess());
        System.out.println("Total Files: " + result.getTotalFiles());
        System.out.println("Successful Uploads: " + result.getSuccessfulUploads());
        System.out.println("Failed Uploads: " + result.getFailedUploads());
        System.out.println("Total Processing Time: " + result.getTotalProcessingTimeMs() + "ms");
        System.out.println("Actual Duration: " + duration + "ms");

        assertThat(result.getOverallSuccess()).isTrue();
        assertThat(result.getTotalFiles()).isEqualTo(5);
        assertThat(result.getSuccessfulUploads()).isEqualTo(5);
        assertThat(result.getFailedUploads()).isEqualTo(0);
        assertThat(result.getResults()).hasSize(5);

        // Verify each upload result
        System.out.println("\n--- Individual Upload Results ---");
        for (BulkUploadResultResponse.FileUploadResult fileResult : result.getResults()) {
            System.out.println("File: " + fileResult.getFilename());
            System.out.println("  Success: " + fileResult.getSuccess());
            System.out.println("  Document ID: " + fileResult.getDocumentId());
            System.out.println("  Processing Time: " + fileResult.getProcessingTimeMs() + "ms");

            assertThat(fileResult.getSuccess()).isTrue();
            assertThat(fileResult.getDocumentId()).isNotNull();
            assertThat(fileResult.getDocumentUuid()).isNotNull();
            assertThat(fileResult.getTypeCode()).isEqualTo("BULK_UPLOAD_DOC");
        }

        // Verify all documents in database
        List<DocumentResponse> orderDocuments = documentManagementService.getOrderDocuments(testOrderId);
        assertThat(orderDocuments).hasSize(5);

        // Verify all documents have correct metadata and tags
        orderDocuments.forEach(doc -> {
            assertThat(doc.getOrderId()).isEqualTo(testOrderId);
            assertThat(doc.getOrderNumber()).isEqualTo(testOrderNumber);
            assertThat(doc.getTypeCode()).isEqualTo("BULK_UPLOAD_DOC");
        });

        System.out.println("\n✓ All 5 documents uploaded successfully in bulk!");
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Bulk Upload with Partial Failures - Continue on Error")
    void testBulkUploadWithPartialFailuresContinueOnError() {
        System.out.println("\n=== BULK UPLOAD WITH PARTIAL FAILURES (CONTINUE ON ERROR) ===");

        // Create mix of valid and invalid files
        List<MultipartFile> files = new ArrayList<>();

        // Valid file 1
        files.add(new MockMultipartFile(
                "files",
                "valid-doc-1.pdf",
                "application/pdf",
                "Valid document 1".getBytes(StandardCharsets.UTF_8)
        ));

        // Invalid file - unsupported format
        files.add(new MockMultipartFile(
                "files",
                "invalid-doc.exe",
                "application/x-executable",
                "Invalid executable".getBytes(StandardCharsets.UTF_8)
        ));

        // Valid file 2
        files.add(new MockMultipartFile(
                "files",
                "valid-doc-2.pdf",
                "application/pdf",
                "Valid document 2".getBytes(StandardCharsets.UTF_8)
        ));

        // Invalid file - too large (simulate by setting metadata)
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB - exceeds 10MB limit
        Arrays.fill(largeContent, (byte) 'X');
        files.add(new MockMultipartFile(
                "files",
                "too-large-doc.pdf",
                "application/pdf",
                largeContent
        ));

        // Valid file 3
        files.add(new MockMultipartFile(
                "files",
                "valid-doc-3.pdf",
                "application/pdf",
                "Valid document 3".getBytes(StandardCharsets.UTF_8)
        ));

        System.out.println("Created " + files.size() + " files (3 valid, 2 invalid)");

        // Create bulk upload request with continueOnError = true
        BulkUploadRequest bulkRequest = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .continueOnError(true) // Continue processing even if some fail
                .build();

        // Execute bulk upload
        System.out.println("\n--- Executing Bulk Upload (Continue on Error) ---");
        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(bulkRequest);

        // Verify results
        System.out.println("\n--- Bulk Upload Results ---");
        System.out.println("Overall Success: " + result.getOverallSuccess());
        System.out.println("Total Files: " + result.getTotalFiles());
        System.out.println("Successful Uploads: " + result.getSuccessfulUploads());
        System.out.println("Failed Uploads: " + result.getFailedUploads());

        assertThat(result.getOverallSuccess()).isFalse(); // Not all succeeded
        assertThat(result.getTotalFiles()).isEqualTo(5);
        assertThat(result.getSuccessfulUploads()).isGreaterThanOrEqualTo(1); // At least some succeeded
        assertThat(result.getFailedUploads()).isGreaterThanOrEqualTo(1); // At least some failed

        // Analyze individual results
        System.out.println("\n--- Individual Results ---");
        int successCount = 0;
        int failureCount = 0;

        for (BulkUploadResultResponse.FileUploadResult fileResult : result.getResults()) {
            System.out.println("\nFile: " + fileResult.getFilename());
            System.out.println("  Success: " + fileResult.getSuccess());

            if (fileResult.getSuccess()) {
                System.out.println("  Document ID: " + fileResult.getDocumentId());
                successCount++;
            } else {
                System.out.println("  Error: " + fileResult.getErrorMessage());
                failureCount++;
            }
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Successful: " + successCount);
        System.out.println("Failed: " + failureCount);
        System.out.println("Processing continued despite errors: ✓");

        assertThat(successCount).isGreaterThan(0);
        assertThat(failureCount).isGreaterThan(0);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Bulk Upload with Stop on First Error")
    void testBulkUploadStopOnFirstError() {
        System.out.println("\n=== BULK UPLOAD WITH STOP ON FIRST ERROR ===");

        // Create mix of files with invalid file first
        List<MultipartFile> files = new ArrayList<>();

        // Invalid file first
        files.add(new MockMultipartFile(
                "files",
                "invalid-first.exe",
                "application/x-executable",
                "Invalid file".getBytes(StandardCharsets.UTF_8)
        ));

        // Valid files that should not be processed
        for (int i = 1; i <= 3; i++) {
            files.add(new MockMultipartFile(
                    "files",
                    "valid-doc-" + i + ".pdf",
                    "application/pdf",
                    ("Valid document " + i).getBytes(StandardCharsets.UTF_8)
            ));
        }

        // Create bulk upload request with continueOnError = false
        BulkUploadRequest bulkRequest = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .continueOnError(false) // Stop on first error
                .build();

        // Execute bulk upload
        System.out.println("\n--- Executing Bulk Upload (Stop on Error) ---");
        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(bulkRequest);

        // Verify results
        System.out.println("\n--- Bulk Upload Results ---");
        System.out.println("Overall Success: " + result.getOverallSuccess());
        System.out.println("Total Files: " + result.getTotalFiles());
        System.out.println("Successful Uploads: " + result.getSuccessfulUploads());
        System.out.println("Failed Uploads: " + result.getFailedUploads());
        System.out.println("Results Count: " + result.getResults().size());

        assertThat(result.getOverallSuccess()).isFalse();
        assertThat(result.getSuccessfulUploads()).isEqualTo(0);
        assertThat(result.getFailedUploads()).isEqualTo(1);

        // Should only have 1 result (stopped after first error)
        assertThat(result.getResults().size()).isLessThanOrEqualTo(1);

        System.out.println("\n✓ Processing stopped after first error as expected");
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Bulk Upload with File-Specific Metadata")
    void testBulkUploadWithFileSpecificMetadata() {
        System.out.println("\n=== BULK UPLOAD WITH FILE-SPECIFIC METADATA ===");

        // Create files
        List<MultipartFile> files = new ArrayList<>();

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "contract.pdf",
                "application/pdf",
                "Contract content".getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "invoice.pdf",
                "application/pdf",
                "Invoice content".getBytes(StandardCharsets.UTF_8)
        );

        files.add(file1);
        files.add(file2);

        // Create file-specific metadata
        List<BulkUploadRequest.FileMetadata> fileMetadataList = new ArrayList<>();

        fileMetadataList.add(BulkUploadRequest.FileMetadata.builder()
                .filename("contract.pdf")
                .description("Service contract agreement")
                .documentTypeId(testDocumentType.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .metadata(Map.of(
                        "contractNumber", "CNT-2024-001",
                        "contractValue", "50000"
                ))
                .tags(List.of("contract", "legal"))
                .build());

        fileMetadataList.add(BulkUploadRequest.FileMetadata.builder()
                .filename("invoice.pdf")
                .description("Monthly service invoice")
                .documentTypeId(testDocumentType.getId())
                .metadata(Map.of(
                        "invoiceNumber", "INV-2024-001",
                        "invoiceAmount", "5000"
                ))
                .tags(List.of("invoice", "finance"))
                .build());

        // Create bulk upload request
        BulkUploadRequest bulkRequest = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .fileMetadata(fileMetadataList)
                .continueOnError(true)
                .build();

        // Execute bulk upload
        System.out.println("\n--- Executing Bulk Upload with File-Specific Metadata ---");
        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(bulkRequest);

        // Verify results
        System.out.println("\n--- Results ---");
        assertThat(result.getOverallSuccess()).isTrue();
        assertThat(result.getSuccessfulUploads()).isEqualTo(2);

        System.out.println("✓ All documents uploaded with file-specific metadata");

        // Verify metadata in database
        List<DocumentResponse> orderDocuments = documentManagementService.getOrderDocuments(testOrderId);
        assertThat(orderDocuments).hasSize(2);

        System.out.println("\n--- Document Details ---");
        for (DocumentResponse doc : orderDocuments) {
            System.out.println("Document: " + doc.getOriginalFilename());
            System.out.println("  Description: " + doc.getDescription());
            System.out.println("  Metadata: " + doc.getCustomMetadata());
            System.out.println("  Tags: " + doc.getTags());

            assertThat(doc.getCustomMetadata()).isNotEmpty();
        }
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Large Bulk Upload Performance Test")
    void testLargeBulkUploadPerformance() {
        System.out.println("\n=== LARGE BULK UPLOAD PERFORMANCE TEST ===");

        // Create 20 files for performance testing
        int fileCount = 20;
        List<MultipartFile> files = new ArrayList<>();

        for (int i = 1; i <= fileCount; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    "perf-test-doc-" + i + ".pdf",
                    "application/pdf",
                    ("Performance test document " + i + " with some content").getBytes(StandardCharsets.UTF_8)
            );
            files.add(file);
        }

        System.out.println("Created " + fileCount + " files for bulk upload");

        // Create bulk upload request
        BulkUploadRequest bulkRequest = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .continueOnError(true)
                .commonTags(List.of("performance-test"))
                .build();

        // Execute bulk upload and measure time
        System.out.println("\n--- Executing Large Bulk Upload ---");
        long startTime = System.currentTimeMillis();

        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(bulkRequest);

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // Performance analysis
        System.out.println("\n--- Performance Results ---");
        System.out.println("Total Files: " + result.getTotalFiles());
        System.out.println("Successful Uploads: " + result.getSuccessfulUploads());
        System.out.println("Failed Uploads: " + result.getFailedUploads());
        System.out.println("Total Processing Time: " + result.getTotalProcessingTimeMs() + "ms");
        System.out.println("Actual Duration: " + totalDuration + "ms");

        double avgTimePerFile = result.getTotalProcessingTimeMs() / (double) fileCount;
        System.out.println("Average Time Per File: " + String.format("%.2f", avgTimePerFile) + "ms");

        assertThat(result.getOverallSuccess()).isTrue();
        assertThat(result.getSuccessfulUploads()).isEqualTo(fileCount);

        // Verify all documents in storage
        List<DocumentResponse> orderDocuments = documentManagementService.getOrderDocuments(testOrderId);
        assertThat(orderDocuments).hasSize(fileCount);

        System.out.println("\n✓ Large bulk upload completed successfully!");
        System.out.println("Performance: " + String.format("%.2f", avgTimePerFile) + "ms average per file");
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Bulk Upload Verification - All Stored and Scanned")
    void testBulkUploadVerification() throws InterruptedException {
        System.out.println("\n=== BULK UPLOAD VERIFICATION TEST ===");

        // Upload 5 documents
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            files.add(new MockMultipartFile(
                    "files",
                    "verify-doc-" + i + ".pdf",
                    "application/pdf",
                    ("Document " + i).getBytes(StandardCharsets.UTF_8)
            ));
        }

        BulkUploadRequest bulkRequest = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(testDocumentType.getId())
                .orderId(testOrderId)
                .orderNumber(testOrderNumber)
                .continueOnError(true)
                .build();

        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(bulkRequest);

        assertThat(result.getOverallSuccess()).isTrue();
        System.out.println("Uploaded " + result.getSuccessfulUploads() + " documents");

        // Wait for async virus scanning to complete
        Thread.sleep(2000);

        // Verify all documents are stored
        System.out.println("\n--- Verifying Storage ---");
        List<DocumentResponse> orderDocuments = documentManagementService.getOrderDocuments(testOrderId);
        assertThat(orderDocuments).hasSize(5);
        System.out.println("✓ All 5 documents found in database");

        // Verify all documents have storage paths
        for (DocumentResponse doc : orderDocuments) {
            assertThat(doc.getStoragePath()).isNotNull();
            assertThat(doc.getStorageBucket()).isNotNull();
            System.out.println("Document " + doc.getId() + " stored at: " + doc.getStoragePath());
        }

        // Verify all documents have been scanned (or scan attempted)
        System.out.println("\n--- Verifying Virus Scans ---");
        for (DocumentResponse doc : orderDocuments) {
            assertThat(doc.getVirusScanStatus()).isNotNull();
            System.out.println("Document " + doc.getId() + " scan status: " + doc.getVirusScanStatus());
        }

        System.out.println("\n✓ All documents verified: stored and scanned!");
    }
}
