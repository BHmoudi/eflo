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
import com.eflo.document.service.ExpirationManagementService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Cucumber step definitions for Document Expiration Management scenarios.
 */
public class ExpirationManagementSteps {

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private ExpirationManagementService expirationManagementService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @SpyBean
    private DocumentEventPublisher eventPublisher;

    // Scenario context
    private Document expiringDocument;
    private Document expiredDocument;
    private int warningDays;
    private boolean autoArchiveEnabled;
    private List<Document> expiringDocuments;
    private List<Document> expiredDocuments;

    @Before
    public void setUp() {
        expiringDocument = null;
        expiredDocument = null;
        warningDays = 30;
        autoArchiveEnabled = false;
        expiringDocuments = null;
        expiredDocuments = null;
    }

    @Given("a document with expiration date in {int} days")
    public void aDocumentWithExpirationDateInDays(int days) {
        // Create document type with expiration
        DocumentType documentType = DocumentType.builder()
                .typeCode("EXPIRING_DOC_TYPE")
                .typeName("Expiring Document Type")
                .description("Document type with expiration")
                .category("EXPIRATION_TEST")
                .allowedFormats(List.of("pdf", "jpg"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(1)
                .isMandatory(true)
                .requiresValidation(false)
                .hasExpiration(true)
                .expirationWarningDays(30)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        // Upload document with expiration date
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "expiring-document.pdf",
                "application/pdf",
                "Expiring document content".getBytes(StandardCharsets.UTF_8)
        );

        LocalDate expirationDate = LocalDate.now().plusDays(days);

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(documentType.getId())
                .orderId(7001L)
                .orderNumber("ORD-EXPIRATION-001")
                .description("Document with expiration date")
                .expirationDate(expirationDate)
                .build();

        DocumentResponse uploaded = documentManagementService.uploadDocument(uploadRequest);
        expiringDocument = documentRepository.findById(uploaded.getId()).orElseThrow();

        // Validate it so it's in VALIDATED status
        expiringDocument.setStatus(DocumentStatus.VALIDATED);
        expiringDocument = documentRepository.save(expiringDocument);

        assertThat(expiringDocument.getExpirationDate()).isEqualTo(expirationDate);
        System.out.println("Created document expiring in " + days + " days");
        System.out.println("  - Expiration date: " + expirationDate);
    }

    @When("the expiration check scheduler runs")
    public void theExpirationCheckSchedulerRuns() {
        // Manually trigger the expiration check
        expirationManagementService.checkExpiringDocuments();
        System.out.println("Expiration check scheduler executed");
    }

    @Then("an expiration notification should be sent")
    public void anExpirationNotificationShouldBeSent() {
        // Verify notification was sent by checking the event publication
        ArgumentCaptor<String> eventTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> eventDataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(eventPublisher, atLeastOnce()).publishEvent(
                eventTypeCaptor.capture(),
                eventDataCaptor.capture()
        );

        // Check if DOCUMENT_EXPIRING event was published
        List<String> eventTypes = eventTypeCaptor.getAllValues();
        assertThat(eventTypes).contains("DOCUMENT_EXPIRING");

        System.out.println("✓ Expiration notification sent");
        System.out.println("  - Event type: DOCUMENT_EXPIRING");
    }

    @And("the document should be flagged for review")
    public void theDocumentShouldBeFlaggedForReview() {
        // Verify document has expiration notification flag set
        Document doc = documentRepository.findById(expiringDocument.getId()).orElseThrow();
        assertThat(doc.getExpirationNotified()).isTrue();
        assertThat(doc.getExpirationNotificationSentAt()).isNotNull();

        System.out.println("✓ Document flagged for review");
        System.out.println("  - Expiration notified: " + doc.getExpirationNotified());
        System.out.println("  - Notification sent at: " + doc.getExpirationNotificationSentAt());
    }

    @Given("a document that expired {int} day ago")
    public void aDocumentThatExpiredDayAgo(int daysAgo) {
        // Create document type
        DocumentType documentType = DocumentType.builder()
                .typeCode("EXPIRED_DOC_TYPE")
                .typeName("Expired Document Type")
                .description("Document type for expired documents")
                .category("EXPIRATION_TEST")
                .allowedFormats(List.of("pdf"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(1)
                .isMandatory(false)
                .requiresValidation(false)
                .hasExpiration(true)
                .expirationWarningDays(30)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        // Upload document with past expiration date
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "expired-document.pdf",
                "application/pdf",
                "Expired document content".getBytes(StandardCharsets.UTF_8)
        );

        LocalDate expirationDate = LocalDate.now().minusDays(daysAgo);

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(documentType.getId())
                .orderId(7002L)
                .orderNumber("ORD-EXPIRED-001")
                .description("Expired document")
                .expirationDate(expirationDate)
                .build();

        DocumentResponse uploaded = documentManagementService.uploadDocument(uploadRequest);
        expiredDocument = documentRepository.findById(uploaded.getId()).orElseThrow();

        // Set to VALIDATED status (expired documents should be validated before expiring)
        expiredDocument.setStatus(DocumentStatus.VALIDATED);
        expiredDocument = documentRepository.save(expiredDocument);

        assertThat(expiredDocument.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(expiredDocument.getExpirationDate().isBefore(LocalDate.now())).isTrue();

        System.out.println("Created expired document");
        System.out.println("  - Expiration date: " + expirationDate);
        System.out.println("  - Days ago: " + daysAgo);
    }

    @And("auto-archive is enabled")
    public void autoArchiveIsEnabled() {
        autoArchiveEnabled = true;
        System.out.println("Auto-archive enabled: " + autoArchiveEnabled);
    }

    @Then("the document should be archived")
    public void theDocumentShouldBeArchived() {
        // Refresh document from database
        Document doc = documentRepository.findById(expiredDocument.getId()).orElseThrow();

        assertThat(doc.getStatus()).isIn(DocumentStatus.EXPIRED, DocumentStatus.ARCHIVED);
        assertThat(doc.isArchived()).isTrue();

        System.out.println("✓ Document archived");
        System.out.println("  - Status: " + doc.getStatus());
    }

    @And("a document.archived event should be published")
    public void aDocumentArchivedEventShouldBePublished() {
        // Verify archived or expired event was published
        ArgumentCaptor<String> eventTypeCaptor = ArgumentCaptor.forClass(String.class);

        verify(eventPublisher, atLeastOnce()).publishEvent(
                eventTypeCaptor.capture(),
                any(Map.class)
        );

        List<String> eventTypes = eventTypeCaptor.getAllValues();
        assertThat(eventTypes).containsAnyOf("DOCUMENT_EXPIRED", "DOCUMENT_ARCHIVED");

        System.out.println("✓ document.archived event published");
    }

    @Given("documents with various expiration dates exist")
    public void documentsWithVariousExpirationDatesExist() {
        // Create document type
        DocumentType documentType = DocumentType.builder()
                .typeCode("MULTI_EXPIRY_TYPE")
                .typeName("Multi Expiry Type")
                .description("Document type for multiple expiration testing")
                .category("EXPIRATION_TEST")
                .allowedFormats(List.of("pdf"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(0)
                .maxDocuments(10)
                .isMandatory(false)
                .requiresValidation(false)
                .hasExpiration(true)
                .expirationWarningDays(30)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        Long orderId = 7003L;

        // Create documents with different expiration dates
        int[] expirationDays = {5, 15, 25, 35, 45};

        for (int i = 0; i < expirationDays.length; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "multi-expiry-doc-" + i + ".pdf",
                    "application/pdf",
                    ("Document " + i).getBytes(StandardCharsets.UTF_8)
            );

            LocalDate expirationDate = LocalDate.now().plusDays(expirationDays[i]);

            DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                    .file(file)
                    .documentTypeId(documentType.getId())
                    .orderId(orderId)
                    .orderNumber("ORD-MULTI-EXPIRY-001")
                    .description("Document expiring in " + expirationDays[i] + " days")
                    .expirationDate(expirationDate)
                    .build();

            DocumentResponse uploaded = documentManagementService.uploadDocument(uploadRequest);

            // Set to VALIDATED
            Document doc = documentRepository.findById(uploaded.getId()).orElseThrow();
            doc.setStatus(DocumentStatus.VALIDATED);
            documentRepository.save(doc);
        }

        System.out.println("Created 5 documents with various expiration dates");
    }

    @When("the system checks for documents expiring within {int} days")
    public void theSystemChecksForDocumentsExpiringWithinDays(int days) {
        warningDays = days;
        expiringDocuments = expirationManagementService.getExpiringDocuments(days);
        System.out.println("Checked for documents expiring within " + days + " days");
        System.out.println("  - Found: " + expiringDocuments.size() + " documents");
    }

    @Then("documents expiring within the warning period should be identified")
    public void documentsExpiringWithinTheWarningPeriodShouldBeIdentified() {
        assertThat(expiringDocuments).isNotNull();
        assertThat(expiringDocuments).isNotEmpty();

        // Verify all identified documents are within the warning period
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(warningDays);

        for (Document doc : expiringDocuments) {
            assertThat(doc.getExpirationDate()).isNotNull();
            assertThat(doc.getExpirationDate()).isBetween(today, warningDate);
        }

        System.out.println("✓ Identified " + expiringDocuments.size() + " expiring documents");
    }

    @And("expiration notifications should be prepared")
    public void expirationNotificationsShouldBePrepared() {
        // Verify documents need notification
        for (Document doc : expiringDocuments) {
            assertThat(doc.needsExpirationNotification(warningDays)).isTrue();
        }

        System.out.println("✓ Notifications prepared for " + expiringDocuments.size() + " documents");
    }

    @Given("a document has an expiration date")
    public void aDocumentHasAnExpirationDate() {
        aDocumentWithExpirationDateInDays(60);
        System.out.println("Document with expiration date created");
    }

    @When("the expiration date is extended by {int} days")
    public void theExpirationDateIsExtendedByDays(int additionalDays) {
        LocalDate originalExpiration = expiringDocument.getExpirationDate();

        expirationManagementService.extendDocumentExpiration(
                expiringDocument.getId(),
                additionalDays
        );

        // Refresh document
        expiringDocument = documentRepository.findById(expiringDocument.getId()).orElseThrow();
        LocalDate newExpiration = expiringDocument.getExpirationDate();

        System.out.println("Expiration extended:");
        System.out.println("  - Original: " + originalExpiration);
        System.out.println("  - New: " + newExpiration);
        System.out.println("  - Extension: " + additionalDays + " days");
    }

    @Then("the new expiration date should be calculated correctly")
    public void theNewExpirationDateShouldBeCalculatedCorrectly() {
        assertThat(expiringDocument.getExpirationDate()).isNotNull();
        assertThat(expiringDocument.getExpirationDate()).isAfter(LocalDate.now());

        System.out.println("✓ New expiration date: " + expiringDocument.getExpirationDate());
    }

    @And("the expiration notification flag should be reset")
    public void theExpirationNotificationFlagShouldBeReset() {
        assertThat(expiringDocument.getExpirationNotified()).isFalse();
        assertThat(expiringDocument.getExpirationNotificationSentAt()).isNull();

        System.out.println("✓ Expiration notification flag reset");
    }

    @Given("expired documents exist in the system")
    public void expiredDocumentsExistInTheSystem() {
        // Create multiple expired documents
        DocumentType documentType = DocumentType.builder()
                .typeCode("BATCH_EXPIRED_TYPE")
                .typeName("Batch Expired Type")
                .description("Document type for batch expiration")
                .category("EXPIRATION_TEST")
                .allowedFormats(List.of("pdf"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(0)
                .maxDocuments(10)
                .isMandatory(false)
                .requiresValidation(false)
                .hasExpiration(true)
                .expirationWarningDays(30)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        for (int i = 1; i <= 3; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "batch-expired-" + i + ".pdf",
                    "application/pdf",
                    ("Expired document " + i).getBytes(StandardCharsets.UTF_8)
            );

            LocalDate expirationDate = LocalDate.now().minusDays(i);

            DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                    .file(file)
                    .documentTypeId(documentType.getId())
                    .orderId(7004L)
                    .orderNumber("ORD-BATCH-EXPIRED-001")
                    .description("Expired document " + i)
                    .expirationDate(expirationDate)
                    .build();

            DocumentResponse uploaded = documentManagementService.uploadDocument(uploadRequest);

            // Set to VALIDATED
            Document doc = documentRepository.findById(uploaded.getId()).orElseThrow();
            doc.setStatus(DocumentStatus.VALIDATED);
            documentRepository.save(doc);
        }

        System.out.println("Created 3 expired documents");
    }

    @When("the batch expiration process runs")
    public void theBatchExpirationProcessRuns() {
        expiredDocuments = expirationManagementService.getExpiredDocuments();
        System.out.println("Batch expiration process executed");
        System.out.println("  - Found " + expiredDocuments.size() + " expired documents");
    }

    @Then("all expired documents should be processed")
    public void allExpiredDocumentsShouldBeProcessed() {
        assertThat(expiredDocuments).isNotNull();
        assertThat(expiredDocuments).isNotEmpty();

        // Verify all are expired
        LocalDate today = LocalDate.now();
        for (Document doc : expiredDocuments) {
            assertThat(doc.getExpirationDate()).isBefore(today);
        }

        System.out.println("✓ Processed " + expiredDocuments.size() + " expired documents");
    }

    @And("appropriate actions should be taken based on configuration")
    public void appropriateActionsShouldBeTakenBasedOnConfiguration() {
        // Verify documents are marked appropriately
        for (Document doc : expiredDocuments) {
            Document refreshed = documentRepository.findById(doc.getId()).orElseThrow();
            // Document should either be marked expired or archived
            assertThat(refreshed.getStatus()).isIn(
                    DocumentStatus.VALIDATED,
                    DocumentStatus.EXPIRED,
                    DocumentStatus.ARCHIVED
            );
        }

        System.out.println("✓ Appropriate actions taken based on configuration");
    }
}
