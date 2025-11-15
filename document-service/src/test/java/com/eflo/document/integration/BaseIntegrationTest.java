package com.eflo.document.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base Integration Test class with Testcontainers configuration.
 * Provides shared container instances for PostgreSQL, MinIO, and Kafka.
 *
 * All integration tests should extend this class to benefit from:
 * - Shared container instances (faster test execution)
 * - Automatic container lifecycle management
 * - Test profile configuration
 * - Database cleanup between tests
 *
 * @author Document Service
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    /**
     * PostgreSQL Container - Shared across all tests
     * Using PostgreSQL 15 for database operations
     */
    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("document_service_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * MinIO Container - Shared across all tests
     * Using MinIO for S3-compatible object storage
     */
    @Container
    protected static final GenericContainer<?> minioContainer =
        new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server", "/data")
            .withReuse(true);

    /**
     * Kafka Container - Shared across all tests
     * Using Confluent Kafka for event streaming
     */
    @Container
    protected static final KafkaContainer kafkaContainer =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Configure dynamic properties from containers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Flyway configuration
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");

        // MinIO configuration
        registry.add("minio.url", () -> "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
        registry.add("minio.bucket-name", () -> "documents");
        registry.add("minio.secure", () -> "false");

        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");

        // Disable security for tests
        registry.add("spring.security.enabled", () -> "false");
        registry.add("spring.autoconfigure.exclude", () ->
            "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration");

        // Disable Eureka for tests
        registry.add("eureka.client.enabled", () -> "false");

        // Disable ClamAV for tests (use mock)
        registry.add("clamav.enabled", () -> "false");
        registry.add("virus-scanning.enabled", () -> "false");
    }

    /**
     * Ensure containers are started before running any tests
     */
    @BeforeAll
    static void beforeAll() {
        // Containers are automatically started by Testcontainers
        // This method can be used for additional setup if needed
    }

    /**
     * Clean up database after each test to ensure test isolation
     * This prevents test data from one test affecting another test
     */
    @AfterEach
    void cleanupDatabase() {
        // Delete in order to respect foreign key constraints
        jdbcTemplate.execute("TRUNCATE TABLE document_access_log CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE document_metadata CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE documents CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE document_validation_rules CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE document_types CASCADE");

        // Reset sequences
        jdbcTemplate.execute("ALTER SEQUENCE IF EXISTS documents_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE IF EXISTS document_types_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE IF EXISTS document_access_log_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE IF EXISTS document_validation_rules_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE IF EXISTS document_metadata_id_seq RESTART WITH 1");
    }

    /**
     * Helper method to get MinIO endpoint
     */
    protected String getMinioEndpoint() {
        return "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000);
    }

    /**
     * Helper method to get Kafka bootstrap servers
     */
    protected String getKafkaBootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }

    /**
     * Helper method to get PostgreSQL JDBC URL
     */
    protected String getPostgresJdbcUrl() {
        return postgresContainer.getJdbcUrl();
    }
}
