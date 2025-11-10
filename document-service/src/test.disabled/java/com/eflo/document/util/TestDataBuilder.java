package com.eflo.document.util;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builder utility for creating test data
 */
public class TestDataBuilder {

    private static final Random RANDOM = new Random();

    /**
     * Create a test Document entity with default values
     */
    public static Document createTestDocument() {
        return createTestDocument(null, null);
    }

    /**
     * Create a test Document entity with specified type and order ID
     */
    public static Document createTestDocument(DocumentType documentType, Long orderId) {
        UUID uuid = UUID.randomUUID();
        String randomSuffix = generateRandomString(6);

        return Document.builder()
                .documentUuid(uuid)
                .documentType(documentType)
                .typeCode(documentType != null ? documentType.getTypeCode() : "TEST_DOC")
                .orderId(orderId != null ? orderId : randomLong())
                .orderNumber(orderId != null ? "ORD-" + orderId : "ORD-" + randomLong())
                .originalFilename("test-document-" + randomSuffix + ".pdf")
                .storedFilename(uuid.toString() + ".pdf")
                .fileExtension("pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(randomLong(1024, 1024 * 1024))
                .fileHash(generateRandomHash())
                .storageBucket("test-documents")
                .storagePath("documents/" + LocalDate.now().getYear() + "/" + uuid + ".pdf")
                .storageRegion("us-east-1")
                .version(1)
                .isLatestVersion(true)
                .status(DocumentStatus.PENDING)
                .virusScanStatus(VirusScanStatus.PENDING)
                .isConfidential(false)
                .accessLevel(AccessLevel.STANDARD)
                .encryptionEnabled(false)
                .businessUnitId(randomLong(1, 100))
                .uploadedBy("test-user-" + randomSuffix)
                .uploadedAt(LocalDateTime.now())
                .createdBy("test-user-" + randomSuffix)
                .createdAt(LocalDateTime.now())
                .customMetadata(new HashMap<>())
                .tags(new ArrayList<>())
                .expirationNotified(false)
                .build();
    }

    /**
     * Create a test DocumentType with default values
     */
    public static DocumentType createTestDocumentType() {
        return createTestDocumentType("TEST_TYPE_" + generateRandomString(4));
    }

    /**
     * Create a test DocumentType with specified code
     */
    public static DocumentType createTestDocumentType(String typeCode) {
        return DocumentType.builder()
                .typeCode(typeCode)
                .typeName("Test Document Type - " + typeCode)
                .description("Test document type for testing purposes")
                .category(DocumentCategory.ORDER)
                .isMandatory(false)
                .minDocuments(0)
                .maxDocuments(10)
                .allowedFormats(Arrays.asList("pdf", "jpg", "png", "doc", "docx"))
                .maxFileSizeMb(BigDecimal.valueOf(10.0))
                .requiresValidation(true)
                .validatorRoles(Arrays.asList("ROLE_VALIDATOR", "ROLE_ADMIN"))
                .autoValidateConditions(new HashMap<>())
                .hasExpiration(false)
                .expirationWarningDays(30)
                .customValidationRules(new HashMap<>())
                .metadataSchema(new HashMap<>())
                .isActive(true)
                .displayOrder(0)
                .createdBy("test-system")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a mandatory DocumentType
     */
    public static DocumentType createMandatoryDocumentType(String typeCode, int minDocuments) {
        DocumentType docType = createTestDocumentType(typeCode);
        docType.setIsMandatory(true);
        docType.setMinDocuments(minDocuments);
        return docType;
    }

    /**
     * Create a DocumentType with expiration
     */
    public static DocumentType createExpiringDocumentType(String typeCode, int validityDays) {
        DocumentType docType = createTestDocumentType(typeCode);
        docType.setHasExpiration(true);
        docType.setDefaultValidityDays(validityDays);
        docType.setExpirationWarningDays(30);
        return docType;
    }

    /**
     * Create a test MultipartFile with text content
     */
    public static MockMultipartFile createTestMultipartFile() {
        return createTestMultipartFile("test-file.txt", "text/plain", "Test file content".getBytes());
    }

    /**
     * Create a test MultipartFile with specified parameters
     */
    public static MockMultipartFile createTestMultipartFile(String filename, String contentType, byte[] content) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                content
        );
    }

    /**
     * Create a test PDF file as MultipartFile
     */
    public static MockMultipartFile createTestPdfFile() throws IOException {
        return createTestPdfFile("test-document.pdf");
    }

    /**
     * Create a test PDF file with specified filename
     */
    public static MockMultipartFile createTestPdfFile(String filename) throws IOException {
        byte[] pdfBytes = generatePdfBytes("Test PDF Document", "This is a test PDF for testing purposes.");
        return new MockMultipartFile(
                "file",
                filename,
                "application/pdf",
                pdfBytes
        );
    }

