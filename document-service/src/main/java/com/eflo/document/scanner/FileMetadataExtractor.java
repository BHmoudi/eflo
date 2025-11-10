package com.eflo.document.scanner;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Component for extracting metadata from files using Apache Tika.
 * Supports various file formats including PDF, Office documents, images, and more.
 * Provides file type detection, metadata extraction, and file signature validation.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Extract metadata (author, title, creation date, page count)</li>
 *   <li>Detect file type using magic bytes</li>
 *   <li>Validate file type against expected types</li>
 *   <li>Get file signatures for integrity verification</li>
 * </ul>
 *
 * @author Eflo Document Service
 * @version 1.0
 */
@Slf4j
@Component
public class FileMetadataExtractor {

    private final Tika tika;
    private final AutoDetectParser parser;

    /**
     * Constructs a new FileMetadataExtractor with default Tika configuration.
     */
    public FileMetadataExtractor() {
        this.tika = new Tika();
        this.parser = new AutoDetectParser();
        log.info("FileMetadataExtractor initialized with Apache Tika");
    }

    /**
     * Extracts comprehensive metadata from a file.
     * Includes standard metadata like author, title, creation date, and format-specific details.
     *
     * @param file the file to extract metadata from
     * @return Map containing extracted metadata key-value pairs
     * @throws IOException if file reading fails
     */
    public Map<String, Object> extractMetadata(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null");
        }

        log.debug("Extracting metadata from file: {}", file.getName());

        Map<String, Object> metadataMap = new HashMap<>();

        try (InputStream stream = new FileInputStream(file)) {
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getName());

            BodyContentHandler handler = new BodyContentHandler(-1); // No limit on content
            ParseContext parseContext = new ParseContext();

            parser.parse(stream, handler, metadata, parseContext);

            // Extract common metadata
            extractCommonMetadata(metadata, metadataMap);

            // Extract PDF-specific metadata
            if (isPdfFile(file)) {
                extractPdfMetadata(file, metadataMap);
            }

            // Add file system metadata
            metadataMap.put("file_name", file.getName());
            metadataMap.put("file_size", file.length());
            metadataMap.put("file_path", file.getAbsolutePath());
            metadataMap.put("last_modified", LocalDateTime.ofInstant(
                    new Date(file.lastModified()).toInstant(), ZoneId.systemDefault()));

            // Add file signature
            metadataMap.put("file_signature", getFileSignature(file));

            // Add MIME type
            String mimeType = detectMimeType(file);
            metadataMap.put("mime_type", mimeType);
            metadataMap.put("detected_type", mimeType);

            log.info("Successfully extracted metadata from file: {} - Type: {}, Size: {} bytes",
                    file.getName(), mimeType, file.length());

