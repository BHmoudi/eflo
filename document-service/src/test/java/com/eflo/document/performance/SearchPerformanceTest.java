package com.eflo.document.performance;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentSearchService;
import com.eflo.document.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for document search and query operations
 *
 * These tests are disabled by default as they take longer to run
 * and require a large dataset.
 *
 * Run with: mvn test -Dtest=SearchPerformanceTest
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Performance tests - enable manually when needed")
public class SearchPerformanceTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentSearchService searchService;

    private DocumentType testDocumentType;
    private static final int LARGE_DATASET_SIZE = 10000;
    private static final int MEDIUM_DATASET_SIZE = 1000;
    private static final int QUERY_ITERATIONS = 100;

    @BeforeEach
    void setUp() {
        // Create test document type
        testDocumentType = TestDataBuilder.createTestDocumentType("SEARCH_PERF_TEST");
        testDocumentType = documentTypeRepository.save(testDocumentType);
    }

    @Test
    @Transactional
    void testSearchWithLargeDataset() {
        System.out.println("\n=== Search Performance with Large Dataset ===");
        System.out.println("Creating " + LARGE_DATASET_SIZE + " test documents...");

        // Create large dataset
        createLargeDataset(LARGE_DATASET_SIZE);

        System.out.println("Dataset created. Running search queries...");

        // Test various search scenarios
        testSimpleSearch();
        testFilteredSearch();
        testFullTextSearch();
        testComplexSearch();
        testPaginatedSearch();
    }

    @Test
    @Transactional
    void testIndexEffectiveness() {
        System.out.println("\n=== Index Effectiveness Test ===");

        createLargeDataset(MEDIUM_DATASET_SIZE);

        // Test indexed fields
        testIndexedFieldQuery("order_id");
        testIndexedFieldQuery("document_type_id");
        testIndexedFieldQuery("status");
        testIndexedFieldQuery("uploaded_by");

        // Test compound index
        testCompoundIndexQuery();
    }

    @Test
    @Transactional
    void testAggregationPerformance() {
        System.out.println("\n=== Aggregation Performance Test ===");

        createLargeDataset(MEDIUM_DATASET_SIZE);

        // Count by status
        Instant start = Instant.now();
        long count = documentRepository.countByStatus(DocumentStatus.VALIDATED);
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        System.out.println("Count by status: " + duration + "ms (result: " + count + ")");

        // Count by type
        start = Instant.now();
        count = documentRepository.countByDocumentType(testDocumentType);
        end = Instant.now();
        duration = Duration.between(start, end).toMillis();
        System.out.println("Count by type: " + duration + "ms (result: " + count + ")");

        // Count by order
        start = Instant.now();
        count = documentRepository.countByOrderId(100001L);
        end = Instant.now();
        duration = Duration.between(start, end).toMillis();
        System.out.println("Count by order: " + duration + "ms (result: " + count + ")");

        assertThat(duration).isLessThan(500); // Aggregations should be fast
    }

    @Test
    @Transactional
    void testDateRangeQueryPerformance() {
        System.out.println("\n=== Date Range Query Performance ===");

        createLargeDataset(MEDIUM_DATASET_SIZE);

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Instant start = Instant.now();
            List<Document> results = documentRepository.findByUploadedAtBetween(startDate, endDate);
            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);

            if ((i + 1) % 20 == 0) {
                System.out.println("Completed " + (i + 1) + " queries...");
            }
        }

        printQueryStats("Date Range Query", queryTimes);

        // Assertions
        double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        assertThat(avgTime).isLessThan(100); // Date range queries should be < 100ms
    }

    @Test
    @Transactional
    void testPaginationPerformance() {
        System.out.println("\n=== Pagination Performance Test ===");

        createLargeDataset(MEDIUM_DATASET_SIZE);

        int[] pageSizes = {10, 20, 50, 100};

        for (int pageSize : pageSizes) {
            List<Long> queryTimes = new ArrayList<>();
            int totalPages = MEDIUM_DATASET_SIZE / pageSize;

            for (int page = 0; page < Math.min(totalPages, 50); page++) {
                Pageable pageable = PageRequest.of(page, pageSize);

                Instant start = Instant.now();
                Page<Document> results = documentRepository.findAll(pageable);
                Instant end = Instant.now();

                long duration = Duration.between(start, end).toMillis();
                queryTimes.add(duration);
            }

            double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            System.out.println("Page size " + pageSize + " - Average query time: " + String.format("%.2f", avgTime) + "ms");
        }
    }

    @Test
    @Transactional
    void testFullTextSearchPerformance() {
        System.out.println("\n=== Full Text Search Performance ===");

        createLargeDataset(MEDIUM_DATASET_SIZE);

        String[] searchTerms = {"test", "invoice", "contract", "receipt", "document"};

        for (String term : searchTerms) {
            List<Long> queryTimes = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                Instant start = Instant.now();
                List<Document> results = documentRepository.findByOriginalFilenameContaining(term);
                Instant end = Instant.now();

                long duration = Duration.between(start, end).toMillis();
                queryTimes.add(duration);
            }

            double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            System.out.println("Search term '" + term + "' - Average: " + String.format("%.2f", avgTime) + "ms");
        }
    }

    @Test
    @Transactional
    void testComplexJoinQueryPerformance() {
        System.out.println("\n=== Complex Join Query Performance ===");

        createLargeDataset(MEDIUM_DATASET_SIZE);

        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Instant start = Instant.now();

            // Complex query with joins
            List<Document> results = documentRepository.findByDocumentTypeAndStatusAndUploadedAtAfter(
                    testDocumentType,
                    DocumentStatus.VALIDATED,
                    LocalDateTime.now().minusDays(30)
            );

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        printQueryStats("Complex Join Query", queryTimes);

        double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        assertThat(avgTime).isLessThan(200); // Complex queries should still be reasonably fast
    }

    /**
     * Helper method to create large dataset
     */
    private void createLargeDataset(int size) {
        Instant start = Instant.now();

        List<Document> documents = IntStream.range(0, size)
                .mapToObj(i -> {
                    Document doc = TestDataBuilder.createTestDocument(testDocumentType, 100000L + (i / 10));

                    // Randomize some fields for variety
                    if (i % 3 == 0) {
                        doc.setStatus(DocumentStatus.VALIDATED);
                    } else if (i % 5 == 0) {
                        doc.setStatus(DocumentStatus.REJECTED);
                    }

                    // Random dates
                    doc.setUploadedAt(LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(1, 365)));

                    // Add tags to some documents
                    if (i % 4 == 0) {
                        doc.setTags(List.of("urgent", "important"));
                    }

                    return doc;
                })
                .toList();

        // Batch save for performance
        documentRepository.saveAll(documents);

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        System.out.println("Created " + size + " documents in " + duration + "ms");
    }

    /**
     * Test simple search query
     */
    private void testSimpleSearch() {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Long orderId = 100000L + ThreadLocalRandom.current().nextInt(LARGE_DATASET_SIZE / 10);

            Instant start = Instant.now();
            List<Document> results = documentRepository.findByOrderId(orderId);
            Instant end = Instant.now();

            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        printQueryStats("Simple Search (by Order ID)", queryTimes);
    }

    /**
     * Test filtered search query
     */
    private void testFilteredSearch() {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Instant start = Instant.now();
            List<Document> results = documentRepository.findByDocumentTypeAndStatus(
                    testDocumentType,
                    DocumentStatus.VALIDATED
            );
            Instant end = Instant.now();

            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        printQueryStats("Filtered Search (Type + Status)", queryTimes);
    }

    /**
     * Test full text search
     */
    private void testFullTextSearch() {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Instant start = Instant.now();
            List<Document> results = documentRepository.findByOriginalFilenameContaining("test");
            Instant end = Instant.now();

            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        printQueryStats("Full Text Search", queryTimes);
    }

    /**
     * Test complex search with multiple conditions
     */
    private void testComplexSearch() {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Instant start = Instant.now();

            // Complex search with multiple conditions
            List<Document> results = documentRepository.findByDocumentTypeAndStatusAndUploadedAtAfter(
                    testDocumentType,
                    DocumentStatus.VALIDATED,
                    LocalDateTime.now().minusDays(180)
            );

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        printQueryStats("Complex Search", queryTimes);
    }

    /**
     * Test paginated search
     */
    private void testPaginatedSearch() {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Pageable pageable = PageRequest.of(i, 20);

            Instant start = Instant.now();
            Page<Document> results = documentRepository.findByDocumentType(testDocumentType, pageable);
            Instant end = Instant.now();

            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        printQueryStats("Paginated Search", queryTimes);
    }

    /**
     * Test indexed field query performance
     */
    private void testIndexedFieldQuery(String fieldName) {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Instant start = Instant.now();

            switch (fieldName) {
                case "order_id" -> documentRepository.findByOrderId(100001L);
                case "document_type_id" -> documentRepository.findByDocumentType(testDocumentType);
                case "status" -> documentRepository.findByStatus(DocumentStatus.VALIDATED);
                case "uploaded_by" -> documentRepository.findByUploadedBy("test-user");
            }

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.println("Index on " + fieldName + " - Average query time: " + String.format("%.2f", avgTime) + "ms");
    }

    /**
     * Test compound index query
     */
    private void testCompoundIndexQuery() {
        List<Long> queryTimes = new ArrayList<>();

        for (int i = 0; i < QUERY_ITERATIONS; i++) {
            Instant start = Instant.now();
            List<Document> results = documentRepository.findByOrderIdAndDocumentType(100001L, testDocumentType);
            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            queryTimes.add(duration);
        }

        double avgTime = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.println("Compound index (order_id + type_id) - Average: " + String.format("%.2f", avgTime) + "ms");
    }

    /**
     * Print query statistics
     */
    private void printQueryStats(String queryType, List<Long> times) {
        times.sort(Long::compareTo);

        long min = times.get(0);
        long max = times.get(times.size() - 1);
        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long median = times.get(times.size() / 2);
        long p95 = times.get((int) (times.size() * 0.95));

        System.out.println("\n--- " + queryType + " Statistics ---");
        System.out.println("Queries executed: " + times.size());
        System.out.println("Min: " + min + "ms");
        System.out.println("Max: " + max + "ms");
        System.out.println("Avg: " + String.format("%.2f", avg) + "ms");
        System.out.println("Median: " + median + "ms");
        System.out.println("95th percentile: " + p95 + "ms");
        System.out.println("----------------------------------\n");
    }
}
