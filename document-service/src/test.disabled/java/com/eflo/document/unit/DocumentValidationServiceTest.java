package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.entity.DocumentValidationRule;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.ValidationResult;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentValidationRuleRepository;
import com.eflo.document.service.DocumentTypeService;
import com.eflo.document.service.DocumentValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentValidationService Unit Tests")
class DocumentValidationServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentValidationRuleRepository validationRuleRepository;

    @Mock
    private DocumentTypeService documentTypeService;

    @InjectMocks
    private DocumentValidationService validationService;

    private Document testDocument;
    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
                .id(1L)
                .typeCode("INVOICE")
                .allowedFormats(Arrays.asList("pdf", "jpg"))
                .maxFileSizeMb(10)
                .hasExpiration(false)
                .build();

        testDocument = Document.builder()
                .id(1L)
                .documentType(testDocumentType)
                .originalFilename("test.pdf")
                .fileExtension("pdf")
                .fileSizeBytes(1024L)
                .orderId(100L)
                .uploadedBy("testuser")
                .expirationDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("validateDocument - valid document")
    void testValidateDocument_Valid() {
        // Arrange
        when(validationRuleRepository.findByDocumentTypeIdAndIsActiveTrueOrderByExecutionOrder(1L))
                .thenReturn(Collections.emptyList());

        // Act
        ValidationResult result = validationService.validateDocument(testDocument, testDocumentType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("validateDocument - invalid file format")
    void testValidateDocument_InvalidFormat() {
        // Arrange
        Document invalidDoc = testDocument.toBuilder()
                .fileExtension("exe")
                .build();

        when(validationRuleRepository.findByDocumentTypeIdAndIsActiveTrueOrderByExecutionOrder(1L))
                .thenReturn(Collections.emptyList());

        // Act
        ValidationResult result = validationService.validateDocument(invalidDoc, testDocumentType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("validateFileFormat - allowed format")
    void testValidateFileFormat_Allowed() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        // Act
        ValidationResult result = validationService.validateFileFormat(file, testDocumentType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("validateFileFormat - not allowed format")
    void testValidateFileFormat_NotAllowed() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("document.exe");

        // Act
        ValidationResult result = validationService.validateFileFormat(file, testDocumentType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    @DisplayName("validateFileSize - within limit")
    void testValidateFileSize_WithinLimit() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L * 1024L); // 1MB

        // Act
        ValidationResult result = validationService.validateFileSize(file, testDocumentType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("validateFileSize - exceeds limit")
    void testValidateFileSize_ExceedsLimit() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(20L * 1024L * 1024L); // 20MB

        // Act
        ValidationResult result = validationService.validateFileSize(file, testDocumentType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
    }

    @Test
    @DisplayName("validateOrderRequirements - complete")
    void testValidateOrderRequirements_Complete() {
        // Arrange
        List<DocumentType> mandatory = Arrays.asList(testDocumentType);
        when(documentTypeService.getMandatoryDocumentTypes(null)).thenReturn(mandatory);

        Document validatedDoc = testDocument.toBuilder()
                .status(DocumentStatus.VALIDATED)
                .build();
        when(documentRepository.findByOrderId(100L)).thenReturn(Arrays.asList(validatedDoc));

        // Act
        ValidationResult result = validationService.validateOrderRequirements(100L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("validateOrderRequirements - incomplete")
    void testValidateOrderRequirements_Incomplete() {
        // Arrange
        List<DocumentType> mandatory = Arrays.asList(testDocumentType);
        when(documentTypeService.getMandatoryDocumentTypes(null)).thenReturn(mandatory);
        when(documentRepository.findByOrderId(100L)).thenReturn(Collections.emptyList());

        // Act
        ValidationResult result = validationService.validateOrderRequirements(100L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getBlockingIssues()).isNotEmpty();
    }

    @Test
    @DisplayName("executeValidationRules - blocking and non-blocking")
    void testExecuteValidationRules() {
        // Arrange
        DocumentValidationRule rule1 = DocumentValidationRule.builder()
                .id(1L)
                .ruleName("Rule 1")
                .ruleType("FORMAT_CHECK")
                .build();

        List<DocumentValidationRule> rules = Arrays.asList(rule1);

        // Act
        ValidationResult result = validationService.executeValidationRules(testDocument, rules);

        // Assert
        assertThat(result).isNotNull();
    }
}
