package com.eflo.document.storage;

import com.eflo.document.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * File Validator for validating uploaded files.
 * Validates file extensions, MIME types, and file sizes.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidator {

    private final FileUploadConfig fileUploadConfig;
    private final Tika tika = new Tika();

    /**
     * Validates a multipart file against configured rules.
     *
     * @param file the file to validate
     * @return ValidationResult containing validation status and errors
     */
    public ValidationResult validateFile(MultipartFile file) {
        log.debug("Validating file: {}", file.getOriginalFilename());

        List<String> errors = new ArrayList<>();

        // Check if file is empty
        if (file.isEmpty()) {
            errors.add("File is empty");
            return ValidationResult.invalid(errors);
        }

        // Validate file size
        if (!checkFileSize(file)) {
            errors.add(String.format("File size exceeds maximum allowed size of %d MB",
                    fileUploadConfig.getMaxFileSizeMb()));
        }

        // Validate file extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedExtension(extension)) {
            errors.add(String.format("File extension '%s' is not allowed. Allowed extensions: %s",
                    extension, fileUploadConfig.getAllowedExtensions()));
        }

        // Validate MIME type
        String mimeType = detectMimeType(file);
        if (!isAllowedMimeType(mimeType)) {
            errors.add(String.format("File MIME type '%s' is not allowed. Allowed types: %s",
                    mimeType, fileUploadConfig.getAllowedMimeTypes()));
        }

        // Check for MIME type and extension mismatch
        if (!errors.isEmpty()) {
            log.warn("File validation failed for {}: {}", file.getOriginalFilename(), errors);
            return ValidationResult.invalid(errors);
        }

        log.info("File validation passed for: {}", file.getOriginalFilename());
        return ValidationResult.valid();
    }

    /**
     * Checks if a file extension is allowed.
     *
     * @param extension the file extension (without dot)
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            log.debug("Extension is null or empty");
            return false;
        }

        boolean isAllowed = fileUploadConfig.isAllowedExtension(extension);
        log.debug("Extension '{}' allowed: {}", extension, isAllowed);
        return isAllowed;
    }

    /**
     * Checks if a MIME type is allowed.
     *
     * @param mimeType the MIME type to check
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            log.debug("MIME type is null or empty");
            return false;
        }

        boolean isAllowed = fileUploadConfig.isAllowedMimeType(mimeType);
        log.debug("MIME type '{}' allowed: {}", mimeType, isAllowed);
        return isAllowed;
    }

    /**
     * Checks if a file size is within the allowed limit.
     *
     * @param file the file to check
     * @return true if size is allowed, false otherwise
     */
    public boolean checkFileSize(MultipartFile file) {
        long fileSize = file.getSize();
        long maxSize = fileUploadConfig.getMaxFileSizeBytes();

        boolean withinLimit = fileSize <= maxSize;
        log.debug("File size: {} bytes, Max allowed: {} bytes, Within limit: {}",
                fileSize, maxSize, withinLimit);

        return withinLimit;
    }

    /**
     * Detects the MIME type of a file using Apache Tika.
     *
     * @param file the file to analyze
     * @return detected MIME type
     */
    public String detectMimeType(MultipartFile file) {
        log.debug("Detecting MIME type for file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            String mimeType = tika.detect(inputStream, file.getOriginalFilename());
            log.debug("Detected MIME type: {} for file: {}", mimeType, file.getOriginalFilename());
            return mimeType;
        } catch (IOException e) {
            log.error("Failed to detect MIME type for file {}: {}",
                    file.getOriginalFilename(), e.getMessage());

            // Fallback to content type from the file
            String contentType = file.getContentType();
            if (contentType != null && !contentType.isEmpty()) {
                log.debug("Using content type from file: {}", contentType);
                return contentType;
            }

            return "application/octet-stream";
        }
    }

    /**
     * Detects the MIME type from an InputStream.
     *
     * @param inputStream the input stream
     * @param filename the filename (used as hint)
     * @return detected MIME type
     */
    public String detectMimeType(InputStream inputStream, String filename) {
        log.debug("Detecting MIME type for stream with filename hint: {}", filename);

        try {
            String mimeType = tika.detect(inputStream, filename);
            log.debug("Detected MIME type: {} for filename: {}", mimeType, filename);
            return mimeType;
        } catch (IOException e) {
            log.error("Failed to detect MIME type for stream: {}", e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * Gets the file extension from a filename.
     *
     * @param filename the filename
     * @return the extension (without dot) in lowercase
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }

        String extension = FilenameUtils.getExtension(filename);
        return extension != null ? extension.toLowerCase() : "";
    }

    /**
     * Validates file name for potentially dangerous patterns.
     *
     * @param filename the filename to validate
     * @return ValidationResult
     */
    public ValidationResult validateFilename(String filename) {
        log.debug("Validating filename: {}", filename);

        List<String> errors = new ArrayList<>();

        if (filename == null || filename.trim().isEmpty()) {
            errors.add("Filename is null or empty");
            return ValidationResult.invalid(errors);
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            errors.add("Filename contains invalid characters (path traversal attempt)");
        }

        // Check for excessively long filenames
        if (filename.length() > 255) {
            errors.add("Filename is too long (maximum 255 characters)");
        }

        // Check for null bytes
        if (filename.contains("\0")) {
            errors.add("Filename contains null bytes");
        }

        if (!errors.isEmpty()) {
            log.warn("Filename validation failed for {}: {}", filename, errors);
            return ValidationResult.invalid(errors);
        }

        log.debug("Filename validation passed for: {}", filename);
        return ValidationResult.valid();
    }

    /**
     * Validation Result class to hold validation outcome.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }

        /**
         * Creates a valid result.
         *
         * @return valid ValidationResult
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, new ArrayList<>());
        }

        /**
         * Creates an invalid result with errors.
         *
         * @param errors list of error messages
         * @return invalid ValidationResult
         */
        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        /**
         * Creates an invalid result with a single error.
         *
         * @param error error message
         * @return invalid ValidationResult
         */
        public static ValidationResult invalid(String error) {
            List<String> errors = new ArrayList<>();
            errors.add(error);
            return new ValidationResult(false, errors);
        }

        /**
         * Checks if validation passed.
         *
         * @return true if valid, false otherwise
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Gets the list of validation errors.
         *
         * @return list of errors (empty if valid)
         */
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        /**
         * Gets the first error message.
         *
         * @return first error or empty string if valid
         */
        public String getFirstError() {
            return errors.isEmpty() ? "" : errors.get(0);
        }

        /**
         * Gets all errors as a single string.
         *
         * @return errors joined by semicolons
         */
        public String getAllErrors() {
            return String.join("; ", errors);
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, errors=%s}", valid, errors);
        }
    }
}
