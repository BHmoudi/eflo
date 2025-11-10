package com.eflo.document.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;
import java.util.List;

/**
 * File Upload Configuration.
 * Configures multipart file upload settings and validation parameters.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Getter
@Configuration
public class FileUploadConfig {

    @Value("${eflo.document.upload.max-file-size-mb:100}")
    private int maxFileSizeMb;

    @Value("${eflo.document.upload.allowed-extensions:pdf,jpg,jpeg,png}")
    private List<String> allowedExtensions;

    @Value("${eflo.document.upload.blocked-extensions:}")
    private List<String> blockedExtensions;

    @Value("${eflo.document.upload.allowed-mime-types:application/pdf,image/jpeg,image/png}")
    private List<String> allowedMimeTypes;

    @Value("${spring.servlet.multipart.max-file-size:100MB}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:100MB}")
    private String maxRequestSize;

    @Value("${spring.servlet.multipart.file-size-threshold:2MB}")
    private String fileSizeThreshold;

    /**
     * Configures the multipart resolver for handling file uploads.
     * Uses the standard servlet multipart resolver.
     *
     * @return configured MultipartResolver
     */
    @Bean
    public MultipartResolver multipartResolver() {
        log.info("Configuring multipart resolver with max file size: {}", maxFileSize);
        return new StandardServletMultipartResolver();
    }

    /**
     * Configures multipart settings for file uploads.
     * Sets maximum file size, request size, and file size threshold.
     *
     * @return configured MultipartConfigElement
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Set max file size
        factory.setMaxFileSize(DataSize.parse(maxFileSize));
        log.debug("Max file size set to: {}", maxFileSize);

        // Set max request size
        factory.setMaxRequestSize(DataSize.parse(maxRequestSize));
        log.debug("Max request size set to: {}", maxRequestSize);

        // Set file size threshold for writing to disk
        factory.setFileSizeThreshold(DataSize.parse(fileSizeThreshold));
        log.debug("File size threshold set to: {}", fileSizeThreshold);

        log.info("Multipart config initialized - Max File: {}, Max Request: {}, Threshold: {}",
                maxFileSize, maxRequestSize, fileSizeThreshold);

        return factory.createMultipartConfig();
    }

    /**
     * Gets the maximum file size in bytes.
     *
     * @return maximum file size in bytes
     */
    public long getMaxFileSizeBytes() {
        return (long) maxFileSizeMb * 1024 * 1024;
    }

    /**
     * Checks if a file extension is allowed.
     *
     * @param extension the file extension to check (without dot)
     * @return true if the extension is allowed, false otherwise
     */
    public boolean isAllowedExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }

        String normalizedExtension = extension.toLowerCase().trim();

        // Check if extension is blocked
        if (blockedExtensions != null && blockedExtensions.stream()
                .anyMatch(blocked -> blocked.equalsIgnoreCase(normalizedExtension))) {
            log.warn("Extension {} is in blocked list", normalizedExtension);
            return false;
        }

        // Check if extension is in allowed list
        boolean isAllowed = allowedExtensions != null && allowedExtensions.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(normalizedExtension));

        if (!isAllowed) {
            log.debug("Extension {} is not in allowed list", normalizedExtension);
        }

        return isAllowed;
    }

    /**
     * Checks if a MIME type is allowed.
     *
     * @param mimeType the MIME type to check
     * @return true if the MIME type is allowed, false otherwise
     */
    public boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return false;
        }

        String normalizedMimeType = mimeType.toLowerCase().trim();

        boolean isAllowed = allowedMimeTypes != null && allowedMimeTypes.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(normalizedMimeType));

        if (!isAllowed) {
            log.debug("MIME type {} is not in allowed list", normalizedMimeType);
        }

        return isAllowed;
    }

    /**
     * Gets the list of allowed file extensions.
     *
     * @return list of allowed extensions
     */
    public List<String> getAllowedExtensions() {
        return allowedExtensions != null ? List.copyOf(allowedExtensions) : List.of();
    }

    /**
     * Gets the list of blocked file extensions.
     *
     * @return list of blocked extensions
     */
    public List<String> getBlockedExtensions() {
        return blockedExtensions != null ? List.copyOf(blockedExtensions) : List.of();
    }

    /**
     * Gets the list of allowed MIME types.
     *
     * @return list of allowed MIME types
     */
    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes != null ? List.copyOf(allowedMimeTypes) : List.of();
    }
}
