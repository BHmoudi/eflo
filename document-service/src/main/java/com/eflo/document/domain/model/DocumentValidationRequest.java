package com.eflo.document.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating or rejecting a document.
 * Contains validation decision and associated comments.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentValidationRequest {

    /**
     * Flag indicating whether the document is approved.
     * true = approved/validated, false = rejected.
     */
    @NotNull(message = "Approval status is required")
    private Boolean isApproved;

    /**
     * Comments or reasons for the validation decision.
     * Required for rejections, optional for approvals.
     */
    @Size(max = 2000, message = "Comments must not exceed 2000 characters")
    private String comments;

    /**
     * Username or identifier of the person validating the document.
     * Must not be blank and have a maximum length of 255 characters.
     */
    @NotBlank(message = "Validator identifier is required")
    @Size(max = 255, message = "Validated by must not exceed 255 characters")
    private String validatedBy;

    /**
     * Check if this is a rejection request.
     *
     * @return true if the document is being rejected
     */
    public boolean isRejection() {
        return Boolean.FALSE.equals(isApproved);
    }

    /**
     * Check if this is an approval request.
     *
     * @return true if the document is being approved
     */
    public boolean isApproval() {
        return Boolean.TRUE.equals(isApproved);
    }
}
