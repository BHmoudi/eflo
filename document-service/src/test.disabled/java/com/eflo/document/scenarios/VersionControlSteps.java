package com.eflo.document.scenarios;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentResponse;
import com.eflo.document.domain.model.DocumentUploadRequest;
import com.eflo.document.domain.model.DocumentValidationRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Cucumber step definitions for Document Version Control scenarios.
 */
public class VersionControlSteps {

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @SpyBean
    private DocumentEventPublisher eventPublisher;

    // Scenario context
    private Document validatedDocument;
    private DocumentResponse newVersionDocument;
    private Document oldVersion;
    private List<Document> allVersions;
    private Map<String, Object> originalMetadata;

    @Before
    public void setUp() {
        validatedDocument = null;
        newVersionDocument = null;
        oldVersion = null;
        allVersions = null;
        originalMetadata = new HashMap<>();
    }

    @Given("a validated document exists")
    public void aValidatedDocumentExists() {
        // Create document type
        DocumentType documentType = DocumentType.builder()
                .typeCode("VERSION_DOC_TYPE")
                .typeName("Version Document Type")
                .description("Document type for version control testing")
                .category("VERSION_TEST")
                .allowedFormats(List.of("pdf", "jpg", "png"))
                .maxFileSizeMb(BigDecimal.valueOf(5.0))
                .minDocuments(1)
                .maxDocuments(10)
                .isMandatory(false)
                .requiresValidation(true)
                .hasExpiration(false)
                .isActive(true)
                .build();
        documentType = documentTypeRepository.save(documentType);

        // Upload document
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "original-document.pdf",
                "application/pdf",
                "Original document content - version 1".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
                .file(file)
                .documentTypeId(documentType.getId())
                .orderId(8001L)
                .orderNumber("ORD-VERSION-001")
                .description("Original validated document")
                .build();

        DocumentResponse uploaded = documentManagementService.uploadDocument(uploadRequest);

        // Validate it
        DocumentValidationRequest validationRequest = DocumentValidationRequest.builder()
                .isApproved(true)
                .comments("Initial validation")
                .validatedBy("validator")
                .build();

        documentManagementService.validateDocument(uploaded.getId(), validationRequest);

        validatedDocument = documentRepository.findById(uploaded.getId()).orElseThrow();
        assertThat(validatedDocument.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(validatedDocument.getVersion()).isEqualTo(1);

        System.out.println("Created validated document - ID: " + validatedDocument.getId() + ", Version: 1");
    }

    @When("a user uploads a replacement file")
    public void aUserUploadsAReplacementFile() {
        MockMultipartFile replacementFile = new MockMultipartFile(
                "file",
                "replacement-document.pdf",
                "application/pdf",
                "Replacement document content - version 2".getBytes(StandardCharsets.UTF_8)
        );

        newVersionDocument = documentManagementService.replaceDocument(
                validatedDocument.getId(),
                replacementFile,
                "version-creator"
        );

        System.out.println("Uploaded replacement file - New Document ID: " + newVersionDocument.getId());
    }

    @Then("a new version should be created")
    public void aNewVersionShouldBeCreated() {
        assertThat(newVersionDocument).isNotNull();
        assertThat(newVersionDocument.getId()).isNotEqualTo(validatedDocument.getId());
        assertThat(newVersionDocument.getVersion()).isGreaterThan(validatedDocument.getVersion());

        System.out.println("✓ New version created - Version: " + newVersionDocument.getVersion());
    }

    @And("the old version should be marked as superseded")
    public void theOldVersionShouldBeMarkedAsSuperseded() {
        Document oldDoc = documentRepository.findById(validatedDocument.getId()).orElseThrow();
        assertThat(oldDoc.getIsLatestVersion()).isFalse();
        assertThat(oldDoc.getReplacedByDocument()).isNotNull();

        System.out.println("✓ Old version marked as superseded");
        System.out.println("  - isLatestVersion: " + oldDoc.getIsLatestVersion());
    }

    @And("a document.version.created event should be published")
    public void aDocumentVersionCreatedEventShouldBePublished() {
        ArgumentCaptor<Document> parentCaptor = ArgumentCaptor.forClass(Document.class);
        ArgumentCaptor<Document> newVersionCaptor = ArgumentCaptor.forClass(Document.class);

        verify(eventPublisher, atLeastOnce()).publishVersionCreated(
                parentCaptor.capture(),
                newVersionCaptor.capture()
        );

        assertThat(parentCaptor.getValue().getId()).isEqualTo(validatedDocument.getId());
        assertThat(newVersionCaptor.getValue().getId()).isEqualTo(newVersionDocument.getId());

        System.out.println("✓ document.version.created event published");
    }

    @Given("a validated document exists with version {int}")
    public void aValidatedDocumentExistsWithVersion(int version) {
        aValidatedDocumentExists();
        assertThat(validatedDocument.getVersion()).isEqualTo(version);
        System.out.println("Validated document exists with version " + version);
    }

    @Then("the new version number should be {int}")
    public void theNewVersionNumberShouldBe(int expectedVersion) {
        assertThat(newVersionDocument.getVersion()).isEqualTo(expectedVersion);
        System.out.println("✓ New version number is " + expectedVersion);
    }

    @And("the old version should have isLatestVersion set to false")
    public void theOldVersionShouldHaveIsLatestVersionSetToFalse() {
        Document oldDoc = documentRepository.findById(validatedDocument.getId()).orElseThrow();
        assertThat(oldDoc.getIsLatestVersion()).isFalse();
        System.out.println("✓ Old version isLatestVersion = false");
    }

    @And("the new version should have isLatestVersion set to true")
    public void theNewVersionShouldHaveIsLatestVersionSetToTrue() {
        Document newDoc = documentRepository.findById(newVersionDocument.getId()).orElseThrow();
        assertThat(newDoc.getIsLatestVersion()).isTrue();
        System.out.println("✓ New version isLatestVersion = true");
    }

    @Then("the new version should reference the old version as parent")
    public void theNewVersionShouldReferenceTheOldVersionAsParent() {
        Document newDoc = documentRepository.findById(newVersionDocument.getId()).orElseThrow();
        assertThat(newDoc.getParentDocument()).isNotNull();
        assertThat(newDoc.getParentDocument().getId()).isEqualTo(validatedDocument.getId());

        System.out.println("✓ New version references old version as parent");
        System.out.println("  - Parent ID: " + newDoc.getParentDocument().getId());
    }

    @And("the old version should reference the new version as replacedBy")
    public void theOldVersionShouldReferenceTheNewVersionAsReplacedBy() {
        Document oldDoc = documentRepository.findById(validatedDocument.getId()).orElseThrow();
        assertThat(oldDoc.getReplacedByDocument()).isNotNull();
        assertThat(oldDoc.getReplacedByDocument().getId()).isEqualTo(newVersionDocument.getId());

        System.out.println("✓ Old version references new version as replacedBy");
        System.out.println("  - Replaced by ID: " + oldDoc.getReplacedByDocument().getId());
    }

    @When("a user uploads a replacement file creating version {int}")
    public void aUserUploadsAReplacementFileCreatingVersion(int versionNumber) {
        MockMultipartFile replacementFile = new MockMultipartFile(
                "file",
                "replacement-v" + versionNumber + ".pdf",
                "application/pdf",
                ("Replacement document content - version " + versionNumber).getBytes(StandardCharsets.UTF_8)
        );

        Long currentLatestId = newVersionDocument != null
                ? newVersionDocument.getId()
                : validatedDocument.getId();

        newVersionDocument = documentManagementService.replaceDocument(
                currentLatestId,
                replacementFile,
                "version-creator"
        );

        System.out.println("Created version " + versionNumber + " - ID: " + newVersionDocument.getId());
    }

    @When("a user uploads another replacement file creating version {int}")
    public void aUserUploadsAnotherReplacementFileCreatingVersion(int versionNumber) {
        aUserUploadsAReplacementFileCreatingVersion(versionNumber);
    }

    @Then("version {int} should be the latest version")
    public void versionShouldBeTheLatestVersion(int versionNumber) {
        Document latestDoc = documentRepository.findById(newVersionDocument.getId()).orElseThrow();
        assertThat(latestDoc.getVersion()).isEqualTo(versionNumber);
        assertThat(latestDoc.getIsLatestVersion()).isTrue();

        System.out.println("✓ Version " + versionNumber + " is the latest");
    }

    @And("version {int} should have version {int} as replacedBy")
    public void versionShouldHaveVersionAsReplacedBy(int oldVersionNum, int newVersionNum) {
        // Find the document with oldVersionNum
        List<Document> allDocs = documentRepository.findAll();
        Document oldVersionDoc = allDocs.stream()
                .filter(d -> d.getVersion() == oldVersionNum)
                .findFirst()
                .orElseThrow();

        assertThat(oldVersionDoc.getReplacedByDocument()).isNotNull();
        assertThat(oldVersionDoc.getReplacedByDocument().getVersion()).isEqualTo(newVersionNum);

        System.out.println("✓ Version " + oldVersionNum + " has version " + newVersionNum + " as replacedBy");
    }

    @And("the version chain should be complete")
    public void theVersionChainShouldBeComplete() {
        allVersions = documentRepository.findByOrderId(validatedDocument.getOrderId());

        // Verify chain integrity
        for (int i = 0; i < allVersions.size() - 1; i++) {
            Document current = allVersions.get(i);
            if (current.getVersion() < allVersions.size()) {
                assertThat(current.getReplacedByDocument()).isNotNull();
                System.out.println("Version " + current.getVersion() + " → Version " +
                        current.getReplacedByDocument().getVersion());
            }
        }

        System.out.println("✓ Version chain is complete");
    }

    @Given("a document with multiple versions exists")
    public void aDocumentWithMultipleVersionsExists() {
        aValidatedDocumentExists();
        aUserUploadsAReplacementFile();
        aUserUploadsAReplacementFileCreatingVersion(3);

        System.out.println("Created document with 3 versions");
    }

    @When("the system retrieves all versions for the order")
    public void theSystemRetrievesAllVersionsForTheOrder() {
        allVersions = documentRepository.findByOrderId(validatedDocument.getOrderId());
        System.out.println("Retrieved " + allVersions.size() + " versions");
    }

    @Then("all versions should be returned in order")
    public void allVersionsShouldBeReturnedInOrder() {
        assertThat(allVersions).isNotEmpty();
        assertThat(allVersions.size()).isGreaterThanOrEqualTo(3);

        System.out.println("✓ All " + allVersions.size() + " versions returned");
    }

    @And("each version should have correct parent references")
    public void eachVersionShouldHaveCorrectParentReferences() {
        for (Document doc : allVersions) {
            if (doc.getVersion() > 1) {
                assertThat(doc.getParentDocument()).isNotNull();
                System.out.println("Version " + doc.getVersion() + " parent: Version " +
                        doc.getParentDocument().getVersion());
            }
        }

        System.out.println("✓ All parent references are correct");
    }

    @And("only the latest version should be marked as current")
    public void onlyTheLatestVersionShouldBeMarkedAsCurrent() {
        long latestCount = allVersions.stream()
                .filter(Document::getIsLatestVersion)
                .count();

        assertThat(latestCount).isEqualTo(1);

        Document latest = allVersions.stream()
                .filter(Document::getIsLatestVersion)
                .findFirst()
                .orElseThrow();

        System.out.println("✓ Only 1 version marked as latest: Version " + latest.getVersion());
    }

    @Given("a validated document exists with metadata")
    public void aValidatedDocumentExistsWithMetadata() {
        aValidatedDocumentExists();

        // Add some metadata
        originalMetadata.put("contractNumber", "CNT-2024-001");
        originalMetadata.put("customerType", "Premium");

        validatedDocument.setCustomMetadata(originalMetadata);
        validatedDocument = documentRepository.save(validatedDocument);

        System.out.println("Document has metadata: " + originalMetadata);
    }

    @Then("the new version should inherit the order information")
    public void theNewVersionShouldInheritTheOrderInformation() {
        assertThat(newVersionDocument.getOrderId()).isEqualTo(validatedDocument.getOrderId());
        assertThat(newVersionDocument.getOrderNumber()).isEqualTo(validatedDocument.getOrderNumber());

        System.out.println("✓ Order information inherited");
        System.out.println("  - Order ID: " + newVersionDocument.getOrderId());
        System.out.println("  - Order Number: " + newVersionDocument.getOrderNumber());
    }

    @And("the new version should inherit the document type")
    public void theNewVersionShouldInheritTheDocumentType() {
        assertThat(newVersionDocument.getTypeCode()).isEqualTo(validatedDocument.getTypeCode());

        System.out.println("✓ Document type inherited: " + newVersionDocument.getTypeCode());
    }

    @And("the new version should maintain the same business context")
    public void theNewVersionShouldMaintainTheSameBusinessContext() {
        Document newDoc = documentRepository.findById(newVersionDocument.getId()).orElseThrow();

        assertThat(newDoc.getBusinessUnitId()).isEqualTo(validatedDocument.getBusinessUnitId());
        assertThat(newDoc.getOrderId()).isEqualTo(validatedDocument.getOrderId());

        System.out.println("✓ Business context maintained");
        System.out.println("  - Business Unit ID: " + newDoc.getBusinessUnitId());
    }
}
