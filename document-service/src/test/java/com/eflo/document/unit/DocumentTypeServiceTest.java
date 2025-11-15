package com.eflo.document.unit;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.model.DocumentTypeCreateRequest;
import com.eflo.document.domain.model.DocumentTypeUpdateRequest;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.service.DocumentTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentTypeService Unit Tests")
class DocumentTypeServiceTest {

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentTypeService documentTypeService;

    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
                .id(1L)
                .typeCode("INVOICE")
                .typeName("Invoice")
                .description("Invoice document")
                .category(DocumentCategory.FINANCIAL)
                .isMandatory(true)
                .isActive(true)
                .allowedFormats(Arrays.asList("pdf", "jpg"))
                .build();
    }

    @Test
    @DisplayName("createDocumentType - success")
    void testCreateDocumentType_Success() {
        // Arrange
        DocumentTypeCreateRequest request = DocumentTypeCreateRequest.builder()
                .typeCode("INVOICE")
                .typeName("Invoice")
                .description("Invoice document")
                .category(DocumentCategory.FINANCIAL)
                .isMandatory(true)
                .allowedFormats(Arrays.asList("pdf", "jpg"))
                .build();

        when(documentTypeRepository.existsByTypeCode("INVOICE")).thenReturn(false);
        when(documentTypeRepository.save(any(DocumentType.class))).thenReturn(testDocumentType);

        // Act
        DocumentType result = documentTypeService.createDocumentType(request, "testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTypeCode()).isEqualTo("INVOICE");

        verify(documentTypeRepository).existsByTypeCode("INVOICE");
        verify(documentTypeRepository).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("createDocumentType - duplicate code")
    void testCreateDocumentType_DuplicateCode() {
        // Arrange
        DocumentTypeCreateRequest request = DocumentTypeCreateRequest.builder()
                .typeCode("INVOICE")
                .typeName("Invoice")
                .category(DocumentCategory.FINANCIAL)
                .allowedFormats(Arrays.asList("pdf"))
                .build();

        when(documentTypeRepository.existsByTypeCode("INVOICE")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> documentTypeService.createDocumentType(request, "testuser"))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessageContaining("already exists");

        verify(documentTypeRepository).existsByTypeCode("INVOICE");
        verify(documentTypeRepository, never()).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("updateDocumentType - success")
    void testUpdateDocumentType_Success() {
        // Arrange
        DocumentTypeUpdateRequest request = DocumentTypeUpdateRequest.builder()
                .typeName("Updated Invoice")
                .description("Updated description")
                .build();

        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(documentTypeRepository.save(any(DocumentType.class))).thenReturn(testDocumentType);

        // Act
        DocumentType result = documentTypeService.updateDocumentType(1L, request, "testuser");

        // Assert
        assertThat(result).isNotNull();
        verify(documentTypeRepository).findById(1L);
        verify(documentTypeRepository).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("updateDocumentType - not found")
    void testUpdateDocumentType_NotFound() {
        // Arrange
        DocumentTypeUpdateRequest request = DocumentTypeUpdateRequest.builder()
                .typeName("Updated")
                .build();

        when(documentTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentTypeService.updateDocumentType(999L, request, "testuser"))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessageContaining("not found");

        verify(documentTypeRepository).findById(999L);
        verify(documentTypeRepository, never()).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("deleteDocumentType - success")
    void testDeleteDocumentType_Success() {
        // Arrange
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(documentRepository.countByOrderIdAndDocumentTypeId(null, 1L)).thenReturn(0L);

        // Act
        documentTypeService.deleteDocumentType(1L);

        // Assert
        verify(documentTypeRepository).findById(1L);
        verify(documentRepository).countByOrderIdAndDocumentTypeId(null, 1L);
        verify(documentTypeRepository).delete(testDocumentType);
    }

    @Test
    @DisplayName("deleteDocumentType - has documents")
    void testDeleteDocumentType_HasDocuments() {
        // Arrange
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(documentRepository.countByOrderIdAndDocumentTypeId(null, 1L)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> documentTypeService.deleteDocumentType(1L))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessageContaining("documents exist");

        verify(documentTypeRepository, never()).delete(any(DocumentType.class));
    }

    @Test
    @DisplayName("duplicateDocumentType - success")
    void testDuplicateDocumentType_Success() {
        // Arrange
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(documentTypeRepository.existsByTypeCode("INVOICE_COPY")).thenReturn(false);
        when(documentTypeRepository.save(any(DocumentType.class))).thenReturn(testDocumentType);

        // Act
        DocumentType result = documentTypeService.duplicateDocumentType(1L, "INVOICE_COPY", "testuser");

        // Assert
        assertThat(result).isNotNull();
        verify(documentTypeRepository).findById(1L);
        verify(documentTypeRepository).existsByTypeCode("INVOICE_COPY");
        verify(documentTypeRepository).save(argThat(dt ->
                "INVOICE_COPY".equals(dt.getTypeCode())
        ));
    }

    @Test
    @DisplayName("getMandatoryDocumentTypes - filtering")
    void testGetMandatoryDocumentTypes() {
        // Arrange
        List<DocumentType> mandatory = Arrays.asList(testDocumentType);
        when(documentTypeRepository.findByIsMandatoryTrue()).thenReturn(mandatory);

        // Act
        List<DocumentType> result = documentTypeService.getMandatoryDocumentTypes(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(documentTypeRepository).findByIsMandatoryTrue();
    }
}
