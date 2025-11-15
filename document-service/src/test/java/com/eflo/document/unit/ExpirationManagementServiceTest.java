package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentEventPublisher;
import com.eflo.document.service.ExpirationManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpirationManagementService Unit Tests")
class ExpirationManagementServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private ExpirationManagementService expirationService;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = Document.builder()
                .id(1L)
                .documentUuid(UUID.randomUUID())
                .documentType(DocumentType.builder().id(1L).build())
                .expirationDate(LocalDate.now().plusDays(10))
                .status(DocumentStatus.VALIDATED)
                .build();
    }

    @Test
    @DisplayName("checkExpiringDocuments - scheduled job")
    void testCheckExpiringDocuments() {
        // Arrange
        when(documentTypeRepository.findWithExpiration()).thenReturn(Collections.emptyList());

        // Act
        expirationService.checkExpiringDocuments();

        // Assert
        verify(documentTypeRepository).findWithExpiration();
    }

    @Test
    @DisplayName("getExpiringDocuments - filtering")
    void testGetExpiringDocuments() {
        // Arrange
        when(documentRepository.findAll()).thenReturn(Arrays.asList(testDocument));

        // Act
        List<Document> result = expirationService.getExpiringDocuments(30);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("setDocumentExpiration - update")
    void testSetDocumentExpiration() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        // Act
        expirationService.setDocumentExpiration(1L, futureDate);

        // Assert
        verify(documentRepository).save(argThat(doc ->
                futureDate.equals(doc.getExpirationDate())
        ));
    }

    @Test
    @DisplayName("archiveExpiredDocuments - auto-archive")
    void testArchiveExpiredDocuments() {
        // Arrange
        Document expired = testDocument.toBuilder()
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        // Act
        expirationService.archiveExpiredDocuments(Arrays.asList(expired));

        // Assert
        verify(documentRepository).save(argThat(doc ->
                doc.getStatus() == DocumentStatus.EXPIRED && doc.isArchived()
        ));
        verify(eventPublisher).publishEvent(eq("DOCUMENT_EXPIRED"), anyMap());
    }
}
