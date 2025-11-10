package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating document status in Order Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusUpdateRequest {

    private String documentStatus;
    private Integer totalDocuments;
    private Integer validatedDocuments;
    private Integer pendingDocuments;
    private Integer rejectedDocuments;
    private Boolean allDocumentsValidated;
    private String updatedBy;
}
