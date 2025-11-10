package com.eflo.document.unit;

import com.eflo.document.storage.FileProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileProcessor Unit Tests")
class FileProcessorTest {

    @InjectMocks
    private FileProcessor fileProcessor;

    @Test
    @DisplayName("calculateFileHash - SHA-256")
    void testCalculateFileHash() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        // Act
        String hash = fileProcessor.calculateFileHash(file);

        // Assert
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64); // SHA-256 produces 64 hex characters
    }

    @Test
    @DisplayName("extractFileMetadata - PDF")
    void testExtractFileMetadata_PDF() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act
        Map<String, String> metadata = fileProcessor.extractFileMetadata(file);

        // Assert
        assertThat(metadata).isNotNull();
        assertThat(metadata).containsKey("originalFilename");
        assertThat(metadata).containsKey("fileSize");
    }

    @Test
    @DisplayName("calculateHash - from InputStream")
    void testCalculateHash() throws IOException {
        // Arrange
        ByteArrayInputStream stream = new ByteArrayInputStream("test".getBytes());

        // Act
        String hash = fileProcessor.calculateHash(stream);

        // Assert
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64);
    }

    @Test
    @DisplayName("shouldCompress - true for large files")
    void testShouldCompress_Large() {
        // Arrange
        long largeSize = 10L * 1024L * 1024L; // 10MB

        // Act
        boolean result = fileProcessor.shouldCompress(largeSize);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldCompress - false for small files")
    void testShouldCompress_Small() {
        // Arrange
        long smallSize = 1024L; // 1KB

        // Act
        boolean result = fileProcessor.shouldCompress(smallSize);

        // Assert
        assertThat(result).isFalse();
    }
}