            return metadataMap;

        } catch (SAXException | TikaException e) {
            log.error("Failed to parse file {} using Tika: {}", file.getName(), e.getMessage(), e);
            throw new IOException("Metadata extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts common metadata fields from Tika metadata object.
     *
     * @param metadata    the Tika metadata object
     * @param metadataMap the map to populate with extracted data
     */
    private void extractCommonMetadata(Metadata metadata, Map<String, Object> metadataMap) {
        // Author information
        String author = metadata.get(TikaCoreProperties.CREATOR);
        if (author == null) {
            author = metadata.get("Author");
        }
        if (author != null && !author.isEmpty()) {
            metadataMap.put("author", author);
        }

        // Title
        String title = metadata.get(TikaCoreProperties.TITLE);
        if (title != null && !title.isEmpty()) {
            metadataMap.put("title", title);
        }

        // Subject
        String subject = metadata.get(TikaCoreProperties.SUBJECT);
        if (subject != null && !subject.isEmpty()) {
            metadataMap.put("subject", subject);
        }

        // Creation date
        Date creationDate = metadata.getDate(TikaCoreProperties.CREATED);
        if (creationDate != null) {
            metadataMap.put("creation_date", LocalDateTime.ofInstant(
                    creationDate.toInstant(), ZoneId.systemDefault()));
        }

        // Modification date
        Date modifiedDate = metadata.getDate(TikaCoreProperties.MODIFIED);
        if (modifiedDate != null) {
            metadataMap.put("modified_date", LocalDateTime.ofInstant(
                    modifiedDate.toInstant(), ZoneId.systemDefault()));
        }

        // Keywords
        String keywords = metadata.get(TikaCoreProperties.SUBJECT);
        if (keywords != null && !keywords.isEmpty()) {
            metadataMap.put("keywords", keywords);
        }

        // Content type
        String contentType = metadata.get(Metadata.CONTENT_TYPE);
        if (contentType != null && !contentType.isEmpty()) {
            metadataMap.put("content_type", contentType);
        }

        // Language
        String language = metadata.get(TikaCoreProperties.LANGUAGE);
        if (language != null && !language.isEmpty()) {
            metadataMap.put("language", language);
        }

        // All metadata for debugging
        Map<String, String> allMetadata = new HashMap<>();
        for (String name : metadata.names()) {
            String value = metadata.get(name);
            if (value != null && !value.isEmpty()) {
                allMetadata.put(name, value);
            }
        }
        metadataMap.put("all_metadata", allMetadata);
    }

    /**
     * Extracts PDF-specific metadata including page count.
     *
     * @param file        the PDF file
     * @param metadataMap the map to populate with extracted data
     */
    private void extractPdfMetadata(File file, Map<String, Object> metadataMap) {
        try (InputStream stream = new FileInputStream(file)) {
            Metadata metadata = new Metadata();
            PDFParser pdfParser = new PDFParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            ParseContext parseContext = new ParseContext();

            pdfParser.parse(stream, handler, metadata, parseContext);

            // Extract page count
            String pageCount = metadata.get("xmpTPg:NPages");
            if (pageCount == null) {
                pageCount = metadata.get("pdf:pageCount");
            }
            if (pageCount == null) {
                pageCount = metadata.get("Page-Count");
            }

            if (pageCount != null) {
                try {
                    metadataMap.put("page_count", Integer.parseInt(pageCount));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse page count: {}", pageCount);
                }
            }

            // PDF version
            String pdfVersion = metadata.get("pdf:PDFVersion");
            if (pdfVersion != null && !pdfVersion.isEmpty()) {
                metadataMap.put("pdf_version", pdfVersion);
            }

            // PDF encryption
            String encrypted = metadata.get("pdf:encrypted");
            if (encrypted != null) {
                metadataMap.put("encrypted", Boolean.parseBoolean(encrypted));
            }

        } catch (Exception e) {
            log.debug("Failed to extract PDF-specific metadata: {}", e.getMessage());
        }
    }

    /**
     * Gets the file signature (magic bytes) from the beginning of a file.
     * Reads the first 16 bytes which typically contain the file signature.
     *
     * @param file the file to read signature from
     * @return hex string representation of file signature
     * @throws IOException if file reading fails
     */
    public String getFileSignature(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null");
        }

        byte[] signature = new byte[16];
        int bytesRead;

        try (InputStream stream = new FileInputStream(file)) {
            bytesRead = stream.read(signature);
        }

        if (bytesRead <= 0) {
            return "";
        }

        // Convert bytes to hex string
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytesRead; i++) {
            hexString.append(String.format("%02X", signature[i]));
        }

        return hexString.toString();
    }

    /**
     * Validates if a file matches the expected file type.
     * Compares the detected MIME type with the expected type.
     *
     * @param file         the file to validate
     * @param expectedType the expected MIME type (e.g., "application/pdf")
     * @return true if file type matches expected type, false otherwise
     * @throws IOException if file reading fails
     */
    public boolean isValidFileType(File file, String expectedType) throws IOException {
        if (file == null || expectedType == null) {
            return false;
        }

        String detectedType = detectMimeType(file);
        boolean isValid = expectedType.equalsIgnoreCase(detectedType);

        log.debug("File type validation - File: {}, Expected: {}, Detected: {}, Valid: {}",
                file.getName(), expectedType, detectedType, isValid);

        return isValid;
    }

    /**
     * Detects the MIME type of a file using Apache Tika.
     * Uses both file name and content for accurate detection.
     *
     * @param file the file to detect type for
     * @return detected MIME type
     * @throws IOException if file reading fails
     */
    public String detectMimeType(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null");
        }

        try {
            String mimeType = tika.detect(file);
            log.debug("Detected MIME type for file {}: {}", file.getName(), mimeType);
            return mimeType;
        } catch (Exception e) {
            log.error("Failed to detect MIME type for file {}: {}", file.getName(), e.getMessage());
            throw new IOException("MIME type detection failed", e);
        }
    }

    /**
     * Detects the file type from an input stream.
     * Uses content analysis for type detection.
     *
     * @param inputStream the input stream to analyze
     * @return detected MIME type
     * @throws IOException if stream reading fails
     */
    public String detectFileType(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException("InputStream is null");
        }

        try {
            String mimeType = tika.detect(inputStream);
            log.debug("Detected MIME type from stream: {}", mimeType);
            return mimeType;
        } catch (Exception e) {
            log.error("Failed to detect file type from stream: {}", e.getMessage());
            throw new IOException("File type detection failed", e);
        }
    }

    /**
     * Detects file type from an input stream with filename hint.
     *
     * @param inputStream the input stream to analyze
     * @param fileName    the filename to use as a hint
     * @return detected MIME type
     * @throws IOException if stream reading fails
     */
    public String detectFileType(InputStream inputStream, String fileName) throws IOException {
        if (inputStream == null) {
            throw new IOException("InputStream is null");
        }

        try {
            Metadata metadata = new Metadata();
            if (fileName != null) {
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
            }

            String mimeType = tika.detect(inputStream, metadata);
            log.debug("Detected MIME type for {}: {}", fileName, mimeType);
            return mimeType;
        } catch (Exception e) {
            log.error("Failed to detect file type: {}", e.getMessage());
            throw new IOException("File type detection failed", e);
        }
    }

    /**
     * Checks if a file is a PDF based on its content.
     *
     * @param file the file to check
     * @return true if file is a PDF, false otherwise
     */
    private boolean isPdfFile(File file) {
        try {
            String mimeType = detectMimeType(file);
            return "application/pdf".equalsIgnoreCase(mimeType);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Extracts text content from a file (first 1000 characters).
     * Useful for content-based indexing and search.
     *
     * @param file the file to extract text from
     * @return extracted text content
     * @throws IOException if file reading fails
     */
    public String extractTextContent(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null");
        }

        try (InputStream stream = new FileInputStream(file)) {
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getName());

            BodyContentHandler handler = new BodyContentHandler(1000); // Limit to 1000 chars
            ParseContext parseContext = new ParseContext();

            parser.parse(stream, handler, metadata, parseContext);

            String content = handler.toString().trim();
            log.debug("Extracted {} characters of text from file: {}", content.length(), file.getName());

            return content;

        } catch (SAXException | TikaException e) {
            log.error("Failed to extract text content from file {}: {}", file.getName(), e.getMessage());
            throw new IOException("Text extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets supported file types by Apache Tika.
     *
     * @return set of supported MIME types
     */
    public Set<String> getSupportedFileTypes() {
        // Common supported types
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add("application/pdf");
        supportedTypes.add("application/msword");
        supportedTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        supportedTypes.add("application/vnd.ms-excel");
        supportedTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        supportedTypes.add("image/jpeg");
        supportedTypes.add("image/png");
        supportedTypes.add("image/gif");
        supportedTypes.add("text/plain");
        supportedTypes.add("text/html");
        supportedTypes.add("application/xml");
        supportedTypes.add("application/json");

        return supportedTypes;
    }
}
