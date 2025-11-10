package com.eflo.document.domain.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for uploading a new document.
 * Contains all necessary information for document upload including file, metadata, and associations.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    /**
     * The file to be uploaded.
     * Must not be null and must not be empty.
     */
    @NotNull(message = "File is required")
    private MultipartFile file;

    /**
     * The ID of the document type.
     * Must be a positive number.
     */
    @NotNull(message = "Document type ID is required")
    @Positive(message = "Document type ID must be positive")
    private Long documentTypeId;

    /**
     * The ID of the associated order.
     * Must be a positive number.
     */
    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    /**
     * The order number associated with this document.
     * Must not be blank and have a maximum length of 100 characters.
     */
    @NotBlank(message = "Order number is required")
    @Size(max = 100, message = "Order number must not exceed 100 characters")
    private String orderNumber;

    /**
     * Custom metadata for the document as key-value pairs.
     * Optional field for additional document information.
     */
    private Map<String, Object> metadata;

    /**
     * Tags associated with the document for categorization and search.
     * Each tag must not exceed 50 characters.
     */
    @Size(max = 20, message = "Cannot have more than 20 tags")
    private List<@NotBlank @Size(max = 50, message = "Tag must not exceed 50 characters") String> tags;

    /**
     * Description of the document.
     * Optional field with maximum length of 2000 characters.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Expiration date of the document.
     * Must be in the future if provided.
     */
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    /**
     * Business unit ID for the document.
     * Optional field for multi-tenant scenarios.
     */
    @Positive(message = "Business unit ID must be positive")
    private Long businessUnitId;
}
