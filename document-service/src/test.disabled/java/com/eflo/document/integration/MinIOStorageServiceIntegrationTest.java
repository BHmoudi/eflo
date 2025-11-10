package com.eflo.document.integration;

import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.storage.MinIOStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for MinIOStorageService with real MinIO container.
 * Tests all storage operations including bucket and file management.
 *
 * @author Document Service
 * @version 1.0
 */
class MinIOStorageServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MinIOStorageService minIOStorageService;

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_CONTENT = "This is test file content for MinIO integration test";

    @BeforeEach
    void setUp() {
        // Clean up test bucket if it exists
        try {
            minIOStorageService.deleteBucket(TEST_BUCKET);
        } catch (Exception e) {
            // Bucket might not exist, ignore
        }
    }

    @Test
    @DisplayName("Should create bucket successfully")
    void testCreateBucket() {
        // When
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);

        // Then
        List<String> buckets = minIOStorageService.listBuckets();
        assertThat(buckets).contains(TEST_BUCKET);
    }

    @Test
    @DisplayName("Should not fail when creating existing bucket")
    void testCreateExistingBucket() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);

        // When & Then - Should not throw exception
        assertThatCode(() -> minIOStorageService.createBucketIfNotExists(TEST_BUCKET))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should upload file to bucket")
    void testUploadFile() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "test-folder/test-file.txt";
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));

        // When
        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            inputStream,
            "text/plain",
            TEST_CONTENT.length()
        );

        // Then
        boolean exists = minIOStorageService.fileExists(TEST_BUCKET, objectName);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should download file from bucket")
    void testDownloadFile() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "test-download.txt";
        String content = "Download test content";

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
            "text/plain",
            content.length()
        );

        // When
        InputStream downloadedStream = minIOStorageService.downloadFile(TEST_BUCKET, objectName);

        // Then
        String downloadedContent = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(downloadedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Should throw exception when downloading non-existent file")
    void testDownloadNonExistentFile() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);

        // When & Then
        assertThatThrownBy(() ->
            minIOStorageService.downloadFile(TEST_BUCKET, "non-existent.txt"))
            .isInstanceOf(DocumentStorageException.class);
    }

    @Test
    @DisplayName("Should delete file from bucket")
    void testDeleteFile() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "test-delete.txt";

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream("Delete me".getBytes()),
            "text/plain",
            9
        );

        assertThat(minIOStorageService.fileExists(TEST_BUCKET, objectName)).isTrue();

        // When
        minIOStorageService.deleteFile(TEST_BUCKET, objectName);

        // Then
        assertThat(minIOStorageService.fileExists(TEST_BUCKET, objectName)).isFalse();
    }

    @Test
    @DisplayName("Should check if file exists")
    void testFileExists() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String existingFile = "existing.txt";
        String nonExistingFile = "non-existing.txt";

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            existingFile,
            new ByteArrayInputStream("Content".getBytes()),
            "text/plain",
            7
        );

        // When & Then
        assertThat(minIOStorageService.fileExists(TEST_BUCKET, existingFile)).isTrue();
        assertThat(minIOStorageService.fileExists(TEST_BUCKET, nonExistingFile)).isFalse();
    }

    @Test
    @DisplayName("Should get file metadata")
    void testGetFileMetadata() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "metadata-test.txt";
        String content = "Metadata test content";

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream(content.getBytes()),
            "text/plain",
            content.length()
        );

        // When
        Map<String, String> metadata = minIOStorageService.getFileMetadata(TEST_BUCKET, objectName);

        // Then
        assertThat(metadata).isNotEmpty();
        assertThat(metadata).containsKey("size");
        assertThat(metadata).containsKey("contentType");
        assertThat(metadata.get("size")).isEqualTo(String.valueOf(content.length()));
        assertThat(metadata.get("contentType")).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("Should get file size")
    void testGetFileSize() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "size-test.txt";
        String content = "File size test";

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream(content.getBytes()),
            "text/plain",
            content.length()
        );

        // When
        long size = minIOStorageService.getFileSize(TEST_BUCKET, objectName);

        // Then
        assertThat(size).isEqualTo(content.length());
    }

    @Test
    @DisplayName("Should get content type")
    void testGetContentType() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "content-type-test.pdf";

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream("PDF content".getBytes()),
            "application/pdf",
            11
        );

        // When
        String contentType = minIOStorageService.getContentType(TEST_BUCKET, objectName);

        // Then
        assertThat(contentType).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("Should copy file within MinIO")
    void testCopyFile() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String sourceBucket = TEST_BUCKET;
        String targetBucket = TEST_BUCKET + "-copy";
        minIOStorageService.createBucketIfNotExists(targetBucket);

        String sourceObject = "source.txt";
        String targetObject = "target.txt";
        String content = "Copy test content";

        minIOStorageService.uploadFile(
            sourceBucket,
            sourceObject,
            new ByteArrayInputStream(content.getBytes()),
            "text/plain",
            content.length()
        );

        // When
        minIOStorageService.copyFile(sourceBucket, sourceObject, targetBucket, targetObject);

        // Then
        assertThat(minIOStorageService.fileExists(sourceBucket, sourceObject)).isTrue();
        assertThat(minIOStorageService.fileExists(targetBucket, targetObject)).isTrue();

        // Verify content is same
        InputStream copiedStream = minIOStorageService.downloadFile(targetBucket, targetObject);
        String copiedContent = new String(copiedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(copiedContent).isEqualTo(content);

        // Cleanup
        minIOStorageService.deleteBucket(targetBucket);
    }

    @Test
    @DisplayName("Should move file within MinIO")
    void testMoveFile() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String sourceBucket = TEST_BUCKET;
        String targetBucket = TEST_BUCKET + "-move";
        minIOStorageService.createBucketIfNotExists(targetBucket);

        String sourceObject = "move-source.txt";
        String targetObject = "move-target.txt";
        String content = "Move test content";

        minIOStorageService.uploadFile(
            sourceBucket,
            sourceObject,
            new ByteArrayInputStream(content.getBytes()),
            "text/plain",
            content.length()
        );

        // When
        minIOStorageService.moveFile(sourceBucket, sourceObject, targetBucket, targetObject);

        // Then
        assertThat(minIOStorageService.fileExists(sourceBucket, sourceObject)).isFalse();
        assertThat(minIOStorageService.fileExists(targetBucket, targetObject)).isTrue();

        // Verify content
        InputStream movedStream = minIOStorageService.downloadFile(targetBucket, targetObject);
        String movedContent = new String(movedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(movedContent).isEqualTo(content);

        // Cleanup
        minIOStorageService.deleteBucket(targetBucket);
    }

    @Test
    @DisplayName("Should list objects in bucket")
    void testListObjects() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            "folder1/file1.txt",
            new ByteArrayInputStream("Content 1".getBytes()),
            "text/plain",
            9
        );

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            "folder1/file2.txt",
            new ByteArrayInputStream("Content 2".getBytes()),
            "text/plain",
            9
        );

        minIOStorageService.uploadFile(
            TEST_BUCKET,
            "folder2/file3.txt",
            new ByteArrayInputStream("Content 3".getBytes()),
            "text/plain",
            9
        );

        // When
        List<String> allObjects = minIOStorageService.listObjects(TEST_BUCKET, null);
        List<String> folder1Objects = minIOStorageService.listObjects(TEST_BUCKET, "folder1/");

        // Then
        assertThat(allObjects).hasSize(3);
        assertThat(folder1Objects).hasSize(2);
        assertThat(folder1Objects).allMatch(name -> name.startsWith("folder1/"));
    }

    @Test
    @DisplayName("Should list all buckets")
    void testListBuckets() {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET + "-2");

        // When
        List<String> buckets = minIOStorageService.listBuckets();

        // Then
        assertThat(buckets).contains(TEST_BUCKET, TEST_BUCKET + "-2");

        // Cleanup
        minIOStorageService.deleteBucket(TEST_BUCKET + "-2");
    }

    @Test
    @DisplayName("Should handle large file upload and download")
    void testLargeFileOperations() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "large-file.bin";

        // Create 1MB content
        int size = 1024 * 1024; // 1MB
        byte[] largeContent = new byte[size];
        for (int i = 0; i < size; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        // When - Upload
        long uploadStart = System.currentTimeMillis();
        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream(largeContent),
            "application/octet-stream",
            size
        );
        long uploadTime = System.currentTimeMillis() - uploadStart;

        // Then
        assertThat(minIOStorageService.fileExists(TEST_BUCKET, objectName)).isTrue();
        assertThat(uploadTime).isLessThan(5000); // Should complete within 5 seconds

        // When - Download
        long downloadStart = System.currentTimeMillis();
        InputStream downloadedStream = minIOStorageService.downloadFile(TEST_BUCKET, objectName);
        byte[] downloadedContent = downloadedStream.readAllBytes();
        long downloadTime = System.currentTimeMillis() - downloadStart;

        // Then
        assertThat(downloadedContent).hasSize(size);
        assertThat(downloadTime).isLessThan(5000);
        assertThat(downloadedContent).isEqualTo(largeContent);
    }

    @Test
    @DisplayName("Should handle concurrent file uploads")
    void testConcurrentUploads() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);

        // When - Upload multiple files concurrently
        int fileCount = 10;
        for (int i = 0; i < fileCount; i++) {
            String objectName = "concurrent-" + i + ".txt";
            String content = "Content " + i;

            minIOStorageService.uploadFile(
                TEST_BUCKET,
                objectName,
                new ByteArrayInputStream(content.getBytes()),
                "text/plain",
                content.length()
            );
        }

        // Then
        List<String> objects = minIOStorageService.listObjects(TEST_BUCKET, "concurrent-");
        assertThat(objects).hasSize(fileCount);

        // Verify all files exist and have correct content
        for (int i = 0; i < fileCount; i++) {
            String objectName = "concurrent-" + i + ".txt";
            assertThat(minIOStorageService.fileExists(TEST_BUCKET, objectName)).isTrue();

            InputStream stream = minIOStorageService.downloadFile(TEST_BUCKET, objectName);
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo("Content " + i);
        }
    }

    @Test
    @DisplayName("Should handle file path with special characters")
    void testFilePathWithSpecialCharacters() throws Exception {
        // Given
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        String objectName = "special/path-with_chars/file (1).txt";
        String content = "Special chars test";

        // When
        minIOStorageService.uploadFile(
            TEST_BUCKET,
            objectName,
            new ByteArrayInputStream(content.getBytes()),
            "text/plain",
            content.length()
        );

        // Then
        assertThat(minIOStorageService.fileExists(TEST_BUCKET, objectName)).isTrue();

        InputStream downloadedStream = minIOStorageService.downloadFile(TEST_BUCKET, objectName);
        String downloadedContent = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(downloadedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Should handle error scenarios gracefully")
    void testErrorScenarios() {
        // Test download from non-existent bucket
        assertThatThrownBy(() ->
            minIOStorageService.downloadFile("non-existent-bucket", "file.txt"))
            .isInstanceOf(DocumentStorageException.class);

        // Test get metadata for non-existent file
        minIOStorageService.createBucketIfNotExists(TEST_BUCKET);
        assertThatThrownBy(() ->
            minIOStorageService.getFileMetadata(TEST_BUCKET, "non-existent.txt"))
            .isInstanceOf(DocumentStorageException.class);
    }
}
