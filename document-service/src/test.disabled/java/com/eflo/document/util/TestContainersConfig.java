package com.eflo.document.util;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.KafkaContainer;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration with singleton container instances
 * Containers are shared across all tests for performance
 */
@TestConfiguration
public class TestContainersConfig {

    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String MINIO_IMAGE = "minio/minio:latest";
    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.5.0";
    private static final String CLAMAV_IMAGE = "clamav/clamav:latest";

    // Singleton container instances
    private static PostgreSQLContainer<?> postgresContainer;
    private static GenericContainer<?> minioContainer;
    private static KafkaContainer kafkaContainer;
    private static GenericContainer<?> clamavContainer;

    // Reuse containers flag - set to true for faster test execution
    private static final boolean REUSE_CONTAINERS = true;

    /**
     * Get or create PostgreSQL container
     */
    public static PostgreSQLContainer<?> getPostgresContainer() {
        if (postgresContainer == null) {
            postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(REUSE_CONTAINERS);
            postgresContainer.start();
        }
        return postgresContainer;
    }

    /**
     * Get or create MinIO container
     */
    public static GenericContainer<?> getMinioContainer() {
        if (minioContainer == null) {
            minioContainer = new GenericContainer<?>(MINIO_IMAGE)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withReuse(REUSE_CONTAINERS);
            minioContainer.start();

            // Create default test bucket
            createDefaultBucket();
        }
        return minioContainer;
    }

    /**
     * Get or create Kafka container
     */
    public static KafkaContainer getKafkaContainer() {
        if (kafkaContainer == null) {
            kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
                    .withReuse(REUSE_CONTAINERS);
            kafkaContainer.start();
        }
        return kafkaContainer;
    }

    /**
     * Get or create ClamAV container
     */
    public static GenericContainer<?> getClamavContainer() {
        if (clamavContainer == null) {
            clamavContainer = new GenericContainer<>(CLAMAV_IMAGE)
                    .withExposedPorts(3310)
                    .withReuse(REUSE_CONTAINERS);
            clamavContainer.start();

            // Wait for ClamAV to be ready (it takes time to load virus definitions)
            waitForClamAV();
        }
        return clamavContainer;
    }

    /**
     * Configure Spring properties dynamically from containers
     */
    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        PostgreSQLContainer<?> postgres = getPostgresContainer();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // MinIO configuration
        GenericContainer<?> minio = getMinioContainer();
        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
        registry.add("minio.bucket-name", () -> "test-documents");

        // Kafka configuration
        KafkaContainer kafka = getKafkaContainer();
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // ClamAV configuration
        GenericContainer<?> clamav = getClamavContainer();
        registry.add("clamav.host", clamav::getHost);
        registry.add("clamav.port", () -> clamav.getMappedPort(3310));
    }

    /**
     * Create default MinIO bucket for tests
     */
    private static void createDefaultBucket() {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioContainer.getS3URL())
                    .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                    .build();

            String bucketName = "test-documents";
            boolean bucketExists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MinIO test bucket", e);
        }
    }

    /**
     * Wait for ClamAV to be ready
     * ClamAV takes time to load virus definitions on startup
     */
    private static void waitForClamAV() {
        try {
            // Wait up to 60 seconds for ClamAV to be ready
            int maxAttempts = 60;
            int attempts = 0;

            while (attempts < maxAttempts) {
                try {
                    // Simple check - if container is running and port is exposed, assume ready
                    if (clamavContainer.isRunning()) {
                        Thread.sleep(2000); // Additional wait for initialization
                        break;
                    }
                } catch (Exception e) {
                    // Ignore and retry
                }

                Thread.sleep(1000);
                attempts++;
            }

            if (attempts >= maxAttempts) {
                System.err.println("Warning: ClamAV may not be fully ready");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stop all containers
     * Call this in @AfterAll if needed, though containers will auto-stop
     */
    public static void stopAllContainers() {
        if (postgresContainer != null && postgresContainer.isRunning()) {
            postgresContainer.stop();
        }
        if (minioContainer != null && minioContainer.isRunning()) {
            minioContainer.stop();
        }
        if (kafkaContainer != null && kafkaContainer.isRunning()) {
            kafkaContainer.stop();
        }
        if (clamavContainer != null && clamavContainer.isRunning()) {
            clamavContainer.stop();
        }
    }

    /**
     * Reset containers - stop and clear references
     * Useful for tests that need fresh containers
     */
    public static void resetContainers() {
        stopAllContainers();
        postgresContainer = null;
        minioContainer = null;
        kafkaContainer = null;
        clamavContainer = null;
    }

    /**
     * Get MinIO client for direct operations in tests
     */
    @Bean
    public static MinioClient getTestMinioClient() {
        GenericContainer<?> minio = getMinioContainer();
        return MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(minio.getUserName(), minio.getPassword())
                .build();
    }

    /**
     * Check if a container is running
     */
    public static boolean isPostgresRunning() {
        return postgresContainer != null && postgresContainer.isRunning();
    }

    public static boolean isMinioRunning() {
        return minioContainer != null && minioContainer.isRunning();
    }

    public static boolean isKafkaRunning() {
        return kafkaContainer != null && kafkaContainer.isRunning();
    }

    public static boolean isClamavRunning() {
        return clamavContainer != null && clamavContainer.isRunning();
    }

    /**
     * Get container connection details for manual testing
     */
    public static String getPostgresJdbcUrl() {
        return getPostgresContainer().getJdbcUrl();
    }

    public static String getMinioUrl() {
        return getMinioContainer().getS3URL();
    }

    public static String getKafkaBootstrapServers() {
        return getKafkaContainer().getBootstrapServers();
    }

    public static String getClamavHost() {
        return getClamavContainer().getHost();
    }

    public static Integer getClamavPort() {
        return getClamavContainer().getMappedPort(3310);
    }
}
