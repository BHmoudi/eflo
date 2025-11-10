package com.eflo.user.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Error response for validation errors with field-level details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    @Builder.Default
    private List<FieldError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;
    }

    public void addFieldError(String field, String rejectedValue, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(FieldError.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .message(message)
                .build());
    }

    public static ValidationErrorResponse of(int status, String message, String path) {
        return ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error("Validation Failed")
                .message(message)
                .path(path)
                .errors(new ArrayList<>())
                .build();
    }
}
