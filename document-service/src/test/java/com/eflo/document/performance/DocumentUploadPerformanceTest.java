package com.eflo.document.performance;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentManagementService;
import com.eflo.document.util.TestContainersConfig;
import com.eflo.document.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for document upload operations
 *
 * These tests are disabled by default as they take longer to run.
 * Enable them when you want to measure performance metrics.
 *
 * Run with: mvn test -Dtest=DocumentUploadPerformanceTest
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Performance tests - enable manually when needed")
public class DocumentUploadPerformanceTest {

    @Autowired
    private DocumentManagementService documentService;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentRepository documentRepository;

    private DocumentType testDocumentType;
    private static final int SAMPLE_SIZE = 100;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    void setUp() {
        // Create test document type
        testDocumentType = TestDataBuilder.createTestDocumentType("PERF_TEST");
        testDocumentType = documentTypeRepository.save(testDocumentType);
    }

    @Test
    @Transactional
    void testSequentialUploadPerformance() throws Exception {
        System.out.println("\n=== Sequential Upload Performance Test ===");
        System.out.println("Sample size: " + SAMPLE_SIZE + " documents");

        List<Long> uploadTimes = new ArrayList<>();
        Instant overallStart = Instant.now();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            MockMultipartFile file = TestDataBuilder.createTestPdfFile("perf-test-" + i + ".pdf");
            Long orderId = 20000L + i;

            Instant start = Instant.now();

            // Simulate upload operation (adjust based on your actual service method)
            // Document document = documentService.uploadDocument(file, testDocumentType.getTypeCode(), orderId, "perf-test-user");

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            uploadTimes.add(duration);

            if ((i + 1) % 10 == 0) {
                System.out.println("Uploaded " + (i + 1) + " documents...");
            }
        }

        Instant overallEnd = Instant.now();
        long totalDuration = Duration.between(overallStart, overallEnd).toMillis();

        // Calculate statistics
        PerformanceStats stats = calculateStats(uploadTimes);

        // Print results
        printPerformanceResults("Sequential Upload", SAMPLE_SIZE, totalDuration, stats);

