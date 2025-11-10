package com.eflo.document.unit;

import com.eflo.document.config.FileUploadConfig;
import com.eflo.document.storage.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileValidator Unit Tests")
class FileValidatorTest {

    @Mock
    private FileUploadConfig fileUploadConfig;

    @InjectMocks
    private FileValidator fileValidator;

    @BeforeEach
    void setUp() {
        when(fileUploadConfig.getMaxFileSizeMb()).thenReturn(10);
        when(fileUploadConfig.getMaxFileSizeBytes()).thenReturn(10L * 1024L * 1024L);
        when(fileUploadConfig.getAllowedExtensions()).thenReturn(Arrays.asList("pdf", "jpg", "png"));
        when(fileUploadConfig.getAllowedMimeTypes())
                .thenReturn(Arrays.asList("application/pdf", "image/jpeg", "image/png"));
    }

    @Test
    @DisplayName("validateFile - valid file")
    void testValidateFile_Valid() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("document.pdf");
        when(file.getSize()).thenReturn(1024L * 1024L);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{0x25, 0x50, 0x44, 0x46}));
        when(fileUploadConfig.isAllowedExtension("pdf")).thenReturn(true);
        when(fileUploadConfig.isAllowedMimeType(anyString())).thenReturn(true);

        // Act
        FileValidator.ValidationResult result = fileValidator.validateFile(file);

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("isAllowedExtension - allowed")
    void testIsAllowedExtension_Allowed() {
        // Arrange
        when(fileUploadConfig.isAllowedExtension("pdf")).thenReturn(true);

        // Act
        boolean result = fileValidator.isAllowedExtension("pdf");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAllowedExtension - blocked")
    void testIsAllowedExtension_Blocked() {
        // Arrange
        when(fileUploadConfig.isAllowedExtension("exe")).thenReturn(false);

        // Act
        boolean result = fileValidator.isAllowedExtension("exe");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isAllowedMimeType - validation")
    void testIsAllowedMimeType() {
        // Arrange
        when(fileUploadConfig.isAllowedMimeType("application/pdf")).thenReturn(true);

        // Act
        boolean result = fileValidator.isAllowedMimeType("application/pdf");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkFileSize - within limit")
    void testCheckFileSize_WithinLimit() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L * 1024L);

        // Act
        boolean result = fileValidator.checkFileSize(file);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkFileSize - exceeds limit")
    void testCheckFileSize_ExceedsLimit() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(20L * 1024L * 1024L);

        // Act
        boolean result = fileValidator.checkFileSize(file);

        // Assert
        assertThat(result).isFalse();
    }
}
