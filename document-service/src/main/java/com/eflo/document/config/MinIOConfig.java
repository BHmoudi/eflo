package com.eflo.document.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.util.List;

/**
 * MinIO Configuration for document storage.
 * Configures MinIO client and manages bucket initialization.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class MinIOConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket.documents}")
    private String documentsBucket;

    @Value("${minio.bucket.temp}")
    private String tempBucket;

    @Value("${minio.bucket.archive}")
    private String archiveBucket;

    @Value("${minio.secure:false}")
    private boolean secure;

    @Value("${minio.connect-timeout:10000}")
    private long connectTimeout;

    @Value("${minio.write-timeout:60000}")
    private long writeTimeout;

    @Value("${minio.read-timeout:30000}")
    private long readTimeout;

    /**
     * Creates and configures the MinIO client bean.
     * The client is configured with endpoint, credentials, and timeout settings.
     *
     * @return configured MinioClient instance
     * @throws IllegalArgumentException if endpoint or credentials are invalid
     */
    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client with endpoint: {}", endpoint);

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Set timeouts
            client.setTimeout(
                    Duration.ofMillis(connectTimeout).toMillis(),
                    Duration.ofMillis(writeTimeout).toMillis(),
                    Duration.ofMillis(readTimeout).toMillis()
            );

            log.info("MinIO client initialized successfully");
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to create MinIO client", e);
        }
    }

    /**
     * Returns the list of bucket names that should be created on startup.
     *
     * @return list of bucket names
     */
    @Bean
    public List<String> minioBuckets() {
        return List.of(documentsBucket, tempBucket, archiveBucket);
    }

    /**
     * Initializes MinIO buckets on application startup.
     * Creates buckets if they don't exist.
     * This method is triggered after the application context is fully initialized.
     *
     * @param minioClient the MinIO client
     * @param buckets list of bucket names to initialize
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeBuckets() {
        MinioClient client = minioClient();
        List<String> buckets = minioBuckets();

        log.info("Initializing MinIO buckets: {}", buckets);

        for (String bucketName : buckets) {
            try {
                createBucketIfNotExists(client, bucketName);
            } catch (Exception e) {
                log.error("Failed to initialize bucket {}: {}", bucketName, e.getMessage(), e);
                // Don't throw exception to allow application to start
                // Bucket operations will fail later if bucket doesn't exist
            }
        }

        log.info("MinIO bucket initialization completed");
    }

    /**
     * Creates a bucket if it doesn't already exist.
     *
     * @param client the MinIO client
     * @param bucketName the name of the bucket to create
     * @throws Exception if bucket creation fails
     */
    private void createBucketIfNotExists(MinioClient client, String bucketName) throws Exception {
        boolean exists = client.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            log.info("Creating bucket: {}", bucketName);
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("Bucket created successfully: {}", bucketName);
        } else {
            log.debug("Bucket already exists: {}", bucketName);
        }
    }

    /**
     * Gets the documents bucket name.
     *
     * @return documents bucket name
     */
    public String getDocumentsBucket() {
        return documentsBucket;
    }

    /**
     * Gets the temporary files bucket name.
     *
     * @return temp bucket name
     */
    public String getTempBucket() {
        return tempBucket;
    }

    /**
     * Gets the archive bucket name.
     *
     * @return archive bucket name
     */
    public String getArchiveBucket() {
        return archiveBucket;
    }
}
