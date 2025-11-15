package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.scanner.ClamAVScanner;
import com.eflo.document.scanner.FileMetadataExtractor;
import com.eflo.document.scanner.ScanResult;
import com.eflo.document.scanner.VirusScanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VirusScanningService Unit Tests")
class VirusScanningServiceTest {

    @Mock
    private ClamAVScanner clamAVScanner;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileMetadataExtractor metadataExtractor;

    @InjectMocks
    private VirusScanningService scanningService;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = Document.builder()
                .id(1L)
                .originalFilename("test.pdf")
                .storagePath("/path/test.pdf")
                .build();
    }

    @Test
    @DisplayName("scanDocument - clean result")
    void testScanDocument_Clean() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        ScanResult cleanResult = ScanResult.clean("test.pdf");
        when(clamAVScanner.scanWithClamAV(any(InputStream.class), anyString()))
                .thenReturn(cleanResult);

        // Act
        ScanResult result = scanningService.scanDocument(inputStream, "test.pdf");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSafe()).isTrue();
        assertThat(result.getStatus()).isEqualTo(VirusScanStatus.CLEAN);
        verify(clamAVScanner).scanWithClamAV(any(InputStream.class), eq("test.pdf"));
    }

    @Test
    @DisplayName("scanDocument - infected result")
    void testScanDocument_Infected() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        ScanResult infectedResult = ScanResult.infected("test.pdf", "EICAR-Test");
        when(clamAVScanner.scanWithClamAV(any(InputStream.class), anyString()))
                .thenReturn(infectedResult);

        // Act
        ScanResult result = scanningService.scanDocument(inputStream, "test.pdf");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSafe()).isFalse();
        assertThat(result.getStatus()).isEqualTo(VirusScanStatus.INFECTED);
        assertThat(result.getThreatName()).isEqualTo("EICAR-Test");
    }

    @Test
    @DisplayName("scanDocument - scan failure")
    void testScanDocument_Failure() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        when(clamAVScanner.scanWithClamAV(any(InputStream.class), anyString()))
                .thenThrow(new IOException("Scan failed"));

        // Act
        ScanResult result = scanningService.scanDocument(inputStream, "test.pdf");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(VirusScanStatus.FAILED);
    }

    @Test
    @DisplayName("scanBatch - multiple files")
    void testScanBatch() {
        // Arrange
        List<String> filePaths = Arrays.asList("/path/file1.pdf", "/path/file2.pdf");
        ScanResult cleanResult = ScanResult.clean("file.pdf");

        when(clamAVScanner.scanWithClamAV(any(InputStream.class), anyString()))
                .thenReturn(cleanResult);

        // Act
        List<ScanResult> results = scanningService.scanBatch(filePaths);

        // Assert
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("rescanDocument - success")
    void testRescanDocument() throws IOException {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        ScanResult cleanResult = ScanResult.clean("test.pdf");
        when(clamAVScanner.scanWithClamAV(any(InputStream.class), anyString()))
                .thenReturn(cleanResult);

        // Act
        ScanResult result = scanningService.rescanDocument(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(documentRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("isScannerAvailable - check")
    void testIsScannerAvailable() {
        // Arrange
        when(clamAVScanner.isClamAVAvailable()).thenReturn(true);

        // Act
        boolean result = scanningService.isScannerAvailable();

        // Assert
        assertThat(result).isTrue();
        verify(clamAVScanner).isClamAVAvailable();
    }
}
