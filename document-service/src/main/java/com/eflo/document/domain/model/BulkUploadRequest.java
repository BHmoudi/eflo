package com.eflo.document.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for bulk uploading multiple documents.
 * Contains common metadata and list of files to upload.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadRequest {

    /**
     * List of files to be uploaded in bulk.
     * Must contain at least one file and not exceed 50 files.
     */
    @NotEmpty(message = "At least one file is required for bulk upload")
    @Size(min = 1, max = 50, message = "Bulk upload must contain between 1 and 50 files")
    private List<MultipartFile> files;

    /**
     * Common document type ID for all files.
     * If provided, all files will use this document type.
     */
    @Positive(message = "Document type ID must be positive")
    private Long documentTypeId;

    /**
     * Common order ID for all documents.
     */
    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    /**
     * Common order number for all documents.
     */
    @NotNull(message = "Order number is required")
    @Size(max = 100, message = "Order number must not exceed 100 characters")
    private String orderNumber;

    /**
     * Common metadata to apply to all uploaded documents.
     * Can be overridden by individual file metadata.
     */
    private Map<String, Object> commonMetadata;

    /**
     * Common tags to apply to all uploaded documents.
     * Can be combined with individual file tags.
     */
    @Size(max = 20, message = "Cannot have more than 20 common tags")
    private List<@Size(max = 50) String> commonTags;

    /**
     * Common expiration date for all documents.
     * Can be overridden by individual file expiration dates.
     */
    private LocalDate commonExpirationDate;

    /**
     * Individual file metadata for each uploaded file.
     * Map key is the original filename.
     */
    @Valid
    private List<FileMetadata> fileMetadataList;

    /**
     * Business unit ID for the documents.
     */
    @Positive(message = "Business unit ID must be positive")
    private Long businessUnitId;

    /**
     * Flag to continue processing even if some files fail.
     * If true, successful uploads are committed even if some fail.
     * If false, all uploads are rolled back if any file fails.
     */
    @Builder.Default
    private Boolean continueOnError = true;

    /**
     * Metadata for an individual file in bulk upload.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileMetadata {

        /**
         * Original filename to match with the uploaded file.
         */
        @NotNull(message = "Filename is required")
        @Size(max = 500, message = "Filename must not exceed 500 characters")
        private String filename;

        /**
         * Document type ID specific to this file.
         * Overrides common document type ID if provided.
         */
        @Positive(message = "Document type ID must be positive")
        private Long documentTypeId;

        /**
         * Description specific to this file.
         */
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        private String description;

        /**
         * Additional metadata specific to this file.
         * Will be merged with common metadata.
         */
        private Map<String, Object> metadata;

        /**
         * Additional tags specific to this file.
         * Will be combined with common tags.
         */
        @Size(max = 20, message = "Cannot have more than 20 tags")
        private List<@Size(max = 50) String> tags;

        /**
         * Expiration date specific to this file.
         * Overrides common expiration date if provided.
         */
        private LocalDate expirationDate;
    }

    /**
     * Get metadata for a specific filename.
     *
     * @param filename the filename to search for
     * @return FileMetadata if found, null otherwise
     */
    public FileMetadata getMetadataForFile(String filename) {
        if (fileMetadataList == null) {
            return null;
        }
        return fileMetadataList.stream()
                .filter(fm -> fm.getFilename().equals(filename))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if common document type is provided.
     *
     * @return true if common document type ID is set
     */
    public boolean hasCommonDocumentType() {
        return documentTypeId != null;
    }

    /**
     * Check if common metadata is provided.
     *
     * @return true if common metadata is set
     */
    public boolean hasCommonMetadata() {
        return commonMetadata != null && !commonMetadata.isEmpty();
    }

    /**
     * Check if common tags are provided.
     *
     * @return true if common tags are set
     */
    public boolean hasCommonTags() {
        return commonTags != null && !commonTags.isEmpty();
    }
}
