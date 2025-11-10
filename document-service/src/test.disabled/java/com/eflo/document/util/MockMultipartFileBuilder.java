package com.eflo.document.util;

import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Builder for creating MockMultipartFile instances with various configurations
 */
public class MockMultipartFileBuilder {

    private String fieldName = "file";
    private String originalFilename = "test-file.txt";
    private String contentType = "text/plain";
    private byte[] content = "Test content".getBytes(StandardCharsets.UTF_8);

    private static final Random RANDOM = new Random();

    public MockMultipartFileBuilder() {
    }

    /**
     * Set the field name
     */
    public MockMultipartFileBuilder fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    /**
     * Set the original filename
     */
    public MockMultipartFileBuilder filename(String filename) {
        this.originalFilename = filename;
        return this;
    }

    /**
     * Set the content type
     */
    public MockMultipartFileBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the content as bytes
     */
    public MockMultipartFileBuilder content(byte[] content) {
        this.content = content;
        return this;
    }

    /**
     * Set the content as string
     */
    public MockMultipartFileBuilder content(String content) {
        this.content = content.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Set content size (random bytes)
     */
    public MockMultipartFileBuilder contentSize(int sizeInBytes) {
        this.content = new byte[sizeInBytes];
        RANDOM.nextBytes(this.content);
        return this;
    }

    /**
     * Build a PDF file
     */
    public MockMultipartFileBuilder pdf() throws IOException {
        this.originalFilename = "document.pdf";
        this.contentType = "application/pdf";
        this.content = TestDataBuilder.generatePdfBytes("Test PDF", "This is a test PDF document.");
        return this;
    }

    /**
     * Build a PDF file with custom content
     */
    public MockMultipartFileBuilder pdf(String title, String content) throws IOException {
        this.originalFilename = "document.pdf";
        this.contentType = "application/pdf";
        this.content = TestDataBuilder.generatePdfBytes(title, content);
        return this;
    }

    /**
     * Build a JPEG image file
     */
    public MockMultipartFileBuilder jpeg() throws IOException {
        return jpeg(200, 200);
    }

    /**
     * Build a JPEG image file with specified dimensions
     */
    public MockMultipartFileBuilder jpeg(int width, int height) throws IOException {
        this.originalFilename = "image.jpg";
        this.contentType = "image/jpeg";
        this.content = TestDataBuilder.generateJpegBytes(width, height);
        return this;
    }

    /**
     * Build a PNG image file
     */
    public MockMultipartFileBuilder png() throws IOException {
        return png(200, 200);
    }

    /**
     * Build a PNG image file with specified dimensions
     */
    public MockMultipartFileBuilder png(int width, int height) throws IOException {
        this.originalFilename = "image.png";
        this.contentType = "image/png";
        this.content = TestDataBuilder.generatePngBytes(width, height);
        return this;
    }

    /**
     * Build a text file
     */
    public MockMultipartFileBuilder text() {
        this.originalFilename = "document.txt";
        this.contentType = "text/plain";
        this.content = "This is a test text file.\nLine 2\nLine 3".getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Build a text file with custom content
     */
    public MockMultipartFileBuilder text(String content) {
        this.originalFilename = "document.txt";
        this.contentType = "text/plain";
        this.content = content.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Build a Word document (fake DOCX)
     */
    public MockMultipartFileBuilder docx() {
        this.originalFilename = "document.docx";
        this.contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        // Minimal ZIP structure for DOCX (not a real DOCX but passes basic checks)
        this.content = createFakeZipContent();
        return this;
    }

    /**
     * Build an Excel document (fake XLSX)
     */
    public MockMultipartFileBuilder xlsx() {
        this.originalFilename = "spreadsheet.xlsx";
        this.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        // Minimal ZIP structure for XLSX (not a real XLSX but passes basic checks)
        this.content = createFakeZipContent();
        return this;
    }

    /**
     * Build a CSV file
     */
    public MockMultipartFileBuilder csv() {
        return csv("Name,Age,City\nJohn,30,NYC\nJane,25,LA");
    }

    /**
     * Build a CSV file with custom content
     */
    public MockMultipartFileBuilder csv(String csvContent) {
        this.originalFilename = "data.csv";
        this.contentType = "text/csv";
        this.content = csvContent.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Build a JSON file
     */
    public MockMultipartFileBuilder json() {
        return json("{\"test\": true, \"data\": \"value\"}");
    }

    /**
     * Build a JSON file with custom content
     */
    public MockMultipartFileBuilder json(String jsonContent) {
        this.originalFilename = "data.json";
        this.contentType = "application/json";
        this.content = jsonContent.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Build a XML file
     */
    public MockMultipartFileBuilder xml() {
        return xml("<?xml version=\"1.0\"?><root><item>value</item></root>");
    }

    /**
     * Build a XML file with custom content
     */
    public MockMultipartFileBuilder xml(String xmlContent) {
        this.originalFilename = "data.xml";
        this.contentType = "application/xml";
        this.content = xmlContent.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Build a ZIP file (minimal valid structure)
     */
    public MockMultipartFileBuilder zip() {
        this.originalFilename = "archive.zip";
        this.contentType = "application/zip";
        this.content = createFakeZipContent();
        return this;
    }

    /**
     * Build an EICAR test virus file
     * EICAR is a standard anti-virus test file that is safe but detected as malware
     */
    public MockMultipartFileBuilder eicar() {
        this.originalFilename = "eicar.txt";
        this.contentType = "text/plain";
        // EICAR test string - standard antivirus test file
        this.content = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"
                .getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Build an invalid/corrupted file
     */
    public MockMultipartFileBuilder corrupted() {
        this.originalFilename = "corrupted.pdf";
        this.contentType = "application/pdf";
        // Random bytes that don't form a valid PDF
        byte[] randomBytes = new byte[100];
        RANDOM.nextBytes(randomBytes);
        this.content = randomBytes;
        return this;
    }

    /**
     * Build an empty file
     */
    public MockMultipartFileBuilder empty() {
        this.content = new byte[0];
        return this;
    }

    /**
     * Build a file with specified size in MB
     */
    public MockMultipartFileBuilder sizeInMB(double mb) {
        int sizeInBytes = (int) (mb * 1024 * 1024);
        return contentSize(sizeInBytes);
    }

    /**
     * Build a file with specified size in KB
     */
    public MockMultipartFileBuilder sizeInKB(double kb) {
        int sizeInBytes = (int) (kb * 1024);
        return contentSize(sizeInBytes);
    }

    /**
     * Build the MockMultipartFile
     */
    public MockMultipartFile build() {
        return new MockMultipartFile(
                fieldName,
                originalFilename,
                contentType,
                content
        );
    }

    /**
     * Create a minimal ZIP file structure
     */
    private byte[] createFakeZipContent() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Minimal ZIP file header
            // This is a simplified ZIP structure - just enough to be recognized as ZIP
            byte[] header = new byte[]{
                    0x50, 0x4B, 0x03, 0x04, // Local file header signature
                    0x0A, 0x00, 0x00, 0x00, // Version, flags
                    0x00, 0x00, 0x00, 0x00, // Compression method, mod time, mod date
                    0x00, 0x00, 0x00, 0x00, // CRC-32
                    0x00, 0x00, 0x00, 0x00, // Compressed size
                    0x00, 0x00, 0x00, 0x00, // Uncompressed size
                    0x08, 0x00, 0x00, 0x00, // File name length, extra field length
                    't', 'e', 's', 't', '.', 't', 'x', 't' // File name
            };
            baos.write(header);

            // Central directory
            byte[] centralDir = new byte[]{
                    0x50, 0x4B, 0x01, 0x02, // Central directory header signature
                    0x14, 0x00, 0x0A, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x08, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00,
                    't', 'e', 's', 't', '.', 't', 'x', 't'
            };
            baos.write(centralDir);

            // End of central directory
            byte[] endOfCentralDir = new byte[]{
                    0x50, 0x4B, 0x05, 0x06, // End of central directory signature
                    0x00, 0x00, 0x00, 0x00,
                    0x01, 0x00, 0x01, 0x00,
                    0x36, 0x00, 0x00, 0x00,
                    0x1A, 0x00, 0x00, 0x00,
                    0x00, 0x00
            };
            baos.write(endOfCentralDir);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create fake ZIP content", e);
        }
    }

    /**
     * Static factory method for fluent API
     */
    public static MockMultipartFileBuilder builder() {
        return new MockMultipartFileBuilder();
    }
}
