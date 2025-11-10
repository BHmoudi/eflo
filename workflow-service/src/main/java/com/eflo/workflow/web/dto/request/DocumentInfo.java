package com.eflo.workflow.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for document information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotBlank(message = "Document path is required")
    private String documentPath;

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotNull(message = "Document size is required")
    private Long documentSize;

    private String description;

    private String uploadedBy;
}