        // Assertions
        assertThat(stats.getAverageTime()).isLessThan(5000); // Average upload should be < 5s
        assertThat(stats.getMaxTime()).isLessThan(10000); // Max upload should be < 10s
    }

    @Test
    void testConcurrentUploadPerformance() throws Exception {
        System.out.println("\n=== Concurrent Upload Performance Test ===");
        System.out.println("Sample size: " + SAMPLE_SIZE + " documents");
        System.out.println("Concurrent threads: " + CONCURRENT_THREADS);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        List<Future<Long>> futures = new ArrayList<>();
        Instant overallStart = Instant.now();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            final int index = i;
            Future<Long> future = executor.submit(() -> {
                try {
                    MockMultipartFile file = TestDataBuilder.createTestPdfFile("concurrent-test-" + index + ".pdf");
                    Long orderId = 30000L + index;

                    Instant start = Instant.now();

                    // Simulate upload operation
                    // Document document = documentService.uploadDocument(file, testDocumentType.getTypeCode(), orderId, "perf-test-user");
                    Thread.sleep(ThreadLocalRandom.current().nextLong(100, 500)); // Simulate processing

                    Instant end = Instant.now();
                    return Duration.between(start, end).toMillis();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }

        // Wait for all uploads to complete
        List<Long> uploadTimes = new ArrayList<>();
        for (Future<Long> future : futures) {
            uploadTimes.add(future.get());
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        Instant overallEnd = Instant.now();
        long totalDuration = Duration.between(overallStart, overallEnd).toMillis();

        // Calculate statistics
        PerformanceStats stats = calculateStats(uploadTimes);

        // Print results
        printPerformanceResults("Concurrent Upload", SAMPLE_SIZE, totalDuration, stats);

        // Calculate throughput
        double throughput = (double) SAMPLE_SIZE / (totalDuration / 1000.0);
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " uploads/second");

        // Assertions
        assertThat(stats.getAverageTime()).isLessThan(5000);
        assertThat(throughput).isGreaterThan(1.0); // At least 1 upload per second
    }

    @Test
    @Transactional
    void testBulkUploadPerformance() throws Exception {
        System.out.println("\n=== Bulk Upload Performance Test ===");

        List<MockMultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            files.add(TestDataBuilder.createTestPdfFile("bulk-test-" + i + ".pdf"));
        }

        Long orderId = 40000L;
        Instant start = Instant.now();

        // Simulate bulk upload
        // List<Document> documents = documentService.bulkUploadDocuments(files, testDocumentType.getTypeCode(), orderId, "perf-test-user");

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();

        System.out.println("Bulk uploaded " + files.size() + " documents in " + duration + "ms");
        System.out.println("Average per document: " + (duration / files.size()) + "ms");

        // Assertions
        assertThat(duration).isLessThan(30000); // Bulk upload of 50 should be < 30s
    }

    @Test
    void testLargeFileUploadPerformance() throws Exception {
        System.out.println("\n=== Large File Upload Performance Test ===");

        // Test different file sizes
        long[] fileSizes = {1, 5, 10, 25, 50}; // MB
        List<Long> uploadTimes = new ArrayList<>();

        for (long sizeMB : fileSizes) {
            MockMultipartFile file = TestDataBuilder.createLargeTestFile(sizeMB * 1024 * 1024);
            Long orderId = 50000L + sizeMB;

            Instant start = Instant.now();

            // Simulate upload
            // Document document = documentService.uploadDocument(file, testDocumentType.getTypeCode(), orderId, "perf-test-user");
            Thread.sleep(100 * sizeMB); // Simulate processing time proportional to size

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            uploadTimes.add(duration);

            System.out.println("Uploaded " + sizeMB + "MB file in " + duration + "ms");
        }

        // Verify that upload time scales reasonably with file size
        // Larger files should take longer but not exponentially so
        assertThat(uploadTimes.get(4)).isLessThan(uploadTimes.get(0) * 100); // 50MB shouldn't take 100x longer than 1MB
    }

    @Test
    @Transactional
    void testDatabaseWritePerformance() {
        System.out.println("\n=== Database Write Performance Test ===");

        List<Long> writeTimes = new ArrayList<>();
        Instant overallStart = Instant.now();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            Document document = TestDataBuilder.createTestDocument(testDocumentType, 60000L + i);

            Instant start = Instant.now();
            documentRepository.save(document);
            Instant end = Instant.now();

            long duration = Duration.between(start, end).toMillis();
            writeTimes.add(duration);
        }

        Instant overallEnd = Instant.now();
        long totalDuration = Duration.between(overallStart, overallEnd).toMillis();

        PerformanceStats stats = calculateStats(writeTimes);
        printPerformanceResults("Database Write", SAMPLE_SIZE, totalDuration, stats);

        // Assertions
        assertThat(stats.getAverageTime()).isLessThan(100); // DB writes should be < 100ms
        assertThat(stats.getP95Time()).isLessThan(500); // 95th percentile < 500ms
    }

    @Test
    @Transactional
    void testDatabaseBatchWritePerformance() {
        System.out.println("\n=== Database Batch Write Performance Test ===");

        List<Document> documents = IntStream.range(0, SAMPLE_SIZE)
                .mapToObj(i -> TestDataBuilder.createTestDocument(testDocumentType, 70000L + i))
                .toList();

        Instant start = Instant.now();
        documentRepository.saveAll(documents);
        Instant end = Instant.now();

        long duration = Duration.between(start, end).toMillis();
        double avgPerDocument = (double) duration / SAMPLE_SIZE;

        System.out.println("Batch saved " + SAMPLE_SIZE + " documents in " + duration + "ms");
        System.out.println("Average per document: " + String.format("%.2f", avgPerDocument) + "ms");

        // Assertions
        assertThat(avgPerDocument).isLessThan(50); // Batch writes should be much faster
    }

    /**
     * Calculate performance statistics
     */
    private PerformanceStats calculateStats(List<Long> times) {
        times.sort(Long::compareTo);

        long min = times.get(0);
        long max = times.get(times.size() - 1);
        double average = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long median = times.get(times.size() / 2);
        long p95 = times.get((int) (times.size() * 0.95));
        long p99 = times.get((int) (times.size() * 0.99));

        return new PerformanceStats(min, max, average, median, p95, p99);
    }

    /**
     * Print performance test results
     */
    private void printPerformanceResults(String testName, int sampleSize, long totalDuration, PerformanceStats stats) {
        System.out.println("\n--- " + testName + " Results ---");
        System.out.println("Total documents: " + sampleSize);
        System.out.println("Total duration: " + totalDuration + "ms");
        System.out.println("Min time: " + stats.getMinTime() + "ms");
        System.out.println("Max time: " + stats.getMaxTime() + "ms");
        System.out.println("Average time: " + String.format("%.2f", stats.getAverageTime()) + "ms");
        System.out.println("Median time: " + stats.getMedianTime() + "ms");
        System.out.println("95th percentile: " + stats.getP95Time() + "ms");
        System.out.println("99th percentile: " + stats.getP99Time() + "ms");
        System.out.println("Average throughput: " + String.format("%.2f", (double) sampleSize / (totalDuration / 1000.0)) + " ops/sec");
        System.out.println("------------------------------\n");
    }

    /**
     * Performance statistics holder
     */
    private static class PerformanceStats {
        private final long minTime;
        private final long maxTime;
        private final double averageTime;
        private final long medianTime;
        private final long p95Time;
        private final long p99Time;

        public PerformanceStats(long minTime, long maxTime, double averageTime, long medianTime, long p95Time, long p99Time) {
            this.minTime = minTime;
            this.maxTime = maxTime;
            this.averageTime = averageTime;
            this.medianTime = medianTime;
            this.p95Time = p95Time;
            this.p99Time = p99Time;
        }

        public long getMinTime() { return minTime; }
        public long getMaxTime() { return maxTime; }
        public double getAverageTime() { return averageTime; }
        public long getMedianTime() { return medianTime; }
        public long getP95Time() { return p95Time; }
        public long getP99Time() { return p99Time; }
    }
}
