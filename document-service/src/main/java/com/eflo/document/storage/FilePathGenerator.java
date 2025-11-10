package com.eflo.document.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * File Path Generator for MinIO storage.
 * Generates organized file paths for different document types and purposes.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Component
public class FilePathGenerator {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    /**
     * Generates a storage path for order documents.
     * Pattern: orders/{year}/{month}/{orderNumber}/{typeCode}/{filename}
     *
     * @param orderId the order ID
     * @param orderNumber the order number
     * @param typeCode the document type code
     * @param filename the original filename
     * @return the generated storage path
     */
    public String generateOrderDocumentPath(Long orderId, String orderNumber, String typeCode, String filename) {
        log.debug("Generating order document path for order: {}, type: {}, file: {}",
                orderNumber, typeCode, filename);

        LocalDateTime now = LocalDateTime.now();
        String year = now.format(YEAR_FORMATTER);
        String month = now.format(MONTH_FORMATTER);

        // Sanitize inputs
        String sanitizedOrderNumber = sanitize(orderNumber);
        String sanitizedTypeCode = sanitize(typeCode);
        String sanitizedFilename = sanitizeFilename(filename);

        String path = String.format("orders/%s/%s/%s/%s/%s",
                year,
                month,
                sanitizedOrderNumber,
                sanitizedTypeCode,
                sanitizedFilename
        );

        log.debug("Generated order document path: {}", path);
        return path;
    }

    /**
     * Generates a storage path for temporary files.
     * Pattern: temp/{typeCode}/{uuid}/{filename}
     *
     * @param typeCode the document type code
     * @param filename the original filename
     * @return the generated storage path
     */
    public String generateTempPath(String typeCode, String filename) {
        log.debug("Generating temp path for type: {}, file: {}", typeCode, filename);

        String uuid = UUID.randomUUID().toString();
        String sanitizedTypeCode = sanitize(typeCode);
        String sanitizedFilename = sanitizeFilename(filename);

        String path = String.format("temp/%s/%s/%s",
                sanitizedTypeCode,
                uuid,
                sanitizedFilename
        );

        log.debug("Generated temp path: {}", path);
        return path;
    }

    /**
     * Generates a storage path for archived documents.
     * Pattern: archive/{year}/{orderId}/{typeCode}/{filename}
     *
     * @param orderId the order ID
     * @param orderNumber the order number (for logging)
     * @param typeCode the document type code
     * @param filename the original filename
     * @return the generated storage path
     */
    public String generateArchivePath(Long orderId, String orderNumber, String typeCode, String filename) {
        log.debug("Generating archive path for order: {}, type: {}, file: {}",
                orderNumber, typeCode, filename);

        LocalDateTime now = LocalDateTime.now();
        String year = now.format(YEAR_FORMATTER);

        String sanitizedTypeCode = sanitize(typeCode);
        String sanitizedFilename = sanitizeFilename(filename);

        String path = String.format("archive/%s/%s/%s/%s",
                year,
                orderId,
                sanitizedTypeCode,
                sanitizedFilename
        );

        log.debug("Generated archive path: {}", path);
        return path;
    }

    /**
     * Generates a versioned path for document versions.
     * Pattern: versions/{documentId}/{version}/{filename}
     *
     * @param documentId the document ID
     * @param version the version number
     * @param filename the original filename
     * @return the generated storage path
     */
    public String generateVersionPath(Long documentId, int version, String filename) {
        log.debug("Generating version path for document: {}, version: {}, file: {}",
                documentId, version, filename);

        String sanitizedFilename = sanitizeFilename(filename);

        String path = String.format("versions/%s/%d/%s",
                documentId,
                version,
                sanitizedFilename
        );

        log.debug("Generated version path: {}", path);
        return path;
    }