    /**
     * Create a test image file as MultipartFile
     */
    public static MockMultipartFile createTestImageFile() throws IOException {
        return createTestImageFile("test-image.jpg");
    }

    /**
     * Create a test image file with specified filename
     */
    public static MockMultipartFile createTestImageFile(String filename) throws IOException {
        byte[] imageBytes = generateJpegBytes(200, 200);
        return new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                imageBytes
        );
    }

    /**
     * Create a large test file for size validation testing
     */
    public static MockMultipartFile createLargeTestFile(long sizeInBytes) {
        byte[] content = new byte[(int) sizeInBytes];
        RANDOM.nextBytes(content);
        return new MockMultipartFile(
                "file",
                "large-file.bin",
                "application/octet-stream",
                content
        );
    }

    /**
     * Generate PDF bytes with specified content
     */
    public static byte[] generatePdfBytes(String title, String content) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(title);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(content);
                contentStream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate JPEG image bytes
     */
    public static byte[] generateJpegBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill with gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, Color.BLUE,
                width, height, Color.GREEN
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // Draw some text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("TEST", width / 2 - 30, height / 2);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    /**
     * Generate PNG image bytes
     */
    public static byte[] generatePngBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Fill with solid color
        g2d.setColor(Color.CYAN);
        g2d.fillRect(0, 0, width, height);

        // Draw a border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width - 1, height - 1);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    /**
     * Create a document with validated status
     */
    public static Document createValidatedDocument(DocumentType documentType, Long orderId) {
        Document document = createTestDocument(documentType, orderId);
        document.setStatus(DocumentStatus.VALIDATED);
        document.setValidatedBy("test-validator");
        document.setValidatedAt(LocalDateTime.now());
        document.setVirusScanStatus(VirusScanStatus.CLEAN);
        document.setVirusScanDate(LocalDateTime.now());
        return document;
    }

    /**
     * Create a document with rejected status
     */
    public static Document createRejectedDocument(DocumentType documentType, Long orderId, String reason) {
        Document document = createTestDocument(documentType, orderId);
        document.setStatus(DocumentStatus.REJECTED);
        document.setValidatedBy("test-validator");
        document.setValidatedAt(LocalDateTime.now());
        document.setStatusReason(reason);
        return document;
    }

    /**
     * Create a document with expiration
     */
    public static Document createExpiringDocument(DocumentType documentType, Long orderId, int daysUntilExpiration) {
        Document document = createTestDocument(documentType, orderId);
        document.setExpirationDate(LocalDate.now().plusDays(daysUntilExpiration));
        return document;
    }

    /**
     * Create an expired document
     */
    public static Document createExpiredDocument(DocumentType documentType, Long orderId) {
        Document document = createTestDocument(documentType, orderId);
        document.setExpirationDate(LocalDate.now().minusDays(1));
        document.setStatus(DocumentStatus.EXPIRED);
        return document;
    }

    /**
     * Create a document with virus detected
     */
    public static Document createInfectedDocument(DocumentType documentType, Long orderId) {
        Document document = createTestDocument(documentType, orderId);
        document.setVirusScanStatus(VirusScanStatus.INFECTED);
        document.setVirusScanDate(LocalDateTime.now());
        Map<String, Object> scanResult = new HashMap<>();
        scanResult.put("virus", "EICAR-Test-File");
        scanResult.put("description", "Test virus signature detected");
        document.setVirusScanResult(scanResult);
        return document;
    }

    /**
     * Create a confidential document
     */
    public static Document createConfidentialDocument(DocumentType documentType, Long orderId) {
        Document document = createTestDocument(documentType, orderId);
        document.setIsConfidential(true);
        document.setAccessLevel(AccessLevel.CONFIDENTIAL);
        document.setEncryptionEnabled(true);
        return document;
    }

    /**
     * Generate a random string of specified length
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generate a random hash string
     */
    public static String generateRandomHash() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a random long value
     */
    public static Long randomLong() {
        return randomLong(1, 1000000);
    }

    /**
     * Generate a random long value within range
     */
    public static Long randomLong(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    /**
     * Generate a random integer within range
     */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Create a list of test documents
     */
    public static List<Document> createTestDocuments(int count, DocumentType documentType, Long orderId) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            documents.add(createTestDocument(documentType, orderId));
        }
        return documents;
    }

    /**
     * Create metadata map for testing
     */
    public static Map<String, Object> createTestMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("test_key_1", "test_value_1");
        metadata.put("test_key_2", 12345);
        metadata.put("test_key_3", true);
        metadata.put("timestamp", LocalDateTime.now().toString());
        return metadata;
    }

    /**
     * Create tags list for testing
     */
    public static List<String> createTestTags() {
        return Arrays.asList("test", "automated", "sample");
    }
}
