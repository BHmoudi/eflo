package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.model.*;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.exception.DocumentNotFoundException;
import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.exception.DocumentValidationException;
import com.eflo.document.mapper.DocumentMapper;
import com.eflo.document.scanner.ScanResult;
import com.eflo.document.scanner.VirusScanningService;
import com.eflo.document.service.DocumentEventPublisher;
import com.eflo.document.service.DocumentManagementService;
import com.eflo.document.storage.FileProcessor;
import com.eflo.document.storage.FileValidator;
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
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentManagementService Unit Tests")
class DocumentManagementServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private MinIOStorageService minIOStorageService;

    @Mock
    private FileProcessor fileProcessor;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private VirusScanningService virusScanningService;

    @Mock
    private DocumentEventPublisher eventPublisher;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentManagementService documentManagementService;

    private DocumentType testDocumentType;
    private Document testDocument;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testDocumentType = DocumentType.builder()
                .id(1L)
                .typeCode("INVOICE")
                .typeName("Invoice")
                .isActive(true)
                .build();

        testDocument = Document.builder()
                .id(1L)
                .documentUuid(UUID.randomUUID())
                .documentType(testDocumentType)
                .typeCode("INVOICE")
                .orderId(100L)
                .orderNumber("ORD-001")
                .originalFilename("invoice.pdf")
                .storedFilename("stored_invoice.pdf")
                .fileExtension("pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(1024L)
                .fileHash("abc123")
                .storageBucket("documents")
                .storagePath("/orders/100/INVOICE/stored_invoice.pdf")
                .version(1)
                .isLatestVersion(true)
                .status(DocumentStatus.PENDING)
                .virusScanStatus(VirusScanStatus.CLEAN)
                .uploadedBy("testuser")
                .uploadedAt(LocalDateTime.now())
                .build();

        testFile = mock(MultipartFile.class);
        when(testFile.getOriginalFilename()).thenReturn("invoice.pdf");
        when(testFile.getContentType()).thenReturn("application/pdf");
        when(testFile.getSize()).thenReturn(1024L);
    }

    @Test
    @DisplayName("uploadDocument - success case")
    void testUploadDocument_Success() throws Exception {
        // Arrange
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(testFile)
                .documentTypeId(1L)
                .orderId(100L)
                .orderNumber("ORD-001")
                .build();

        FileValidator.ValidationResult validationResult = FileValidator.ValidationResult.valid();
        when(fileValidator.validateFile(testFile)).thenReturn(validationResult);
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(fileProcessor.calculateFileHash(testFile)).thenReturn("abc123");
        when(testFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ScanResult scanResult = ScanResult.clean("invoice.pdf");
        when(virusScanningService.scanDocument(any(InputStream.class), anyString()))
                .thenReturn(scanResult);

        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        DocumentResponse expectedResponse = DocumentResponse.builder()
                .id(1L)
                .documentUuid(testDocument.getDocumentUuid())
                .build();
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        // Act
        DocumentResponse result = documentManagementService.uploadDocument(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(fileValidator).validateFile(testFile);
        verify(documentTypeRepository).findById(1L);
        verify(fileProcessor).calculateFileHash(testFile);
        verify(minIOStorageService).createBucketIfNotExists("documents");
        verify(minIOStorageService).uploadFile(eq("documents"), anyString(), any(InputStream.class),
                eq("application/pdf"), eq(1024L));
        verify(virusScanningService).scanDocument(any(InputStream.class), eq("invoice.pdf"));
        verify(documentRepository).save(any(Document.class));
        verify(eventPublisher).publishDocumentUploaded(any(Document.class));
        verify(eventPublisher).publishScanCompleted(any(Document.class), any(ScanResult.class));
    }

    @Test
    @DisplayName("uploadDocument - validation failure")
    void testUploadDocument_ValidationFailure() {
        // Arrange
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(testFile)
                .documentTypeId(1L)
                .orderId(100L)
                .build();

        FileValidator.ValidationResult validationResult =
                FileValidator.ValidationResult.invalid("File size exceeds limit");
        when(fileValidator.validateFile(testFile)).thenReturn(validationResult);

        // Act & Assert
        assertThatThrownBy(() -> documentManagementService.uploadDocument(request))
                .isInstanceOf(DocumentValidationException.class)
                .hasMessageContaining("File validation failed");

        verify(fileValidator).validateFile(testFile);
        verify(documentTypeRepository, never()).findById(anyLong());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("uploadDocument - document type not found")
    void testUploadDocument_DocumentTypeNotFound() {
        // Arrange
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(testFile)
                .documentTypeId(999L)
                .orderId(100L)
                .build();

        FileValidator.ValidationResult validationResult = FileValidator.ValidationResult.valid();
        when(fileValidator.validateFile(testFile)).thenReturn(validationResult);
        when(documentTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentManagementService.uploadDocument(request))
                .isInstanceOf(DocumentValidationException.class)
                .hasMessageContaining("Document type not found");

        verify(documentTypeRepository).findById(999L);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("uploadDocument - virus infected")
    void testUploadDocument_VirusInfected() throws Exception {
        // Arrange
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(testFile)
                .documentTypeId(1L)
                .orderId(100L)
                .build();

        FileValidator.ValidationResult validationResult = FileValidator.ValidationResult.valid();
        when(fileValidator.validateFile(testFile)).thenReturn(validationResult);
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(fileProcessor.calculateFileHash(testFile)).thenReturn("abc123");
        when(testFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ScanResult scanResult = ScanResult.infected("invoice.pdf", "EICAR-Test-File");
        when(virusScanningService.scanDocument(any(InputStream.class), anyString()))
                .thenReturn(scanResult);

        Document infectedDoc = testDocument.toBuilder()
                .status(DocumentStatus.QUARANTINED)
                .virusScanStatus(VirusScanStatus.INFECTED)
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(infectedDoc);

        DocumentResponse expectedResponse = DocumentResponse.builder()
                .id(1L)
                .status(DocumentStatus.QUARANTINED)
                .build();
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        // Act
        DocumentResponse result = documentManagementService.uploadDocument(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.QUARANTINED);

        verify(documentRepository).save(argThat(doc ->
                doc.getStatus() == DocumentStatus.QUARANTINED &&
                doc.getVirusScanStatus() == VirusScanStatus.INFECTED
        ));
    }

    @Test
    @DisplayName("uploadDocument - storage failure")
    void testUploadDocument_StorageFailure() throws Exception {
        // Arrange
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(testFile)
                .documentTypeId(1L)
                .orderId(100L)
                .build();

        FileValidator.ValidationResult validationResult = FileValidator.ValidationResult.valid();
        when(fileValidator.validateFile(testFile)).thenReturn(validationResult);
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(fileProcessor.calculateFileHash(testFile)).thenReturn("abc123");
        when(testFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        doThrow(new DocumentStorageException("Storage failed"))
                .when(minIOStorageService).uploadFile(anyString(), anyString(), any(InputStream.class),
                        anyString(), anyLong());

        // Act & Assert
        assertThatThrownBy(() -> documentManagementService.uploadDocument(request))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessageContaining("Storage failed");

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("downloadDocument - success")
    void testDownloadDocument_Success() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        InputStream expectedStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(minIOStorageService.downloadFile("documents", "/orders/100/INVOICE/stored_invoice.pdf"))
                .thenReturn(expectedStream);

        // Act
        InputStream result = documentManagementService.downloadDocument(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedStream);

        verify(documentRepository).findById(1L);
        verify(minIOStorageService).downloadFile("documents", "/orders/100/INVOICE/stored_invoice.pdf");
    }

    @Test
    @DisplayName("downloadDocument - not found")
    void testDownloadDocument_NotFound() {
        // Arrange
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentManagementService.downloadDocument(999L))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining("Document not found with ID: 999");

        verify(documentRepository).findById(999L);
        verify(minIOStorageService, never()).downloadFile(anyString(), anyString());
    }

    @Test
    @DisplayName("validateDocument - approve")
    void testValidateDocument_Approve() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        Document validatedDoc = testDocument.toBuilder()
                .status(DocumentStatus.VALIDATED)
                .validatedBy("validator")
                .validatedAt(LocalDateTime.now())
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(validatedDoc);

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .isApproved(true)
                .validatedBy("validator")
                .comments("Approved")
                .build();

        // Act
        DocumentValidationResponse result = documentManagementService.validateDocument(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isApproved()).isTrue();
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(result.getValidatedBy()).isEqualTo("validator");

        verify(documentRepository).findById(1L);
        verify(documentRepository).save(argThat(doc ->
                doc.getStatus() == DocumentStatus.VALIDATED &&
                "validator".equals(doc.getValidatedBy())
        ));
        verify(eventPublisher).publishDocumentValidated(any(Document.class), eq("validator"));
    }

    @Test
    @DisplayName("validateDocument - reject")
    void testValidateDocument_Reject() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        Document rejectedDoc = testDocument.toBuilder()
                .status(DocumentStatus.REJECTED)
                .validatedBy("validator")
                .validatedAt(LocalDateTime.now())
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(rejectedDoc);

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .isApproved(false)
                .validatedBy("validator")
                .comments("Missing information")
                .build();

        // Act
        DocumentValidationResponse result = documentManagementService.validateDocument(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isApproved()).isFalse();
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.REJECTED);

        verify(documentRepository).save(argThat(doc ->
                doc.getStatus() == DocumentStatus.REJECTED
        ));
        verify(eventPublisher).publishDocumentRejected(any(Document.class), eq("Missing information"));
    }

    @Test
    @DisplayName("deleteDocument - soft delete")
    void testDeleteDocument_SoftDelete() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        // Act
        documentManagementService.deleteDocument(1L, "testuser");

        // Assert
        verify(documentRepository).findById(1L);
        verify(documentRepository).save(argThat(doc -> doc.isDeleted()));
        verify(eventPublisher).publishDocumentDeleted(eq(1L), eq(100L), eq("ORD-001"),
                eq("testuser"), eq("Document deleted"));
    }

    @Test
    @DisplayName("replaceDocument - version creation")
    void testReplaceDocument_VersionCreation() throws Exception {
        // Arrange
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.getOriginalFilename()).thenReturn("invoice_v2.pdf");
        when(newFile.getContentType()).thenReturn("application/pdf");
        when(newFile.getSize()).thenReturn(2048L);
        when(newFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));

        FileValidator.ValidationResult validationResult = FileValidator.ValidationResult.valid();
        when(fileValidator.validateFile(any(MultipartFile.class))).thenReturn(validationResult);
        when(fileProcessor.calculateFileHash(any(MultipartFile.class))).thenReturn("def456");

        Document newVersion = testDocument.toBuilder()
                .id(2L)
                .version(2)
                .parentDocument(testDocument)
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(newVersion);
        when(documentRepository.findById(2L)).thenReturn(Optional.of(newVersion));

        ScanResult scanResult = ScanResult.clean("invoice_v2.pdf");
        when(virusScanningService.scanDocument(any(InputStream.class), anyString()))
                .thenReturn(scanResult);

        DocumentResponse expectedResponse = DocumentResponse.builder()
                .id(2L)
                .version(2)
                .build();
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        // Act
        DocumentResponse result = documentManagementService.replaceDocument(1L, newFile, "testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);

        verify(documentRepository).save(argThat(doc ->
                doc.getId().equals(1L) && !doc.isLatestVersion()
        ));
        verify(eventPublisher).publishVersionCreated(any(Document.class), any(Document.class));
    }

    @Test
    @DisplayName("uploadMultipleDocuments - batch processing")
    void testUploadMultipleDocuments_BatchProcessing() throws Exception {
        // Arrange
        List<MultipartFile> files = Arrays.asList(testFile, testFile);
        BulkUploadRequest request = BulkUploadRequest.builder()
                .files(files)
                .documentTypeId(1L)
                .orderId(100L)
                .orderNumber("ORD-001")
                .continueOnError(true)
                .build();

        FileValidator.ValidationResult validationResult = FileValidator.ValidationResult.valid();
        when(fileValidator.validateFile(any(MultipartFile.class))).thenReturn(validationResult);
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(testDocumentType));
        when(fileProcessor.calculateFileHash(any(MultipartFile.class))).thenReturn("abc123");
        when(testFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ScanResult scanResult = ScanResult.clean("invoice.pdf");
        when(virusScanningService.scanDocument(any(InputStream.class), anyString()))
                .thenReturn(scanResult);

        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        DocumentResponse expectedResponse = DocumentResponse.builder()
                .id(1L)
                .build();
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        // Act
        BulkUploadResultResponse result = documentManagementService.uploadMultipleDocuments(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalFiles()).isEqualTo(2);
        assertThat(result.getSuccessfulUploads()).isEqualTo(2);
        assertThat(result.getFailedUploads()).isEqualTo(0);
        assertThat(result.isOverallSuccess()).isTrue();

        verify(documentRepository, times(2)).save(any(Document.class));
    }
}
