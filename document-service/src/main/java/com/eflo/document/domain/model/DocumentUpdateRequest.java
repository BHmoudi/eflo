package com.eflo.document.domain.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating an existing document.
 * Contains updatable fields for document metadata and properties.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpdateRequest {

    /**
     * Updated description of the document.
     * Optional field with maximum length of 2000 characters.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Updated custom metadata for the document as key-value pairs.
     * This will merge with or replace existing metadata.
     */
    private Map<String, Object> metadata;

    /**
     * Updated tags associated with the document.
     * This will replace existing tags if provided.
     */
    @Size(max = 20, message = "Cannot have more than 20 tags")
    private List<@NotBlank @Size(max = 50, message = "Tag must not exceed 50 characters") String> tags;

    /**
     * Updated expiration date of the document.
     * Must be in the future if provided.
     */
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    /**
     * Flag to indicate if metadata should be merged (true) or replaced (false).
     * Default is true (merge mode).
     */
    @Builder.Default
    private Boolean mergeMetadata = true;
}