    /**
     * Generates a path for thumbnails.
     * Pattern: thumbnails/{year}/{month}/{documentId}/{filename}
     *
     * @param documentId the document ID
     * @param filename the original filename (will add thumbnail suffix)
     * @return the generated storage path
     */
    public String generateThumbnailPath(Long documentId, String filename) {
        log.debug("Generating thumbnail path for document: {}, file: {}", documentId, filename);

        LocalDateTime now = LocalDateTime.now();
        String year = now.format(YEAR_FORMATTER);
        String month = now.format(MONTH_FORMATTER);

        String baseName = FilenameUtils.getBaseName(filename);
        String extension = FilenameUtils.getExtension(filename);
        String thumbnailFilename = String.format("%s_thumb.%s", sanitize(baseName), extension);

        String path = String.format("thumbnails/%s/%s/%s/%s",
                year,
                month,
                documentId,
                thumbnailFilename
        );

        log.debug("Generated thumbnail path: {}", path);
        return path;
    }

    /**
     * Generates a unique filename with UUID prefix.
     *
     * @param originalFilename the original filename
     * @return filename with UUID prefix
     */
    public String generateUniqueFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String sanitizedFilename = sanitizeFilename(originalFilename);

        String baseName = FilenameUtils.getBaseName(sanitizedFilename);
        String extension = FilenameUtils.getExtension(sanitizedFilename);

        String uniqueFilename = String.format("%s_%s.%s", uuid, baseName, extension);
        log.debug("Generated unique filename: {} from {}", uniqueFilename, originalFilename);

        return uniqueFilename;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the file extension (without dot) or empty string if none
     */
    public String getExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }

        String extension = FilenameUtils.getExtension(filename);
        return extension != null ? extension.toLowerCase() : "";
    }

    /**
     * Gets the base name (filename without extension).
     *
     * @param filename the filename
     * @return the base name
     */
    public String getBaseName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }

        return FilenameUtils.getBaseName(filename);
    }

    /**
     * Sanitizes a filename by removing or replacing invalid characters.
     *
     * @param filename the filename to sanitize
     * @return sanitized filename
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed_file";
        }

        // Replace spaces with underscores
        String sanitized = filename.trim().replaceAll("\\s+", "_");

        // Remove or replace special characters (keep only alphanumeric, dots, dashes, underscores)
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "");

        // Ensure filename is not empty after sanitization
        if (sanitized.isEmpty()) {
            sanitized = "unnamed_file";
        }

        // Limit filename length (keep extension)
        String baseName = FilenameUtils.getBaseName(sanitized);
        String extension = FilenameUtils.getExtension(sanitized);

        if (baseName.length() > 200) {
            baseName = baseName.substring(0, 200);
        }

        return extension.isEmpty() ? baseName : baseName + "." + extension;
    }

    /**
     * Sanitizes a path component by removing or replacing invalid characters.
     *
     * @param component the path component to sanitize
     * @return sanitized component
     */
    private String sanitize(String component) {
        if (component == null || component.trim().isEmpty()) {
            return "unknown";
        }

        // Replace spaces with underscores
        String sanitized = component.trim().replaceAll("\\s+", "_");

        // Remove or replace special characters (keep only alphanumeric, dashes, underscores)
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_-]", "");

        // Ensure component is not empty after sanitization
        if (sanitized.isEmpty()) {
            sanitized = "unknown";
        }

        return sanitized;
    }

    /**
     * Parses an object path to extract the filename.
     *
     * @param objectPath the full object path
     * @return the filename
     */
    public String extractFilename(String objectPath) {
        if (objectPath == null || objectPath.trim().isEmpty()) {
            return "";
        }

        // Get the last part after the last slash
        int lastSlashIndex = objectPath.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < objectPath.length() - 1) {
            return objectPath.substring(lastSlashIndex + 1);
        }

        return objectPath;
    }

    /**
     * Builds a full storage path with custom segments.
     *
     * @param segments the path segments
     * @return the full path joined with slashes
     */
    public String buildPath(String... segments) {
        if (segments == null || segments.length == 0) {
            return "";
        }

        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (segments[i] != null && !segments[i].trim().isEmpty()) {
                if (i > 0) {
                    pathBuilder.append("/");
                }
                pathBuilder.append(sanitize(segments[i]));
            }
        }

        return pathBuilder.toString();
    }
}
