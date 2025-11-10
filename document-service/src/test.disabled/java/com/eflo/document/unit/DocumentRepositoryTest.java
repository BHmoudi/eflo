package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("DocumentRepository Integration Tests")
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    private DocumentType testDocumentType;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
                .typeCode("INVOICE")
                .typeName("Invoice")
                .isActive(true)
                .isMandatory(true)
                .build();
        testDocumentType = entityManager.persist(testDocumentType);

        testDocument = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .typeCode("INVOICE")
                .orderId(100L)
                .orderNumber("ORD-001")
                .originalFilename("invoice.pdf")
                .storedFilename("stored_invoice.pdf")
                .fileExtension("pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(1024L)
                .fileHash("abc123")
                .storageBucket("documents")
                .storagePath("/path/invoice.pdf")
                .version(1)
                .isLatestVersion(true)
                .status(DocumentStatus.PENDING)
                .virusScanStatus(VirusScanStatus.CLEAN)
                .uploadedBy("testuser")
                .uploadedAt(LocalDateTime.now())
                .build();
        testDocument = entityManager.persistAndFlush(testDocument);
    }

    @Test
    @DisplayName("findByDocumentUuid - should find document by UUID")
    void testFindByDocumentUuid() {
        // Act
        Optional<Document> result = documentRepository.findByDocumentUuid(testDocument.getDocumentUuid());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testDocument.getId());
        assertThat(result.get().getOriginalFilename()).isEqualTo("invoice.pdf");
    }

    @Test
    @DisplayName("findByOrderId - should find all documents for order")
    void testFindByOrderId() {
        // Arrange
        Document doc2 = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(100L)
                .originalFilename("receipt.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(doc2);

        // Act
        List<Document> result = documentRepository.findByOrderId(100L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Document::getOrderId).containsOnly(100L);
    }

    @Test
    @DisplayName("findByOrderIdAndStatus - should filter by status")
    void testFindByOrderIdAndStatus() {
        // Arrange
        Document validatedDoc = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(100L)
                .status(DocumentStatus.VALIDATED)
                .originalFilename("validated.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(validatedDoc);

        // Act
        List<Document> pendingDocs = documentRepository.findByOrderIdAndStatus(100L, DocumentStatus.PENDING);
        List<Document> validatedDocs = documentRepository.findByOrderIdAndStatus(100L, DocumentStatus.VALIDATED);

        // Assert
        assertThat(pendingDocs).hasSize(1);
        assertThat(validatedDocs).hasSize(1);
    }

    @Test
    @DisplayName("findByUploadedBy - should find user's documents")
    void testFindByUploadedBy() {
        // Act
        List<Document> result = documentRepository.findByUploadedBy("testuser");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Document::getUploadedBy).containsOnly("testuser");
    }

    @Test
    @DisplayName("findByVirusScanStatus - should filter by scan status")
    void testFindByVirusScanStatus() {
        // Act
        List<Document> cleanDocs = documentRepository.findByVirusScanStatus(VirusScanStatus.CLEAN);

        // Assert
        assertThat(cleanDocs).hasSize(1);
        assertThat(cleanDocs.get(0).getVirusScanStatus()).isEqualTo(VirusScanStatus.CLEAN);
    }

    @Test
    @DisplayName("findByIsLatestVersionTrue - should find only latest versions")
    void testFindByIsLatestVersionTrue() {
        // Arrange
        Document oldVersion = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(100L)
                .isLatestVersion(false)
                .version(1)
                .originalFilename("old.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(oldVersion);

        // Act
        List<Document> latestVersions = documentRepository.findByIsLatestVersionTrue();

        // Assert
        assertThat(latestVersions).isNotEmpty();
        assertThat(latestVersions).allMatch(Document::isLatest);
    }

    @Test
    @DisplayName("findByFileHash - should find documents with same hash")
    void testFindByFileHash() {
        // Arrange
        Document duplicate = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(200L)
                .fileHash("abc123")
                .originalFilename("duplicate.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(duplicate);

        // Act
        List<Document> duplicates = documentRepository.findByFileHash("abc123");

        // Assert
        assertThat(duplicates).hasSize(2);
        assertThat(duplicates).extracting(Document::getFileHash).containsOnly("abc123");
    }

    @Test
    @DisplayName("countByStatus - should count documents by status")
    void testCountByStatus() {
        // Act
        long count = documentRepository.countByStatus(DocumentStatus.PENDING);

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countByOrderId - should count order documents")
    void testCountByOrderId() {
        // Act
        long count = documentRepository.countByOrderId(100L);

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countByOrderIdAndDocumentTypeId - should count by type")
    void testCountByOrderIdAndDocumentTypeId() {
        // Act
        long count = documentRepository.countByOrderIdAndDocumentTypeId(100L, testDocumentType.getId());

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("findPendingVirusScan - should find pending scans")
    void testFindPendingVirusScan() {
        // Arrange
        Document pendingScan = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(100L)
                .virusScanStatus(VirusScanStatus.PENDING)
                .originalFilename("pending.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(pendingScan);

        // Act
        List<Document> result = documentRepository.findPendingVirusScan();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVirusScanStatus()).isEqualTo(VirusScanStatus.PENDING);
    }

    @Test
    @DisplayName("existsByDocumentUuid - should check existence")
    void testExistsByDocumentUuid() {
        // Act
        boolean exists = documentRepository.existsByDocumentUuid(testDocument.getDocumentUuid());
        boolean notExists = documentRepository.existsByDocumentUuid(UUID.randomUUID());

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("calculateTotalStorageByOrder - should sum file sizes")
    void testCalculateTotalStorageByOrder() {
        // Arrange
        Document doc2 = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(100L)
                .fileSizeBytes(2048L)
                .status(DocumentStatus.VALIDATED)
                .originalFilename("doc2.pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(doc2);

        // Act
        Long totalSize = documentRepository.calculateTotalStorageByOrder(100L);

        // Assert
        assertThat(totalSize).isEqualTo(3072L); // 1024 + 2048
    }

    @Test
    @DisplayName("findByOrderIdOrderByUploadedAtDesc - should order by upload date")
    void testFindByOrderIdOrderByUploadedAtDesc() {
        // Arrange
        Document newerDoc = Document.builder()
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .orderId(100L)
                .originalFilename("newer.pdf")
                .uploadedAt(LocalDateTime.now().plusMinutes(5))
                .build();
        entityManager.persistAndFlush(newerDoc);

        // Act
        List<Document> result = documentRepository.findByOrderIdOrderByUploadedAtDesc(100L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOriginalFilename()).isEqualTo("newer.pdf");
    }
}
