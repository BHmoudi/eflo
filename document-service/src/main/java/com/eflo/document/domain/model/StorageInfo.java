package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing document storage information.
 * Contains storage bucket, path, size, and access URLs.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageInfo {

    /**
     * Storage bucket or container name.
     * Example: "eflo-documents", "documents-prod"
     */
    private String bucket;

    /**
     * Full storage path including folder structure.
     * Example: "2024/10/order-12345/invoice.pdf"
     */
    private String path;

    /**
     * Full storage key (bucket + path).
     * Example: "s3://eflo-documents/2024/10/order-12345/invoice.pdf"
     */
    private String storageKey;

    /**
     * Storage region or location.
     * Example: "us-east-1", "eu-west-1"
     */
    private String region;

    /**
     * File size in bytes.
     */
    private Long sizeBytes;

    /**
     * File size in megabytes (calculated field).
     */
    private Double sizeMB;

    /**
     * File size in gigabytes (calculated field).
     */
    private Double sizeGB;

    /**
     * Direct download URL (pre-signed or public).
     */
    private String url;

    /**
     * Pre-signed URL with expiration.
     */
    private String presignedUrl;

    /**
     * Expiration time for pre-signed URL (in seconds).
     */
    private Long urlExpirationSeconds;

    /**
     * Thumbnail or preview URL (if available).
     */
    private String thumbnailUrl;

    /**
     * Storage provider type.
     * Example: S3, Azure Blob, Google Cloud Storage, Local File System
     */
    private StorageProvider provider;

    /**
     * Content type / MIME type.
     */
    private String contentType;

    /**
     * ETag or version identifier from storage provider.
     */
    private String etag;

    /**
     * Storage class or tier.
     * Example: STANDARD, STANDARD_IA, GLACIER (for S3)
     */
    private String storageClass;

    /**
     * Server-side encryption status.
     */
    private Boolean encrypted;

    /**
     * Encryption algorithm used.
     * Example: AES256, aws:kms
     */
    private String encryptionAlgorithm;

    /**
     * Supported storage providers.
     */
    public enum StorageProvider {
        AWS_S3("Amazon S3"),
        AZURE_BLOB("Azure Blob Storage"),
        GOOGLE_CLOUD("Google Cloud Storage"),
        LOCAL_FILE_SYSTEM("Local File System"),
        MINIO("MinIO"),
        CUSTOM("Custom Storage");

        private final String displayName;

        StorageProvider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get file size in a human-readable format.
     *
     * @return human-readable file size
     */
    public String getFormattedSize() {
        if (sizeBytes == null) {
            return "Unknown";
        }

        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeBytes / 1024.0);
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Check if the storage has a valid download URL.
     *
     * @return true if URL is available
     */
    public boolean hasUrl() {
        return url != null && !url.isEmpty();
    }

    /**
     * Check if the storage has a pre-signed URL.
     *
     * @return true if pre-signed URL is available
     */
    public boolean hasPresignedUrl() {
        return presignedUrl != null && !presignedUrl.isEmpty();
    }

    /**
     * Check if the storage has a thumbnail.
     *
     * @return true if thumbnail URL is available
     */
    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isEmpty();
    }

    /**
     * Check if the file is encrypted at rest.
     *
     * @return true if encrypted
     */
    public boolean isEncrypted() {
        return Boolean.TRUE.equals(encrypted);
    }

    /**
     * Get the full storage location identifier.
     *
     * @return full storage location
     */
    public String getFullLocation() {
        if (bucket != null && path != null) {
            return String.format("%s/%s", bucket, path);
        }
        return storageKey != null ? storageKey : "";
    }

    /**
     * Create StorageInfo from basic parameters.
     *
     * @param bucket storage bucket
     * @param path storage path
     * @param sizeBytes file size in bytes
     * @return StorageInfo instance
     */
    public static StorageInfo of(String bucket, String path, Long sizeBytes) {
        double sizeMB = sizeBytes / (1024.0 * 1024.0);
        double sizeGB = sizeBytes / (1024.0 * 1024.0 * 1024.0);

        return StorageInfo.builder()
                .bucket(bucket)
                .path(path)
                .sizeBytes(sizeBytes)
                .sizeMB(sizeMB)
                .sizeGB(sizeGB)
                .build();
    }

    /**
     * Create StorageInfo with URL.
     *
     * @param bucket storage bucket
     * @param path storage path
     * @param sizeBytes file size in bytes
     * @param url download URL
     * @return StorageInfo instance
     */
    public static StorageInfo withUrl(String bucket, String path, Long sizeBytes, String url) {
        StorageInfo info = of(bucket, path, sizeBytes);
        info.setUrl(url);
        return info;
    }
}
