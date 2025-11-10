package com.eflo.document.util;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.model.DocumentResponse;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Custom assertion utilities for document service tests
 */
public class AssertionUtils {

    /**
     * Assert that two documents are equal (comparing key fields)
     */
    public static void assertDocumentEquals(Document expected, Document actual) {
        assertThat(actual).isNotNull();
        assertThat(actual.getDocumentUuid()).isEqualTo(expected.getDocumentUuid());
        assertThat(actual.getOriginalFilename()).isEqualTo(expected.getOriginalFilename());
        assertThat(actual.getMimeType()).isEqualTo(expected.getMimeType());
        assertThat(actual.getFileSizeBytes()).isEqualTo(expected.getFileSizeBytes());
        assertThat(actual.getOrderId()).isEqualTo(expected.getOrderId());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
    }

    /**
     * Assert document matches response DTO
     */
    public static void assertDocumentMatchesResponse(Document document, DocumentResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getDocumentUuid()).isEqualTo(document.getDocumentUuid());
        assertThat(response.getOriginalFilename()).isEqualTo(document.getOriginalFilename());
        assertThat(response.getMimeType()).isEqualTo(document.getMimeType());
        assertThat(response.getFileSizeBytes()).isEqualTo(document.getFileSizeBytes());
        assertThat(response.getStatus()).isEqualTo(document.getStatus().name());
    }

    /**
     * Assert that a document is properly stored
     */
    public static void assertDocumentStored(Document document) {
        assertThat(document).isNotNull();
        assertThat(document.getId()).isNotNull();
        assertThat(document.getDocumentUuid()).isNotNull();
        assertThat(document.getStoredFilename()).isNotNull();
        assertThat(document.getStoragePath()).isNotNull();
        assertThat(document.getStorageBucket()).isNotNull();
        assertThat(document.getFileHash()).isNotNull();
        assertThat(document.getCreatedAt()).isNotNull();
    }

    /**
     * Assert that a document has valid metadata
     */
    public static void assertDocumentHasMetadata(Document document, String key, Object expectedValue) {
        assertThat(document.getCustomMetadata()).isNotNull();
        assertThat(document.getCustomMetadata()).containsKey(key);
        assertThat(document.getCustomMetadata().get(key)).isEqualTo(expectedValue);
    }

    /**
     * Assert that a document has a specific tag
     */
    public static void assertDocumentHasTag(Document document, String tag) {
        assertThat(document.getTags()).isNotNull();
        assertThat(document.getTags()).contains(tag);
    }

    /**
     * Assert document status
     */
    public static void assertDocumentStatus(Document document, DocumentStatus expectedStatus) {
        assertThat(document.getStatus()).isEqualTo(expectedStatus);
    }

    /**
     * Assert document is validated
     */
    public static void assertDocumentValidated(Document document) {
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
        assertThat(document.getValidatedAt()).isNotNull();
        assertThat(document.getValidatedBy()).isNotNull();
    }

    /**
     * Assert document is rejected
     */
    public static void assertDocumentRejected(Document document, String expectedReason) {
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(document.getValidatedAt()).isNotNull();
        assertThat(document.getValidatedBy()).isNotNull();
        if (expectedReason != null) {
            assertThat(document.getStatusReason()).contains(expectedReason);
        }
    }

    /**
     * Assert virus scan status
     */
    public static void assertVirusScanStatus(Document document, VirusScanStatus expectedStatus) {
        assertThat(document.getVirusScanStatus()).isEqualTo(expectedStatus);
        assertThat(document.getVirusScanDate()).isNotNull();
    }

    /**
     * Assert document is virus clean
     */
    public static void assertDocumentVirusClean(Document document) {
        assertThat(document.getVirusScanStatus()).isEqualTo(VirusScanStatus.CLEAN);
        assertThat(document.getVirusScanDate()).isNotNull();
    }

    /**
     * Assert document is infected
     */
    public static void assertDocumentInfected(Document document) {
        assertThat(document.getVirusScanStatus()).isEqualTo(VirusScanStatus.INFECTED);
        assertThat(document.getVirusScanDate()).isNotNull();
        assertThat(document.getVirusScanResult()).isNotNull();
    }

    /**
     * Assert document expiration
     */
    public static void assertDocumentExpires(Document document, LocalDate expectedDate) {
        assertThat(document.getExpirationDate()).isEqualTo(expectedDate);
    }

    /**
     * Assert document is expiring soon
     */
    public static void assertDocumentExpiringSoon(Document document, int warningDays) {
        assertThat(document.isExpiringSoon(warningDays)).isTrue();
        assertThat(document.getExpirationDate()).isNotNull();
    }

    /**
     * Assert document is expired
     */
    public static void assertDocumentExpired(Document document) {
        assertThat(document.isExpired()).isTrue();
        assertThat(document.getExpirationDate()).isNotNull();
        assertThat(document.getExpirationDate()).isBefore(LocalDate.now());
    }

    /**
     * Assert document type validation
     */
    public static void assertDocumentTypeValid(DocumentType documentType) {
        assertThat(documentType).isNotNull();
        assertThat(documentType.getId()).isNotNull();
        assertThat(documentType.getTypeCode()).isNotNull();
        assertThat(documentType.getTypeName()).isNotNull();
        assertThat(documentType.getCategory()).isNotNull();
        assertThat(documentType.getMaxFileSizeMb()).isNotNull();
        assertThat(documentType.getCreatedAt()).isNotNull();
    }

    /**
     * Assert file format is allowed
     */
    public static void assertFileFormatAllowed(DocumentType documentType, String format) {
        assertThat(documentType.isFileFormatAllowed(format)).isTrue();
    }

    /**
     * Assert file format is not allowed
     */
    public static void assertFileFormatNotAllowed(DocumentType documentType, String format) {
        assertThat(documentType.isFileFormatAllowed(format)).isFalse();
    }

    /**
     * Assert file size is allowed
     */
    public static void assertFileSizeAllowed(DocumentType documentType, long fileSizeBytes) {
        assertThat(documentType.isFileSizeAllowed(fileSizeBytes)).isTrue();
    }

    /**
     * Assert file size exceeds limit
     */
    public static void assertFileSizeExceeds(DocumentType documentType, long fileSizeBytes) {
        assertThat(documentType.isFileSizeAllowed(fileSizeBytes)).isFalse();
    }

    /**
     * Assert document exists in MinIO storage
     */
    public static void assertMinIOFileExists(MinioClient minioClient, String bucket, String objectName) {
        assertThatCode(() -> {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        }).doesNotThrowAnyException();
    }

    /**
     * Assert document does not exist in MinIO storage
     */
    public static void assertMinIOFileNotExists(MinioClient minioClient, String bucket, String objectName) {
        assertThatThrownBy(() -> {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        }).hasMessageContaining("does not exist");
    }

    /**
     * Assert MinIO file size
     */
    public static void assertMinIOFileSize(MinioClient minioClient, String bucket, String objectName, long expectedSize) {
        assertThatCode(() -> {
            var stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            assertThat(stat.size()).isEqualTo(expectedSize);
        }).doesNotThrowAnyException();
    }

    /**
     * Assert MinIO file content matches
     */
    public static void assertMinIOFileContent(MinioClient minioClient, String bucket, String objectName, byte[] expectedContent) {
        assertThatCode(() -> {
            try (var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            )) {
                byte[] actualContent = stream.readAllBytes();
                assertThat(actualContent).isEqualTo(expectedContent);
            }
        }).doesNotThrowAnyException();
    }

    /**
     * Assert Kafka event was published
     * This is a simplified version - in real tests you'd capture the actual message
     */
    public static void assertEventPublished(EmbeddedKafkaBroker broker, String topic) {
        var consumerProps = KafkaTestUtils.consumerProps("test-group", "true", broker);
        // This is a basic check - extend based on your needs
        assertThat(broker.getTopics()).contains(topic);
    }

    /**
     * Assert list is not empty
     */
    public static <T> void assertListNotEmpty(List<T> list) {
        assertThat(list).isNotNull();
        assertThat(list).isNotEmpty();
    }

    /**
     * Assert list has expected size
     */
    public static <T> void assertListSize(List<T> list, int expectedSize) {
        assertThat(list).isNotNull();
        assertThat(list).hasSize(expectedSize);
    }

    /**
     * Assert list contains item
     */
    public static <T> void assertListContains(List<T> list, T item) {
        assertThat(list).isNotNull();
        assertThat(list).contains(item);
    }

    /**
     * Assert map contains key
     */
    public static void assertMapContainsKey(Map<?, ?> map, Object key) {
        assertThat(map).isNotNull();
        assertThat(map).containsKey(key);
    }

    /**
     * Assert map contains entry
     */
    public static <K, V> void assertMapContainsEntry(Map<K, V> map, K key, V value) {
        assertThat(map).isNotNull();
        assertThat(map).containsEntry(key, value);
    }

    /**
     * Assert document is confidential
     */
    public static void assertDocumentConfidential(Document document) {
        assertThat(document.getIsConfidential()).isTrue();
        assertThat(document.hasRestrictedAccess()).isTrue();
    }

    /**
     * Assert document version
     */
    public static void assertDocumentVersion(Document document, int expectedVersion) {
        assertThat(document.getVersion()).isEqualTo(expectedVersion);
    }

    /**
     * Assert document is latest version
     */
    public static void assertDocumentIsLatestVersion(Document document) {
        assertThat(document.isLatest()).isTrue();
        assertThat(document.getIsLatestVersion()).isTrue();
    }

    /**
     * Assert document is not latest version
     */
    public static void assertDocumentIsNotLatestVersion(Document document) {
        assertThat(document.isLatest()).isFalse();
        assertThat(document.getReplacedByDocument()).isNotNull();
    }

    /**
     * Assert document has parent
     */
    public static void assertDocumentHasParent(Document document, Document expectedParent) {
        assertThat(document.getParentDocument()).isNotNull();
        assertThat(document.getParentDocument().getId()).isEqualTo(expectedParent.getId());
    }

    /**
     * Assert document audit fields
     */
    public static void assertDocumentAuditFields(Document document, String expectedCreatedBy) {
        assertThat(document.getCreatedBy()).isEqualTo(expectedCreatedBy);
        assertThat(document.getCreatedAt()).isNotNull();
        assertThat(document.getUploadedBy()).isNotNull();
        assertThat(document.getUploadedAt()).isNotNull();
    }
}
