package com.eflo.document.unit;

import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.storage.MinIOStorageService;
import io.minio.*;
import io.minio.messages.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinIOStorageService Unit Tests")
class MinIOStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinIOStorageService storageService;

    @Test
    @DisplayName("uploadFile - success")
    void testUploadFile_Success() throws Exception {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // Act
        storageService.uploadFile("test-bucket", "path/file.pdf", inputStream, "application/pdf", 1024L);

        // Assert
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("uploadFile - bucket not exists")
    void testUploadFile_BucketNotExists() throws Exception {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // Act
        storageService.createBucketIfNotExists("new-bucket");

        // Assert
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("uploadFile - connection error")
    void testUploadFile_ConnectionError() throws Exception {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // Act & Assert
        assertThatThrownBy(() ->
                storageService.uploadFile("bucket", "path/file.pdf", inputStream, "application/pdf", 1024L)
        ).isInstanceOf(DocumentStorageException.class);

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("downloadFile - success")
    void testDownloadFile_Success() throws Exception {
        // Arrange
        InputStream expectedStream = new ByteArrayInputStream("content".getBytes());
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(expectedStream);

        // Act
        InputStream result = storageService.downloadFile("bucket", "path/file.pdf");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedStream);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("downloadFile - file not found")
    void testDownloadFile_NotFound() throws Exception {
        // Arrange
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        assertThatThrownBy(() -> storageService.downloadFile("bucket", "path/missing.pdf"))
                .isInstanceOf(DocumentStorageException.class);

        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("deleteFile - success")
    void testDeleteFile_Success() throws Exception {
        // Act
        storageService.deleteFile("bucket", "path/file.pdf");

        // Assert
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("copyFile - success")
    void testCopyFile_Success() throws Exception {
        // Act
        storageService.copyFile("source-bucket", "source/file.pdf",
                "target-bucket", "target/file.pdf");

        // Assert
        verify(minioClient).copyObject(any(CopyObjectArgs.class));
    }

    @Test
    @DisplayName("moveFile - success")
    void testMoveFile_Success() throws Exception {
        // Act
        storageService.moveFile("source-bucket", "source/file.pdf",
                "target-bucket", "target/file.pdf");

        // Assert
        verify(minioClient).copyObject(any(CopyObjectArgs.class));
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("fileExists - true")
    void testFileExists_True() throws Exception {
        // Arrange
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));

        // Act
        boolean result = storageService.fileExists("bucket", "path/file.pdf");

        // Assert
        assertThat(result).isTrue();
        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    @DisplayName("fileExists - false")
    void testFileExists_False() throws Exception {
        // Arrange
        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenThrow(new RuntimeException("Not found"));

        // Act
        boolean result = storageService.fileExists("bucket", "path/missing.pdf");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("listBuckets - success")
    void testListBuckets_Success() throws Exception {
        // Arrange
        Bucket bucket1 = mock(Bucket.class);
        Bucket bucket2 = mock(Bucket.class);
        when(bucket1.name()).thenReturn("bucket1");
        when(bucket2.name()).thenReturn("bucket2");
        when(minioClient.listBuckets()).thenReturn(Arrays.asList(bucket1, bucket2));

        // Act
        List<String> result = storageService.listBuckets();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains("bucket1", "bucket2");
    }
}
