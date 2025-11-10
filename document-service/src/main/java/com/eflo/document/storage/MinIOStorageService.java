package com.eflo.document.storage;

import com.eflo.document.exception.DocumentStorageException;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * MinIO Storage Service for managing document storage operations.
 * Provides bucket management, file operations, and metadata retrieval.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOStorageService {

    private final MinioClient minioClient;

    /**
     * Creates a bucket if it doesn't already exist.
     *
     * @param bucketName the name of the bucket to create
     * @throws DocumentStorageException if bucket creation fails
     */
    public void createBucketIfNotExists(String bucketName) {
        log.debug("Checking if bucket exists: {}", bucketName);

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                log.info("Creating bucket: {}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket created successfully: {}", bucketName);
            } else {
                log.debug("Bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to create bucket {}: {}", bucketName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to create bucket: " + bucketName,
                    e,
                    "CREATE_BUCKET",
                    bucketName,
                    null
            );
        }
    }

    /**
     * Deletes a bucket and all its contents.
     * WARNING: This operation is destructive and cannot be undone.
     *
     * @param bucketName the name of the bucket to delete
     * @throws DocumentStorageException if bucket deletion fails
     */
    public void deleteBucket(String bucketName) {
        log.warn("Deleting bucket: {}", bucketName);

        try {
            // Note: MinIO requires bucket to be empty before deletion
            // In production, you might want to add logic to empty the bucket first
            minioClient.removeBucket(
                    RemoveBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("Bucket deleted successfully: {}", bucketName);
        } catch (Exception e) {
            log.error("Failed to delete bucket {}: {}", bucketName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to delete bucket: " + bucketName,
                    e,
                    "DELETE_BUCKET",
                    bucketName,
                    null
            );
        }
    }

    /**
     * Lists all buckets in MinIO.
     *
     * @return list of bucket names
     * @throws DocumentStorageException if listing buckets fails
     */
    public List<String> listBuckets() {
        log.debug("Listing all buckets");

        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<String> bucketNames = buckets.stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());

            log.debug("Found {} buckets", bucketNames.size());
            return bucketNames;
        } catch (Exception e) {
            log.error("Failed to list buckets: {}", e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to list buckets",
                    e,
                    "LIST_BUCKETS",
                    null,
                    null
            );
        }
    }

    /**
     * Uploads a file to MinIO storage.
     *
     * @param bucketName the bucket to upload to
     * @param objectName the object name (path) in the bucket
     * @param inputStream the file content as an InputStream
     * @param contentType the MIME type of the file
     * @param size the size of the file in bytes (-1 if unknown)
     * @throws DocumentStorageException if upload fails
     */
    public void uploadFile(String bucketName, String objectName, InputStream inputStream,
                          String contentType, long size) {
        log.info("Uploading file to bucket: {}, object: {}", bucketName, objectName);

        try {
            PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1);

            if (contentType != null && !contentType.isEmpty()) {
                argsBuilder.contentType(contentType);
            }

            minioClient.putObject(argsBuilder.build());
            log.info("File uploaded successfully: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("Failed to upload file {}/{}: {}", bucketName, objectName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to upload file: " + objectName,
                    e,
                    "UPLOAD_FILE",
                    bucketName,
                    objectName
            );
        }
    }

    /**
     * Downloads a file from MinIO storage.
     *
     * @param bucketName the bucket to download from
     * @param objectName the object name (path) in the bucket
     * @return InputStream of the file content
     * @throws DocumentStorageException if download fails
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        log.info("Downloading file from bucket: {}, object: {}", bucketName, objectName);

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File downloaded successfully: {}/{}", bucketName, objectName);
            return stream;
        } catch (Exception e) {
            log.error("Failed to download file {}/{}: {}", bucketName, objectName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to download file: " + objectName,
                    e,
                    "DOWNLOAD_FILE",
                    bucketName,
                    objectName
            );
        }
    }

    /**
     * Deletes a file from MinIO storage.
     *
     * @param bucketName the bucket containing the file
     * @param objectName the object name (path) in the bucket
     * @throws DocumentStorageException if deletion fails
     */
    public void deleteFile(String bucketName, String objectName) {
        log.info("Deleting file from bucket: {}, object: {}", bucketName, objectName);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File deleted successfully: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("Failed to delete file {}/{}: {}", bucketName, objectName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to delete file: " + objectName,
                    e,
                    "DELETE_FILE",
                    bucketName,
                    objectName
            );
        }
    }

    /**
     * Checks if a file exists in MinIO storage.
     *
     * @param bucketName the bucket to check
     * @param objectName the object name (path) to check
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String bucketName, String objectName) {
        log.debug("Checking if file exists: {}/{}", bucketName, objectName);

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.debug("File exists: {}/{}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.debug("File does not exist: {}/{}", bucketName, objectName);
            return false;
        }
    }

    /**
     * Gets file metadata from MinIO storage.
     *
     * @param bucketName the bucket containing the file
     * @param objectName the object name (path) in the bucket
     * @return map of metadata key-value pairs
     * @throws DocumentStorageException if retrieval fails
     */
    public Map<String, String> getFileMetadata(String bucketName, String objectName) {
        log.debug("Getting file metadata: {}/{}", bucketName, objectName);

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            Map<String, String> metadata = new HashMap<>();
            metadata.put("size", String.valueOf(stat.size()));
            metadata.put("contentType", stat.contentType());
            metadata.put("etag", stat.etag());
            metadata.put("lastModified", stat.lastModified().toString());

            // Add user metadata
            if (stat.userMetadata() != null) {
                metadata.putAll(stat.userMetadata());
            }

            log.debug("Retrieved metadata for {}/{}: {}", bucketName, objectName, metadata);
            return metadata;
        } catch (Exception e) {
            log.error("Failed to get metadata for {}/{}: {}", bucketName, objectName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to get file metadata: " + objectName,
                    e,
                    "GET_METADATA",
                    bucketName,
                    objectName
            );
        }
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param bucketName the bucket containing the file
     * @param objectName the object name (path) in the bucket
     * @return file size in bytes
     * @throws DocumentStorageException if retrieval fails
     */
    public long getFileSize(String bucketName, String objectName) {
        log.debug("Getting file size: {}/{}", bucketName, objectName);

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            long size = stat.size();
            log.debug("File size for {}/{}: {} bytes", bucketName, objectName, size);
            return size;
        } catch (Exception e) {
            log.error("Failed to get file size for {}/{}: {}", bucketName, objectName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to get file size: " + objectName,
                    e,
                    "GET_FILE_SIZE",
                    bucketName,
                    objectName
            );
        }
    }

    /**
     * Gets the content type of a file.
     *
     * @param bucketName the bucket containing the file
     * @param objectName the object name (path) in the bucket
     * @return content type (MIME type)
     * @throws DocumentStorageException if retrieval fails
     */
    public String getContentType(String bucketName, String objectName) {
        log.debug("Getting content type: {}/{}", bucketName, objectName);

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            String contentType = stat.contentType();
            log.debug("Content type for {}/{}: {}", bucketName, objectName, contentType);
            return contentType;
        } catch (Exception e) {
            log.error("Failed to get content type for {}/{}: {}", bucketName, objectName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to get content type: " + objectName,
                    e,
                    "GET_CONTENT_TYPE",
                    bucketName,
                    objectName
            );
        }
    }

    /**
     * Copies a file from one location to another within MinIO.
     *
     * @param sourceBucket the source bucket
     * @param sourceObject the source object name
     * @param targetBucket the target bucket
     * @param targetObject the target object name
     * @throws DocumentStorageException if copy fails
     */
    public void copyFile(String sourceBucket, String sourceObject, String targetBucket, String targetObject) {
        log.info("Copying file from {}/{} to {}/{}", sourceBucket, sourceObject, targetBucket, targetObject);

        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(targetObject)
                            .source(
                                    CopySource.builder()
                                            .bucket(sourceBucket)
                                            .object(sourceObject)
                                            .build()
                            )
                            .build()
            );
            log.info("File copied successfully from {}/{} to {}/{}",
                    sourceBucket, sourceObject, targetBucket, targetObject);
        } catch (Exception e) {
            log.error("Failed to copy file from {}/{} to {}/{}: {}",
                    sourceBucket, sourceObject, targetBucket, targetObject, e.getMessage(), e);
            throw new DocumentStorageException(
                    String.format("Failed to copy file from %s/%s to %s/%s",
                            sourceBucket, sourceObject, targetBucket, targetObject),
                    e,
                    "COPY_FILE",
                    sourceBucket,
                    sourceObject
            );
        }
    }

    /**
     * Moves a file from one location to another within MinIO.
     * This is implemented as a copy followed by a delete.
     *
     * @param sourceBucket the source bucket
     * @param sourceObject the source object name
     * @param targetBucket the target bucket
     * @param targetObject the target object name
     * @throws DocumentStorageException if move fails
     */
    public void moveFile(String sourceBucket, String sourceObject, String targetBucket, String targetObject) {
        log.info("Moving file from {}/{} to {}/{}", sourceBucket, sourceObject, targetBucket, targetObject);

        try {
            // Copy the file
            copyFile(sourceBucket, sourceObject, targetBucket, targetObject);

            // Delete the source file
            deleteFile(sourceBucket, sourceObject);

            log.info("File moved successfully from {}/{} to {}/{}",
                    sourceBucket, sourceObject, targetBucket, targetObject);
        } catch (Exception e) {
            log.error("Failed to move file from {}/{} to {}/{}: {}",
                    sourceBucket, sourceObject, targetBucket, targetObject, e.getMessage(), e);
            throw new DocumentStorageException(
                    String.format("Failed to move file from %s/%s to %s/%s",
                            sourceBucket, sourceObject, targetBucket, targetObject),
                    e,
                    "MOVE_FILE",
                    sourceBucket,
                    sourceObject
            );
        }
    }

    /**
     * Lists all objects in a bucket with a given prefix.
     *
     * @param bucketName the bucket to list
     * @param prefix the prefix to filter objects (can be null or empty)
     * @return list of object names
     * @throws DocumentStorageException if listing fails
     */
    public List<String> listObjects(String bucketName, String prefix) {
        log.debug("Listing objects in bucket: {} with prefix: {}", bucketName, prefix);

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix != null ? prefix : "")
                            .recursive(true)
                            .build()
            );

            List<String> objectNames = StreamSupport.stream(results.spliterator(), false)
                    .map(result -> {
                        try {
                            return result.get().objectName();
                        } catch (Exception e) {
                            log.error("Error reading object from results: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Found {} objects in bucket {} with prefix {}", objectNames.size(), bucketName, prefix);
            return objectNames;
        } catch (Exception e) {
            log.error("Failed to list objects in bucket {}: {}", bucketName, e.getMessage(), e);
            throw new DocumentStorageException(
                    "Failed to list objects in bucket: " + bucketName,
                    e,
                    "LIST_OBJECTS",
                    bucketName,
                    null
            );
        }
    }
}
