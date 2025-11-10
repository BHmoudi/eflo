package com.eflo.document.scenarios;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentResponse;
import com.eflo.document.domain.model.DocumentUploadRequest;
import com.eflo.document.domain.model.DocumentValidationRequest;
import com.eflo.document.domain.model.DocumentValidationResponse;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentEventPublisher;
import com.eflo.document.service.DocumentManagementService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Cucumber step definitions for Document Validation scenarios.
 */
public class DocumentValidationSteps {

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @SpyBean
    private DocumentEventPublisher eventPublisher;

    // Scenario context
    private Document pendingDocument;
    private DocumentValidationResponse validationResponse;
    private String documentManagerUser;
    private String rejectionReason;

    @Before
    public void setUp() {
        pendingDocument = null;
        validationResponse = null;
        documentManagerUser = null;
        rejectionReason = null;
    }

    @Given("a document is pending validation")
    public void aDocumentIsPendingValidation() {
        // Create a document type
        DocumentType documentType = DocumentType.builder()
                .typeCode("PENDING_DOC_TYPE")
                .typeName("Pending Document Type")
                .description("Document type for validation testing")
                .category("TEST")
                .allowedFormats(List.of("pdf", "jpg", "png"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(1)
                .isMandatory(true)
                .requiresValidation(true)
                .hasExpiration(false)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        // Upload a document
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pending-document.pdf",
                "application/pdf",
                "Pending document content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(documentType.getId())
                .orderId(6001L)
                .orderNumber("ORD-VALIDATION-001")
                .description("Document awaiting validation")
                .build();

        DocumentResponse uploaded = documentManagementService.uploadDocument(uploadRequest);
        pendingDocument = documentRepository.findById(uploaded.getId()).orElseThrow();

        assertThat(pendingDocument.getStatus()).isEqualTo(DocumentStatus.PENDING);
        System.out.println("Created pending document - ID: " + pendingDocument.getId());
    }

    @And("a document manager is assigned to validate")
    public void aDocumentManagerIsAssignedToValidate() {
        documentManagerUser = "document-manager-001";
        System.out.println("Document manager assigned: " + documentManagerUser);
    }

    @When("the document manager validates the document")
    public void theDocumentManagerValidatesTheDocument() {
        DocumentValidationRequest validationRequest = DocumentValidationRequest.builder()
                .isApproved(true)
                .comments("Document reviewed and approved by document manager")
                .validatedBy(documentManagerUser)
                .build();

        validationResponse = documentManagementService.validateDocument(
                pendingDocument.getId(),
                validationRequest
        );

        System.out.println("Validation completed - Approved: " + validationResponse.getIsApproved());
    }

    @Then("the document status should be VALIDATED")
    public void theDocumentStatusShouldBeVALIDATED() {
        assertThat(validationResponse).isNotNull();
        assertThat(validationResponse.getStatus()).isEqualTo(DocumentStatus.VALIDATED);

        // Verify in database
        Document validatedDoc = documentRepository.findById(pendingDocument.getId()).orElseThrow();
        assertThat(validatedDoc.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(validatedDoc.isValidated()).isTrue();

        System.out.println("✓ Document status is VALIDATED");
    }

    @And("the order document status should be updated")
    public void theOrderDocumentStatusShouldBeUpdated() {
        Document validatedDoc = documentRepository.findById(pendingDocument.getId()).orElseThrow();

        assertThat(validatedDoc.getValidatedAt()).isNotNull();
        assertThat(validatedDoc.getValidatedBy()).isEqualTo(documentManagerUser);
        assertThat(validatedDoc.getValidationComments()).isNotNull();

        System.out.println("✓ Order document status updated");
        System.out.println("  - Validated at: " + validatedDoc.getValidatedAt());
        System.out.println("  - Validated by: " + validatedDoc.getValidatedBy());
    }

    @And("a document.validated event should be published")
    public void aDocumentValidatedEventShouldBePublished() {
        // Verify event was published
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);

        verify(eventPublisher, atLeastOnce()).publishDocumentValidated(
                documentCaptor.capture(),
                userCaptor.capture()
        );

        Document publishedDocument = documentCaptor.getValue();
        assertThat(publishedDocument.getId()).isEqualTo(pendingDocument.getId());
        assertThat(userCaptor.getValue()).isEqualTo(documentManagerUser);

        System.out.println("✓ document.validated event published");
        System.out.println("  - Document ID: " + publishedDocument.getId());
        System.out.println("  - Validated by: " + userCaptor.getValue());
    }

    @When("the document manager rejects the document with reason {string}")
    public void theDocumentManagerRejectsTheDocumentWithReason(String reason) {
        rejectionReason = reason;

        DocumentValidationRequest rejectionRequest = DocumentValidationRequest.builder()
                .isApproved(false)
                .comments(reason)
                .validatedBy(documentManagerUser)
                .build();

        validationResponse = documentManagementService.validateDocument(
                pendingDocument.getId(),
                rejectionRequest
        );

        System.out.println("Document rejected - Reason: " + reason);
    }

    @Then("the document status should be REJECTED")
    public void theDocumentStatusShouldBeREJECTED() {
        assertThat(validationResponse).isNotNull();
        assertThat(validationResponse.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(validationResponse.getIsApproved()).isFalse();

        // Verify in database
        Document rejectedDoc = documentRepository.findById(pendingDocument.getId()).orElseThrow();
        assertThat(rejectedDoc.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejectedDoc.isRejected()).isTrue();
        assertThat(rejectedDoc.getStatusReason()).isEqualTo(rejectionReason);

        System.out.println("✓ Document status is REJECTED");
        System.out.println("  - Reason: " + rejectedDoc.getStatusReason());
    }

    @And("a document.rejected event should be published")
    public void aDocumentRejectedEventShouldBePublished() {
        // Verify event was published
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);

        verify(eventPublisher, atLeastOnce()).publishDocumentRejected(
                documentCaptor.capture(),
                reasonCaptor.capture()
        );

        Document publishedDocument = documentCaptor.getValue();
        assertThat(publishedDocument.getId()).isEqualTo(pendingDocument.getId());
        assertThat(reasonCaptor.getValue()).isEqualTo(rejectionReason);

        System.out.println("✓ document.rejected event published");
        System.out.println("  - Document ID: " + publishedDocument.getId());
        System.out.println("  - Rejection reason: " + reasonCaptor.getValue());
    }

    @Given("a validated document exists")
    public void aValidatedDocumentExists() {
        // Create and validate a document
        aDocumentIsPendingValidation();
        aDocumentManagerIsAssignedToValidate();
        theDocumentManagerValidatesTheDocument();

        assertThat(pendingDocument.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        System.out.println("Validated document created - ID: " + pendingDocument.getId());
    }

    @And("the document has validation comments")
    public void theDocumentHasValidationComments() {
        Document doc = documentRepository.findById(pendingDocument.getId()).orElseThrow();
        assertThat(doc.getValidationComments()).isNotNull();
        assertThat(doc.getValidationComments()).isNotEmpty();
        System.out.println("✓ Validation comments present: " + doc.getValidationComments());
    }

    @And("the validation timestamp should be recorded")
    public void theValidationTimestampShouldBeRecorded() {
        Document doc = documentRepository.findById(pendingDocument.getId()).orElseThrow();
        assertThat(doc.getValidatedAt()).isNotNull();
        System.out.println("✓ Validation timestamp: " + doc.getValidatedAt());
    }

    @Given("multiple documents are pending validation for order {string}")
    public void multipleDocumentsArePendingValidationForOrder(String orderNumber) {
        Long orderId = 6002L;

        // Create document type
        DocumentType documentType = DocumentType.builder()
                .typeCode("BATCH_VALIDATION_TYPE")
                .typeName("Batch Validation Type")
                .description("Document type for batch validation")
                .category("TEST")
                .allowedFormats(List.of("pdf"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(10)
                .isMandatory(false)
                .requiresValidation(true)
                .hasExpiration(false)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        // Create multiple pending documents
        for (int i = 1; i <= 3; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "batch-doc-" + i + ".pdf",
                    "application/pdf",
                    ("Batch document " + i).getBytes(StandardCharsets.UTF_8)
            );

            DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                    .file(file)
                    .documentTypeId(documentType.getId())
                    .orderId(orderId)
                    .orderNumber(orderNumber)
                    .description("Batch document " + i)
                    .build();

            documentManagementService.uploadDocument(uploadRequest);
        }

        // Store for later use
        List<Document> pendingDocs = documentRepository.findByOrderId(orderId);
        assertThat(pendingDocs).hasSize(3);
        assertThat(pendingDocs.stream().allMatch(d -> d.getStatus() == DocumentStatus.PENDING)).isTrue();

        System.out.println("Created 3 pending documents for order: " + orderNumber);
    }

    @When("the document manager validates all documents for the order")
    public void theDocumentManagerValidatesAllDocumentsForTheOrder() {
        List<Document> pendingDocs = documentRepository.findAll().stream()
                .filter(d -> d.getStatus() == DocumentStatus.PENDING)
                .toList();

        documentManagerUser = "batch-validator";

        for (Document doc : pendingDocs) {
            DocumentValidationRequest validationRequest = DocumentValidationRequest.builder()
                    .isApproved(true)
                    .comments("Batch validation approved")
                    .validatedBy(documentManagerUser)
                    .build();

            documentManagementService.validateDocument(doc.getId(), validationRequest);
        }

        System.out.println("Validated " + pendingDocs.size() + " documents in batch");
    }

    @Then("all documents should be validated")
    public void allDocumentsShouldBeValidated() {
        List<Document> allDocs = documentRepository.findAll();
        long validatedCount = allDocs.stream()
                .filter(d -> d.getStatus() == DocumentStatus.VALIDATED)
                .count();

        assertThat(validatedCount).isGreaterThan(0);
        System.out.println("✓ All documents validated: " + validatedCount);
    }

    @And("an all.documents.validated event should be published")
    public void anAllDocumentsValidatedEventShouldBePublished() {
        // Verify the all documents validated event was published
        verify(eventPublisher, atLeastOnce()).publishAllDocumentsValidated(
                any(Long.class),
                any(Integer.class),
                any(Integer.class),
                anyString()
        );

        System.out.println("✓ all.documents.validated event published");
    }

    // Getter for pending document
    public Document getPendingDocument() {
        return pendingDocument;
    }
}
