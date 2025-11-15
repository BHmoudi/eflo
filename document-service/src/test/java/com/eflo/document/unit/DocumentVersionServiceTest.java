package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.service.DocumentVersionService;
import com.eflo.document.storage.MinIOStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentVersionService Unit Tests")
class DocumentVersionServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MinIOStorageService storageService;

    @InjectMocks
    private DocumentVersionService versionService;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = Document.builder()
                .id(1L)
                .documentUuid(UUID.randomUUID())
                .documentType(DocumentType.builder().id(1L).build())
                .version(1)
                .isLatestVersion(true)
                .originalFilename("test.pdf")
                .storageBucket("documents")
                .storagePath("/path/test.pdf")
                .customMetadata(new HashMap<>())
                .tags(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("createNewVersion - success")
    void testCreateNewVersion_Success() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test_v2.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("storagePath", "/path/test_v2.pdf");
        uploadResult.put("fileHash", "hash123");
        when(storageService.uploadFile(any(), any(), any())).thenReturn(uploadResult);

        Document newVersion = testDocument.toBuilder()
                .id(2L)
                .version(2)
                .isLatestVersion(true)
                .parentDocument(testDocument)
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(newVersion);

        // Act
        Document result = versionService.createNewVersion(1L, file, "testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(2);
        verify(documentRepository, times(2)).save(any(Document.class));
    }

    @Test
    @DisplayName("revertToVersion - success")
    void testRevertToVersion_Success() throws Exception {
        // Arrange
        Document version1 = testDocument.toBuilder().version(1).build();
        Document version2 = testDocument.toBuilder().id(2L).version(2).isLatestVersion(true).build();

        when(documentRepository.findById(anyLong())).thenReturn(Optional.of(version1), Optional.of(version2));
        when(documentRepository.findAll()).thenReturn(Arrays.asList(version1, version2));
        when(storageService.downloadFile(any(), any())).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(documentRepository.save(any(Document.class))).thenReturn(version1);

        // Act
        Document result = versionService.revertToVersion(1L, 1, "testuser");

        // Assert
        assertThat(result).isNotNull();
        verify(documentRepository, atLeast(2)).save(any(Document.class));
    }

    @Test
    @DisplayName("compareVersions - differences")
    void testCompareVersions() {
        // Arrange
        Document v1 = testDocument.toBuilder().version(1).fileHash("hash1").build();
        Document v2 = testDocument.toBuilder().id(2L).version(2).fileHash("hash2").build();

        when(documentRepository.findById(anyLong())).thenReturn(Optional.of(v1));
        when(documentRepository.findAll()).thenReturn(Arrays.asList(v1, v2));

        // Act
        Map<String, Object> result = versionService.compareVersions(1L, 1, 2);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("version1", "version2", "differences");
    }

    @Test
    @DisplayName("archiveOldVersions - keep N versions")
    void testArchiveOldVersions() {
        // Arrange
        Document v1 = testDocument.toBuilder().version(1).build();
        Document v2 = testDocument.toBuilder().id(2L).version(2).build();
        Document v3 = testDocument.toBuilder().id(3L).version(3).isLatestVersion(true).build();

        when(documentRepository.findById(anyLong())).thenReturn(Optional.of(v1));
        when(documentRepository.findAll()).thenReturn(Arrays.asList(v1, v2, v3));

        // Act
        versionService.archiveOldVersions(1L, 2);

        // Assert
        verify(documentRepository).save(argThat(doc -> doc.isArchived()));
    }
}
