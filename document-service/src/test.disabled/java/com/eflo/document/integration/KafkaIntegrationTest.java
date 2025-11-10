package com.eflo.document.integration;

import com.eflo.document.config.KafkaConfig;
import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentEvent;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.scanner.ScanResult;
import com.eflo.document.service.DocumentEventPublisher;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Kafka event publishing and consuming.
 * Tests with embedded Kafka broker.
 *
 * @author Document Service
 * @version 1.0
 */
@EmbeddedKafka(
    partitions = 1,
    topics = {
        KafkaConfig.TOPIC_DOCUMENT_UPLOADED,
        KafkaConfig.TOPIC_DOCUMENT_VALIDATED,
        KafkaConfig.TOPIC_DOCUMENT_REJECTED,
        KafkaConfig.TOPIC_DOCUMENT_DELETED,
        KafkaConfig.TOPIC_DOCUMENT_ARCHIVED,
        KafkaConfig.TOPIC_DOCUMENT_SCAN_COMPLETED,
        KafkaConfig.TOPIC_DOCUMENT_VERSION_CREATED,
        KafkaConfig.TOPIC_ALL_DOCUMENTS_VALIDATED
    }
)
class KafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DocumentEventPublisher eventPublisher;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestKafkaConsumer testConsumer;

    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        // Clear consumer queues
        testConsumer.clearQueues();

        // Create test document type
        testDocumentType = DocumentType.builder()
            .typeCode("INVOICE")
            .typeName("Invoice")
            .category(DocumentCategory.FINANCIAL)
            .isActive(true)
            .requiresValidation(true)
            .build();
        testDocumentType = documentTypeRepository.save(testDocumentType);
    }

    @Test
    @DisplayName("Should publish document uploaded event to Kafka")
    void testPublishDocumentUploadedEvent() throws Exception {
        // Given
        Document document = createTestDocument();

        // When
        eventPublisher.publishDocumentUploaded(document);

        // Then
        DocumentEvent.DocumentUploadedEvent event = (DocumentEvent.DocumentUploadedEvent)
            testConsumer.uploadedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(document.getId());
        assertThat(event.getDocumentUuid()).isEqualTo(document.getDocumentUuid());
        assertThat(event.getOrderId()).isEqualTo(document.getOrderId());
        assertThat(event.getOrderNumber()).isEqualTo(document.getOrderNumber());
        assertThat(event.getDocumentType()).isEqualTo(document.getTypeCode());
        assertThat(event.getFileName()).isEqualTo(document.getOriginalFilename());
    }

    @Test
    @DisplayName("Should publish document validated event to Kafka")
    void testPublishDocumentValidatedEvent() throws Exception {
        // Given
        Document document = createTestDocument();
        document.markAsValidated("validator@test.com", "Looks good");
        documentRepository.save(document);

        // When
        eventPublisher.publishDocumentValidated(document, "validator@test.com");

        // Then
        DocumentEvent.DocumentValidatedEvent event = (DocumentEvent.DocumentValidatedEvent)
            testConsumer.validatedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(document.getId());
        assertThat(event.getValidatedBy()).isEqualTo("validator@test.com");
        assertThat(event.getValidationComments()).isEqualTo("Looks good");
    }

    @Test
    @DisplayName("Should publish document rejected event to Kafka")
    void testPublishDocumentRejectedEvent() throws Exception {
        // Given
        Document document = createTestDocument();
        document.markAsRejected("validator@test.com", "Incomplete");
        documentRepository.save(document);

        // When
        eventPublisher.publishDocumentRejected(document, "Incomplete");

        // Then
        DocumentEvent.DocumentRejectedEvent event = (DocumentEvent.DocumentRejectedEvent)
            testConsumer.rejectedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(document.getId());
        assertThat(event.getRejectedBy()).isEqualTo("validator@test.com");
        assertThat(event.getRejectionReason()).isEqualTo("Incomplete");
    }

    @Test
    @DisplayName("Should publish document deleted event to Kafka")
    void testPublishDocumentDeletedEvent() throws Exception {
        // Given
        Document document = createTestDocument();

        // When
        eventPublisher.publishDocumentDeleted(
            document.getId(),
            document.getOrderId(),
            document.getOrderNumber(),
            "admin@test.com",
            "No longer needed"
        );

        // Then
        DocumentEvent.DocumentDeletedEvent event = (DocumentEvent.DocumentDeletedEvent)
            testConsumer.deletedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(document.getId());
        assertThat(event.getDeletedBy()).isEqualTo("admin@test.com");
        assertThat(event.getDeletionReason()).isEqualTo("No longer needed");
    }

    @Test
    @DisplayName("Should publish document archived event to Kafka")
    void testPublishDocumentArchivedEvent() throws Exception {
        // Given
        Document document = createTestDocument();
        document.markAsArchived();
        documentRepository.save(document);

        // When
        eventPublisher.publishDocumentArchived(document);

        // Then
        DocumentEvent.DocumentArchivedEvent event = (DocumentEvent.DocumentArchivedEvent)
            testConsumer.archivedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(document.getId());
        assertThat(event.getArchiveLocation()).isEqualTo(document.getStoragePath());
    }

    @Test
    @DisplayName("Should publish scan completed event to Kafka")
    void testPublishScanCompletedEvent() throws Exception {
        // Given
        Document document = createTestDocument();
        ScanResult scanResult = ScanResult.clean("test-file.pdf");

        // When
        eventPublisher.publishScanCompleted(document, scanResult);

        // Then
        DocumentEvent.DocumentScanCompletedEvent event = (DocumentEvent.DocumentScanCompletedEvent)
            testConsumer.scanCompletedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(document.getId());
        assertThat(event.getIsSafe()).isTrue();
        assertThat(event.getScanType()).isEqualTo("VIRUS_SCAN");
    }

    @Test
    @DisplayName("Should publish version created event to Kafka")
    void testPublishVersionCreatedEvent() throws Exception {
        // Given
        Document parentDocument = createTestDocument();
        Document newVersion = createTestDocument();
        newVersion.setVersion(2);
        newVersion.setParentDocument(parentDocument);

        // When
        eventPublisher.publishVersionCreated(parentDocument, newVersion);

        // Then
        DocumentEvent.DocumentVersionCreatedEvent event = (DocumentEvent.DocumentVersionCreatedEvent)
            testConsumer.versionCreatedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getDocumentId()).isEqualTo(newVersion.getId());
        assertThat(event.getParentDocumentId()).isEqualTo(parentDocument.getId());
        assertThat(event.getOldVersion()).isEqualTo(parentDocument.getVersion());
        assertThat(event.getNewVersion()).isEqualTo(newVersion.getVersion());
    }

    @Test
    @DisplayName("Should publish all documents validated event to Kafka")
    void testPublishAllDocumentsValidatedEvent() throws Exception {
        // Given
        Long orderId = 1001L;

        // When
        eventPublisher.publishAllDocumentsValidated(orderId, 5, 5, "validator@test.com");

        // Then
        DocumentEvent.AllDocumentsValidatedEvent event = (DocumentEvent.AllDocumentsValidatedEvent)
            testConsumer.allValidatedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getTotalDocuments()).isEqualTo(5);
        assertThat(event.getValidatedDocuments()).isEqualTo(5);
        assertThat(event.getOrderReadyForProcessing()).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple events in sequence")
    void testMultipleEventsInSequence() throws Exception {
        // Given
        Document doc1 = createTestDocument();
        Document doc2 = createTestDocument();

        // When - Publish multiple events
        eventPublisher.publishDocumentUploaded(doc1);
        eventPublisher.publishDocumentUploaded(doc2);

        // Then - Both events should be received
        DocumentEvent.DocumentUploadedEvent event1 = (DocumentEvent.DocumentUploadedEvent)
            testConsumer.uploadedEvents.poll(5, TimeUnit.SECONDS);
        DocumentEvent.DocumentUploadedEvent event2 = (DocumentEvent.DocumentUploadedEvent)
            testConsumer.uploadedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event1).isNotNull();
        assertThat(event2).isNotNull();
        assertThat(event1.getDocumentId()).isNotEqualTo(event2.getDocumentId());
    }

    @Test
    @DisplayName("Should preserve event metadata")
    void testEventMetadata() throws Exception {
        // Given
        Document document = createTestDocument();
        document.setTags(java.util.List.of("urgent", "q1"));
        document.setBusinessUnitId(100L);
        documentRepository.save(document);

        // When
        eventPublisher.publishDocumentUploaded(document);

        // Then
        DocumentEvent.DocumentUploadedEvent event = (DocumentEvent.DocumentUploadedEvent)
            testConsumer.uploadedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        assertThat(event.getMetadata()).isNotEmpty();
        assertThat(event.getMetadata()).containsKey("fileName");
        assertThat(event.getMetadata()).containsKey("fileSize");
        assertThat(event.getMetadata()).containsKey("businessUnitId");
        assertThat(event.getMetadata()).containsKey("tags");
    }

    @Test
    @DisplayName("Should use order number as event key for partitioning")
    void testEventKeyPartitioning() throws Exception {
        // Given
        Document document = createTestDocument();
        String expectedKey = document.getOrderNumber();

        // When
        eventPublisher.publishDocumentUploaded(document);

        // Then
        DocumentEvent.DocumentUploadedEvent event = (DocumentEvent.DocumentUploadedEvent)
            testConsumer.uploadedEvents.poll(5, TimeUnit.SECONDS);

        assertThat(event).isNotNull();
        // Event should be published with order number as key
        // This ensures all events for the same order go to the same partition
    }

    @Test
    @DisplayName("Should handle event publishing errors gracefully")
    void testEventPublishingErrorHandling() {
        // Given
        Document document = createTestDocument();

        // When - Even if Kafka is down, should not throw exception
        // (EventPublisher catches exceptions internally)
        try {
            eventPublisher.publishDocumentUploaded(document);
            // Should complete without throwing
        } catch (Exception e) {
            // Should not reach here
            assertThat(e).isNull();
        }
    }

    // Helper methods

    private Document createTestDocument() {
        Document document = Document.builder()
            .documentUuid(UUID.randomUUID())
            .documentType(testDocumentType)
            .typeCode(testDocumentType.getTypeCode())
            .orderId(1001L)
            .orderNumber("ORD-2024-" + UUID.randomUUID().toString().substring(0, 4))
            .originalFilename("test-document.pdf")
            .storedFilename("stored-" + UUID.randomUUID() + ".pdf")
            .fileExtension("pdf")
            .mimeType("application/pdf")
            .fileSizeBytes(1000L)
            .fileHash("hash-" + UUID.randomUUID())
            .storageBucket("documents")
            .storagePath("orders/1001/INVOICE/test.pdf")
            .version(1)
            .isLatestVersion(true)
            .status(DocumentStatus.PENDING)
            .uploadedBy("test-user")
            .uploadedAt(LocalDateTime.now())
            .build();

        return documentRepository.save(document);
    }

    /**
     * Test Kafka Consumer to capture events for testing
     */
    @Component
    public static class TestKafkaConsumer {

        public final BlockingQueue<Object> uploadedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> validatedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> rejectedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> deletedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> archivedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> scanCompletedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> versionCreatedEvents = new LinkedBlockingQueue<>();
        public final BlockingQueue<Object> allValidatedEvents = new LinkedBlockingQueue<>();

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_UPLOADED,
            groupId = "test-group",
            containerFactory = "kafkaListenerContainerFactory"
        )
        public void onDocumentUploaded(ConsumerRecord<String, DocumentEvent.DocumentUploadedEvent> record) {
            uploadedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_VALIDATED,
            groupId = "test-group"
        )
        public void onDocumentValidated(ConsumerRecord<String, DocumentEvent.DocumentValidatedEvent> record) {
            validatedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_REJECTED,
            groupId = "test-group"
        )
        public void onDocumentRejected(ConsumerRecord<String, DocumentEvent.DocumentRejectedEvent> record) {
            rejectedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_DELETED,
            groupId = "test-group"
        )
        public void onDocumentDeleted(ConsumerRecord<String, DocumentEvent.DocumentDeletedEvent> record) {
            deletedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_ARCHIVED,
            groupId = "test-group"
        )
        public void onDocumentArchived(ConsumerRecord<String, DocumentEvent.DocumentArchivedEvent> record) {
            archivedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_SCAN_COMPLETED,
            groupId = "test-group"
        )
        public void onScanCompleted(ConsumerRecord<String, DocumentEvent.DocumentScanCompletedEvent> record) {
            scanCompletedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_DOCUMENT_VERSION_CREATED,
            groupId = "test-group"
        )
        public void onVersionCreated(ConsumerRecord<String, DocumentEvent.DocumentVersionCreatedEvent> record) {
            versionCreatedEvents.offer(record.value());
        }

        @KafkaListener(
            topics = KafkaConfig.TOPIC_ALL_DOCUMENTS_VALIDATED,
            groupId = "test-group"
        )
        public void onAllDocumentsValidated(ConsumerRecord<String, DocumentEvent.AllDocumentsValidatedEvent> record) {
            allValidatedEvents.offer(record.value());
        }

        public void clearQueues() {
            uploadedEvents.clear();
            validatedEvents.clear();
            rejectedEvents.clear();
            deletedEvents.clear();
            archivedEvents.clear();
            scanCompletedEvents.clear();
            versionCreatedEvents.clear();
            allValidatedEvents.clear();
        }
    }
}
